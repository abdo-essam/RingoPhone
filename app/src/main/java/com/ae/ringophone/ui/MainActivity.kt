package com.ae.ringophone.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ae.ringophone.ui.screens.MainScreen
import com.ae.ringophone.ui.theme.RingoPhoneTheme
import com.ae.ringophone.ui.viewmodels.MainViewModel
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                FirebaseDatabase.getInstance().reference.child("test").setValue("Hello Abdo!")
            }
            RingoPhoneTheme {
                Box (Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)){
                    MainScreen()
                }
            }
        }
    }
}