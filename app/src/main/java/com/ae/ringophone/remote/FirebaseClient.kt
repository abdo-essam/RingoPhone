package com.ae.ringophone.remote

import com.ae.ringophone.utils.FirebaseFieldNames
import com.ae.ringophone.utils.MatchState
import com.ae.ringophone.utils.MyValueEventListener
import com.ae.ringophone.utils.SharedPrefHelper
import com.google.firebase.database.DataSnapshot
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

    fun observerUserStatus(callback: (MatchState) -> Unit) {
        coroutineScope.launch {
            // remove self data
            removeSelfData()

            // update self data to match state
            updateSelfStatus(StatusDataModel(type = StatusDataModelTypes.LookingForMatch))
            val statusReference =
                database.child(FirebaseFieldNames.USERS).child(prefHelper.getUserId()).child(
                    FirebaseFieldNames.STATUS
                )

            statusReference.addValueEventListener(
                object : MyValueEventListener() {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        super.onDataChange(snapshot)
                        snapshot.getValue(StatusDataModel::class.java)?.let { status ->
                            val newStatus = when (status?.type) {
                                StatusDataModelTypes.IDLE -> MatchState.IDLE
                                StatusDataModelTypes.LookingForMatch -> MatchState.LookingForMatchState
                                StatusDataModelTypes.OfferedMatch -> MatchState.OfferedMatchState(
                                    status.participant!!
                                )

                                StatusDataModelTypes.ReceivedMatch -> MatchState.ReceivedMatchState(
                                    status.participant!!
                                )

                                StatusDataModelTypes.Connected -> MatchState.Connected
                                else -> null
                            }

                            newStatus?.let {
                                callback(it)
                            } ?: coroutineScope.launch {
                                updateSelfStatus(
                                    status = StatusDataModel(type = StatusDataModelTypes.LookingForMatch)
                                )
                                callback(MatchState.LookingForMatchState)
                            }

                        } ?: coroutineScope.launch {
                            updateSelfStatus(StatusDataModel(type = StatusDataModelTypes.LookingForMatch))
                            callback(MatchState.LookingForMatchState)
                        }
                    }
                }
            )
        }
    }

    private suspend fun updateSelfStatus(status: StatusDataModel) {

        database.child(FirebaseFieldNames.USERS).child(prefHelper.getUserId())
            .child(FirebaseFieldNames.STATUS).setValue(status).await()
    }

    private suspend fun removeSelfData() {
        database.child(FirebaseFieldNames.USERS).child(prefHelper.getUserId())
            .child(FirebaseFieldNames.DATA).removeValue().await()
    }

    fun clear() {
        coroutineScope.cancel()
    }

}