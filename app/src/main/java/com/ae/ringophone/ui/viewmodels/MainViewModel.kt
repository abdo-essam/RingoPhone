package com.ae.ringophone.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ae.ringophone.remote.FirebaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseClient: FirebaseClient
) : ViewModel() {

    fun permissionsGranted() {
        firebaseClient.observerUserStatus {
            status -> Log.d("PermissionsGranted", status.toString())
        }
    }

}