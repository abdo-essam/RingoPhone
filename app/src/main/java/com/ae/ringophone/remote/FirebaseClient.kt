package com.ae.ringophone.remote

import android.util.Log
import com.ae.ringophone.utils.FirebaseFieldNames
import com.ae.ringophone.utils.MatchState
import com.ae.ringophone.utils.MyValueEventListener
import com.ae.ringophone.utils.RingoPhoneApplication
import com.ae.ringophone.utils.SharedPrefHelper
import com.ae.ringophone.utils.SignalDataModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(
    private val database: DatabaseReference,
    private val prefHelper: SharedPrefHelper,
    private val gson: Gson
) {
    // to handle all requests to Firebase
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun observeUserStatus(callback: (MatchState) -> Unit) {
        coroutineScope.launch {
            removeSelfData()
            updateSelfStatus(StatusDataModel(type = StatusDataModelTypes.LookingForMatch))

            val userId = prefHelper.getUserId()
            val statusRef = database.child(FirebaseFieldNames.USERS).child(userId)
                .child(FirebaseFieldNames.STATUS)

            statusRef.addValueEventListener(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(StatusDataModel::class.java)?.let { status ->
                        val newState = when (status.type) {
                            StatusDataModelTypes.LookingForMatch -> MatchState.LookingForMatchState
                            StatusDataModelTypes.OfferedMatch -> MatchState.OfferedMatchState(status.participant!!)
                            StatusDataModelTypes.ReceivedMatch -> MatchState.ReceivedMatchState(status.participant!!)
                            StatusDataModelTypes.IDLE -> MatchState.IDLE
                            StatusDataModelTypes.Connected -> MatchState.Connected
                            else -> null
                        }

                        newState?.let { callback(it) } ?: coroutineScope.launch {
                            updateSelfStatus(StatusDataModel(type = StatusDataModelTypes.LookingForMatch))
                            callback(MatchState.LookingForMatchState)
                        }
                    } ?: coroutineScope.launch {
                        updateSelfStatus(StatusDataModel(type = StatusDataModelTypes.LookingForMatch))
                        callback(MatchState.LookingForMatchState)
                    }
                }
            })
        }
    }



    suspend fun findNextMatch() {
        removeSelfData()
        findAvailableParticipant { foundTarget ->
            Log.d(RingoPhoneApplication.TAG, "findNextMatch: $foundTarget")
            foundTarget?.let { target ->
                database.child(FirebaseFieldNames.USERS).child(target)
                    .child(FirebaseFieldNames.STATUS).setValue(
                        StatusDataModel(
                            participant = prefHelper.getUserId(), type = StatusDataModelTypes.ReceivedMatch
                        )
                    )

                coroutineScope.launch {
                    updateSelfStatus(StatusDataModel(type = StatusDataModelTypes.OfferedMatch, participant = target))
                }
            }
        }
    }

    private fun findAvailableParticipant(callback: (String?) -> Unit) {
        database.child(FirebaseFieldNames.USERS).orderByChild("status/type")
            .equalTo(StatusDataModelTypes.LookingForMatch.name)
            .addListenerForSingleValueEvent(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var foundTarget: String? = null
                    snapshot.children.forEach { childSnapshot ->
                        if (childSnapshot.key != prefHelper.getUserId()) {
                            foundTarget = childSnapshot.key
                            return@forEach
                        }
                    }
                    callback(foundTarget)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun observeIncomingSignals(callback: (SignalDataModel) -> Unit) {
        database.child(FirebaseFieldNames.USERS).child(prefHelper.getUserId())
            .child(FirebaseFieldNames.DATA).addValueEventListener(object : MyValueEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    super.onDataChange(snapshot)
                    runCatching {
                        gson.fromJson(snapshot.value.toString(), SignalDataModel::class.java)
                    }.onSuccess {
                        if (it != null) callback(it)
                    }.onFailure {
                        Log.d(RingoPhoneApplication.TAG, "onDataChange: ${it.message}")
                    }
                }
            })
    }

    // send signal to participant
    suspend fun updateParticipantDataModel(participantId: String, data: SignalDataModel) {
        database.child(FirebaseFieldNames.USERS).child(participantId).child(FirebaseFieldNames.DATA)
            .setValue(gson.toJson(data)).await()
    }

    suspend fun updateSelfStatus(status: StatusDataModel) {

        database.child(FirebaseFieldNames.USERS).child(prefHelper.getUserId())
            .child(FirebaseFieldNames.STATUS).setValue(status).await()
    }

    suspend fun removeSelfData() {
        database.child(FirebaseFieldNames.USERS).child(prefHelper.getUserId())
            .child(FirebaseFieldNames.DATA).removeValue().await()
    }

    suspend fun updateParticipantStatus(participantId: String, status: StatusDataModel) {
        database.child(FirebaseFieldNames.USERS).child(participantId)
            .child(FirebaseFieldNames.STATUS).setValue(status).await()
    }

    fun clear() {
        coroutineScope.cancel()
    }

}