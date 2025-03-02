package com.ae.ringophone.remote

import com.ae.ringophone.utils.SharedPrefHelper
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseClient @Inject constructor(
    private val database: DatabaseReference,
    private val prefHelper: SharedPrefHelper,
    private val gson: Gson
) {
    init {
        database.child(prefHelper.getUserId()).setValue("Hello from Ringo Phone")
    }

}