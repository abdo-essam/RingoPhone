package com.ae.ringophone.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ae.ringophone.remote.FirebaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseClient: FirebaseClient
) : ViewModel() {

    fun permissionsGranted() {
        firebaseClient.observerUserStatus { status ->
        }

        viewModelScope.launch {
            firebaseClient.findNextMatch()
        }
    }

}