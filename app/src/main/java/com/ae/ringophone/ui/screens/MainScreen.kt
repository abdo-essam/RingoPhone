package com.ae.ringophone.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ae.ringophone.R
import com.ae.ringophone.ui.components.ChatSection
import com.ae.ringophone.ui.components.SurfaceViewRendererComposable
import com.ae.ringophone.ui.viewmodels.MainViewModel
import com.ae.ringophone.utils.ChatItem
import com.ae.ringophone.utils.MatchState
import compose.icons.FeatherIcons
import compose.icons.feathericons.Play
import compose.icons.feathericons.StopCircle

@Composable
fun MainScreen() {
    val viewModel: MainViewModel = hiltViewModel()
    val matchState = viewModel.matchState.collectAsState()
    val chatState = viewModel.chatList.collectAsState()
    val chatText = remember { mutableStateOf("") }
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            viewModel.permissionsGranted()
        } else {
            Toast.makeText(
                context,
                "Camera and Microphone permissions are required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Video Call Section
        Card(
            modifier = Modifier
                .weight(2.5f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SurfaceViewRendererComposable(
                    modifier = Modifier.fillMaxSize(),
                    onSurfaceReady = { renderer ->
                        viewModel.initRemoteSurfaceView(renderer)
                    },
                    message = when (matchState.value) {
                        MatchState.LookingForMatchState -> "Looking for Match..."
                        MatchState.IDLE -> "Press Start to Begin"
                        else -> null
                    }
                )

                this@Card.AnimatedVisibility(
                    visible = matchState.value == MatchState.Connected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    label = "Connection Status"
                ) {
                    ConnectionStatusBadge(
                        isConnected = matchState.value == MatchState.Connected
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Section
        Card(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            ChatSection(chatItems = chatState.value)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Controls Section
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Local Video Preview
            if (matchState.value != MatchState.NewState) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box {
                        SurfaceViewRendererComposable(
                            modifier = Modifier.fillMaxSize(),
                            onSurfaceReady = { renderer ->
                                viewModel.startLocalStream(renderer)
                            },
                            message = null
                        )

                        FloatingActionButton(
                            onClick = { viewModel.switchCamera() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_switch_camera),
                                contentDescription = "Switch Camera"
                            )
                        }
                    }
                }
            }

            // Chat Input and Controls
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                OutlinedTextField(
                    value = chatText.value,
                    onValueChange = { chatText.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("Type your message") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (chatText.value.isNotEmpty()) {
                                    viewModel.sendChatItem(ChatItem(chatText.value, true))
                                    chatText.value = ""
                                }
                            },
                            enabled = chatText.value.isNotEmpty()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (chatText.value.isNotEmpty())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.stopLookingForMatch() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = FeatherIcons.StopCircle,
                            contentDescription = "Stop",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop")
                    }

                    Button(
                        onClick = { viewModel.findNextMatch() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = FeatherIcons.Play,
                            contentDescription = "Next",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusBadge(isConnected: Boolean) {
    Surface(
        color = if (isConnected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.error,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
            Text(
                text = if (isConnected) "Connected" else "Disconnected",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}