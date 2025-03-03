package com.ae.ringophone.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ae.ringophone.remote.FirebaseClient
import com.ae.ringophone.utils.MatchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseClient: FirebaseClient
) : ViewModel() {

    var  matchState: MutableStateFlow<MatchState> = MutableStateFlow(MatchState.NewState)

    fun permissionsGranted() {
        firebaseClient.observerUserStatus { status ->
            matchState.value = status
            when (status) {
                MatchState.LookingForMatchState -> handleLookingForMatch(status)
                is MatchState.OfferedMatchState -> handleSentOffer(status)
                is MatchState.ReceivedMatchState -> handleIncomingMatchCase(status)
                else -> Unit
            }
        }

        firebaseClient.observeIncomingSignals {
            signalDataModel ->
        }
    }

    private fun handleIncomingMatchCase(status: MatchState.ReceivedMatchState) {

    }

    private fun handleSentOffer(status: MatchState.OfferedMatchState) {

    }

    private fun handleLookingForMatch(status: MatchState) {

        // TODO: reset chat list
        // TODO: destroy the connection

        viewModelScope.launch {
            firebaseClient.findNextMatch()
        }

    }


}