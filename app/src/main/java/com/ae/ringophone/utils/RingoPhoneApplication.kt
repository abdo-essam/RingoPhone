package com.ae.ringophone.utils

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class RingoPhoneApplication : Application() {
    companion object {
        val TAG: String? = RingoPhoneApplication::class.simpleName
    }

}