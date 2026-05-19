package com.example.daveai.ui.live

import android.Manifest
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.DaveApplication
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.util.VoiceToTextManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveVoiceScreen(
    viewModel: ChatViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as DaveApplication
    val voiceManager = remember { VoiceToTextManager(context) }
    val daveVoice = app.voiceManager

    val isListening by voiceManager.isListening.collectAsState()
    val spokenText by voiceManager.spokenText.collectAsState()
    val finalText by voiceManager.finalText.collectAsState()
    val rmsLevel by voiceManager.rmsLevel.collectAsState()
    val isDaveSpeaking by daveVoice.isSpeaking.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var isMuted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setLiveMode(true)
    }

    // Interruption / Barge-in Logic
    LaunchedEffect(Unit) {
        voiceManager.onSpeechBegan = {
            if (isDaveSpeaking) {
                daveVoice.stop() // Immediately stop Dave if the user starts talking
            }
        }
    }

    LaunchedEffect(finalText) {
        if (finalText.isNotBlank() && !isMuted && !uiState.isLoading) {
            viewModel.onInputTextChanged(finalText)
            viewModel.sendMessage()
        }
    }

    // Main orchestration loop
    LaunchedEffect(isListening, isDaveSpeaking, uiState.isLoading, isMuted, micPermissionState.status.isGranted) {
        if (micPermissionState.status.isGranted && !isMuted) {
            if (!isListening && !isDaveSpeaking && !uiState.isLoading) {
                delay(300) // Brief pause to prevent rapid error looping
                // Recheck to ensure loading state hasn't changed during the delay
                if (!uiState.isLoading) {
                    voiceManager.startListening()
                }
            }
        } else if (isMuted && isListening) {
            voiceManager.stopListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setLiveMode(false)
            voiceManager.onSpeechBegan = null
            voiceManager.destroy()
            daveVoice.stop()
        }
    }

    // State interpretation
    val statusText = when {
        isMuted -> "Muted"
        isDaveSpeaking -> "Dave is speaking..."
        uiState.isLoading -> "Dave is thinking..."
        isListening -> "Listening..."
        else -> "Ready"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        // Close Button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .size(48.dp)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Siri-style voice waves
            SiriWaveAnimation(
                isListening = isListening,
                isThinking = uiState.isLoading,
                isSpeaking = isDaveSpeaking,
                rms = rmsLevel
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = statusText,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isListening) spokenText else "",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp).height(48.dp) // Fixed height to prevent bouncing
            )
        }

        // Mute Button
        IconButton(
            onClick = { isMuted = !isMuted },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(48.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isMuted) MaterialTheme.colorScheme.error else Color.DarkGray.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                contentDescription = "Mute",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun SiriWaveAnimation(
    isListening: Boolean,
    isThinking: Boolean,
    isSpeaking: Boolean,
    rms: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Calculate amplitude based on state
    val targetAmplitude = when {
        isSpeaking -> 0.5f + (Math.random().toFloat() * 0.5f) // Fake RMS for Dave
        isListening -> 0.2f + (rms.coerceIn(0f, 10f) / 10f)
        isThinking -> 0.3f
        else -> 0.05f
    }

    val currentAmplitude by animateFloatAsState(
        targetValue = targetAmplitude,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dynamic_amplitude"
    )
    
    // Wave colors
    val color1 by animateColorAsState(targetValue = if (isSpeaking) Color(0xFF18FFFF) else if (isThinking) Color(0xFFFFD54F) else if (isListening) Color(0xFFB388FF) else Color.DarkGray, label = "c1")
    val color2 by animateColorAsState(targetValue = if (isSpeaking) Color(0xFF00B0FF) else if (isThinking) Color(0xFFFF6F00) else if (isListening) Color(0xFF7C4DFF) else Color.Gray, label = "c2")
    val color3 by animateColorAsState(targetValue = if (isSpeaking) Color(0xFF00E5FF) else if (isThinking) Color(0xFFFFC107) else if (isListening) Color(0xFF651FFF) else Color.LightGray, label = "c3")

    androidx.compose.foundation.Canvas(modifier = Modifier.size(300.dp, 200.dp)) {
        val w = size.width
        val h = size.height
        val midY = h / 2f
        val maxAmplitude = h / 2f * currentAmplitude

        // Draw multiple waves
        val colors = listOf(color1, color2, color3)
        val frequencies = listOf(1.5f, 2.0f, 2.5f)
        val speeds = listOf(1f, 1.2f, 1.5f)
        val phaseOffsets = listOf(0f, (Math.PI / 4.0).toFloat(), (Math.PI / 2.0).toFloat())

        colors.forEachIndexed { index, color ->
            val path = androidx.compose.ui.graphics.Path()
            val freq = frequencies[index]
            val speed = speeds[index]
            val pOffset = phaseOffsets[index]
            
            for (x in 0..w.toInt() step 5) {
                val normalizedX = x / w
                // Taper the ends (bell curve)
                val t = normalizedX * 2f - 1f
                val taper = 1f - (t * t)
                
                val yOffset = kotlin.math.sin(normalizedX * freq * 2f * Math.PI.toFloat() + (phase * speed) + pOffset)
                val y = midY + yOffset * maxAmplitude * taper

                if (x == 0) {
                    path.moveTo(x.toFloat(), y)
                } else {
                    path.lineTo(x.toFloat(), y)
                }
            }

            drawPath(
                path = path,
                color = color.copy(alpha = 0.8f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
    }
}
