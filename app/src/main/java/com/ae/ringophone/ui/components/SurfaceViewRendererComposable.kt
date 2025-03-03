package com.ae.ringophone.ui.components

import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.SurfaceViewRenderer

@Composable
fun SurfaceViewRendererComposable(
    modifier: Modifier = Modifier,
    onSurfaceReady: (SurfaceViewRenderer) -> Unit,
    message: String? = null,
    isLoading: Boolean = false,
    isConnected: Boolean = false,
    showOverlay: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Base video surface
        if (message.isNullOrEmpty()) {
            VideoSurface(onSurfaceReady = onSurfaceReady)
        }

        // Gradient overlay for better text visibility
        if (showOverlay) {
            GradientOverlay()
        }

        // Message or loading state
        if (!message.isNullOrEmpty() || isLoading) {
            MessageOverlay(
                message = message,
                isLoading = isLoading
            )
        }

        // Connection status indicator
        AnimatedVisibility(
            visible = isConnected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ConnectionStatusBadge(isConnected = isConnected)
        }
    }
}

@Composable
private fun VideoSurface(
    onSurfaceReady: (SurfaceViewRenderer) -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            FrameLayout(ctx).apply {
                addView(
                    SurfaceViewRenderer(context).also { renderer ->
                        onSurfaceReady(renderer)
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun GradientOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.4f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.4f)
                    )
                )
            )
    )
}

@Composable
private fun MessageOverlay(
    message: String?,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!message.isNullOrEmpty()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusBadge(
    isConnected: Boolean
) {
    Surface(
        color = if (isConnected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.error,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onPrimary)
            )

            // Status text
            Text(
                text = if (isConnected) "Connected" else "Disconnected",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

// Optional: Quality indicator
@Composable
fun ConnectionQualityIndicator(
    quality: ConnectionQuality,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            index < quality.bars -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}

enum class ConnectionQuality(val bars: Int) {
    POOR(1),
    MEDIUM(2),
    GOOD(3)
}

// Optional: Video controls overlay
@Composable
fun VideoControlsOverlay(
    isMuted: Boolean,
    isVideoEnabled: Boolean,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = onToggleMute,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Icon(
                imageVector = if (isMuted)
                    Icons.Default.MicOff
                else
                    Icons.Default.Mic,
                contentDescription = "Toggle Mute",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onToggleVideo,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Icon(
                imageVector = if (isVideoEnabled)
                    Icons.Default.Videocam
                else
                    Icons.Default.VideocamOff,
                contentDescription = "Toggle Video",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Usage example
@Composable
fun VideoCallScreen() {
    var isMuted by remember { mutableStateOf(false) }
    var isVideoEnabled by remember { mutableStateOf(true) }
    var connectionQuality by remember { mutableStateOf(ConnectionQuality.GOOD) }

    Box(modifier = Modifier.fillMaxSize()) {
        SurfaceViewRendererComposable(
            modifier = Modifier.fillMaxSize(),
            onSurfaceReady = { /* Handle surface ready */ },
            isConnected = true
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            ConnectionQualityIndicator(
                quality = connectionQuality
            )
        }

        VideoControlsOverlay(
            isMuted = isMuted,
            isVideoEnabled = isVideoEnabled,
            onToggleMute = { isMuted = !isMuted },
            onToggleVideo = { isVideoEnabled = !isVideoEnabled },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}