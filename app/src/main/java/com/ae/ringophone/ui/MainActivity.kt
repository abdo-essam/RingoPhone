package com.ae.ringophone.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ae.ringophone.ui.screens.MainScreen
import com.ae.ringophone.ui.theme.RingoPhoneTheme
import com.ae.ringophone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            // Enable edge-to-edge drawing
            WindowCompat.setDecorFitsSystemWindows(window, false)

            RingoPhoneTheme {
                Box (Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)){
                    MainScreen()
                }
            }
        }
    }
}