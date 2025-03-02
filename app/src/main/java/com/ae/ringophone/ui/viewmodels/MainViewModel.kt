package com.ae.ringophone.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ae.ringophone.remote.FirebaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseClient: FirebaseClient
) : ViewModel() {


}