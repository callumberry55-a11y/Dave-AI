package com.example.daveai.ui.live

import android.Manifest
import android.app.Activity
import android.util.Base64
import android.view.WindowManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.daveai.DaveApplication
import com.example.daveai.data.model.DaveMode
import com.example.daveai.ui.chat.AttachedFile
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
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as DaveApplication
    val activity = context as? Activity
    val voiceManager = remember { VoiceToTextManager(context) }
    val daveVoice = app.voiceManager

    val isListening by voiceManager.isListening.collectAsState()
    val spokenText by voiceManager.spokenText.collectAsState()
    val finalText by voiceManager.finalText.collectAsState()
    val rmsLevel by voiceManager.rmsLevel.collectAsState()
    val isDaveSpeaking by daveVoice.isSpeaking.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    var isMuted by remember { mutableStateOf(value = false) }
    var isVisionEnabled by remember { mutableStateOf(value = false) }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var previewUseCase: Preview? by remember { mutableStateOf(null) }

    // Initialize CameraX
    LaunchedEffect(isVisionEnabled, cameraPermissionState.status.isGranted) {
        if (isVisionEnabled && (cameraPermissionState.status.isGranted)) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder().build()
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                        imageCapture = capture
                        previewUseCase = preview
                    } catch (e: Exception) {
                        android.util.Log.e("DaveVision", "Camera bind failed", e)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        } else {
            imageCapture = null
            previewUseCase = null
        }
    }

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
        
        daveVoice.onStartSpeaking = {
            activity?.runOnUiThread {
                voiceManager.cancel()
            }
        }
        daveVoice.onDoneSpeaking = {
            activity?.runOnUiThread {
                if (!isMuted) voiceManager.startListening()
            }
        }
        daveVoice.onErrorSpeaking = {
            activity?.runOnUiThread {
                if (!isMuted) voiceManager.startListening()
            }
        }
    }

    LaunchedEffect(finalText) {
        if (finalText.isNotBlank() && !isMuted && !uiState.isLoading) {
            val currentText = finalText
            
            val lowerText = currentText.lowercase()
            val hasVisionTrigger = lowerText.contains("look") || 
                                   lowerText.contains("see") || 
                                   lowerText.contains("what is") || 
                                   lowerText.contains("this") || 
                                   lowerText.contains("read") || 
                                   lowerText.contains("camera") || 
                                   lowerText.contains("vision") || 
                                   lowerText.contains("watch")

            if (isVisionEnabled && imageCapture != null && hasVisionTrigger) {
                // If Vision is active AND the user asked about their surroundings, take a photo
                imageCapture?.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val buffer = image.planes[0].buffer
                            val bytes = ByteArray(buffer.remaining())
                            buffer[bytes]
                            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                            
                            viewModel.addAttachment(
                                AttachedFile(
                                    uri = android.net.Uri.EMPTY,
                                    name = "dave_vision_capture.jpg",
                                    type = "image/jpeg",
                                    base64Data = base64
                                )
                            )
                            
                            viewModel.onInputTextChanged(currentText)
                            viewModel.sendMessage()
                            image.close()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            android.util.Log.e("DaveVision", "Capture failed", exception)
                            viewModel.onInputTextChanged(currentText)
                            viewModel.sendMessage()
                        }
                    }
                )
            } else {
                viewModel.onInputTextChanged(currentText)
                viewModel.sendMessage()
            }
        }
    }

    // Main orchestration loop
    LaunchedEffect(isListening, uiState.isLoading, isMuted, micPermissionState.status.isGranted) {
        if (micPermissionState.status.isGranted && !isMuted) {
            if (!isListening && !uiState.isLoading) {
                delay(300) // Brief pause to prevent rapid error looping
                voiceManager.startListening()
            }
        } else if (isMuted && isListening) {
            voiceManager.stopListening()
        }
    }

    DisposableEffect(Unit) {
        // Keep screen awake while in Live Mode
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
        // Dave Vision Camera Preview
        if (isVisionEnabled && previewUseCase != null) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }.also {
                        previewUseCase?.surfaceProvider = it.surfaceProvider
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.4f } // Darken/blur it so the UI still pops
            )
        }

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
                rms = rmsLevel,
                mode = uiState.currentMode,
                emotionalArc = uiState.emotionalArc
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

            // User Speech Preview
            Text(
                text = if (isListening) spokenText else "",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp).height(48.dp) // Fixed height to prevent bouncing
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dave Live Transcript
            val lastDaveMessage = uiState.messages.lastOrNull { it.isFromDave }?.content ?: ""
            AnimatedVisibility(
                visible = isDaveSpeaking || uiState.isLoading || (!isListening && lastDaveMessage.isNotBlank()),
                enter = fadeIn() + slideInVertically { 50 },
                exit = fadeOut() + slideOutVertically { 50 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(120.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .background(Color.DarkGray.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(lastDaveMessage) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                    Text(
                        text = lastDaveMessage,
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 24.sp,
                        modifier = Modifier.verticalScroll(scrollState)
                    )
                }
            }
        }

        // Mute Button
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(48.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Vision Toggle Button
            IconButton(
                onClick = { 
                    if (cameraPermissionState.status.isGranted) {
                        isVisionEnabled = !isVisionEnabled
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (isVisionEnabled) MaterialTheme.colorScheme.primary else Color.DarkGray.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = if (isVisionEnabled) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = "Dave Vision",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Mute Button
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
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
}

@Composable
fun SiriWaveAnimation(
    isListening: Boolean,
    isThinking: Boolean,
    isSpeaking: Boolean,
    rms: Float,
    mode: DaveMode = DaveMode.EXPLORER,
    emotionalArc: String = ""
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
    
    // Wave colors based on state AND mode AND emotional arc
    val arcColor = when {
        emotionalArc.lowercase().contains("trust") -> Color(0xFF64B5F6) // Soft Blue
        emotionalArc.lowercase().contains("tension") -> Color(0xFFFF7043) // Deep Orange
        emotionalArc.lowercase().contains("excited") -> Color(0xFFFFEE58) // Bright Yellow
        else -> null
    }

    val speakingBaseColor = arcColor ?: when (mode) {
        DaveMode.HACKER -> Color(0xFF00E676) // Matrix Green
        DaveMode.CREATIVE -> Color(0xFFC0CA33) // Lime Gold
        DaveMode.ANALYST -> Color(0xFFFFD600) // Gold
        DaveMode.GAMER -> Color(0xFFF44336) // Red
        DaveMode.RESEARCHER -> Color(0xFF00C853) // Emerald
        DaveMode.VISIONARY -> Color(0xFF64FFDA) // Teal
        DaveMode.SOCIOLOGIST -> Color(0xFFE040FB) // Purple
        DaveMode.APP_FACTORY -> Color(0xFF2979FF) // Blue
        DaveMode.POET -> Color(0xFFF06292) // Pink
        else -> Color(0xFF00E676) // Default Emerald
    }

    val color1 by animateColorAsState(targetValue = if (isSpeaking) speakingBaseColor else if (isThinking) Color(0xFFFFD600) else if (isListening) Color(0xFF00E676) else Color.DarkGray, label = "c1")
    val color2 by animateColorAsState(targetValue = if (isSpeaking) speakingBaseColor.copy(alpha = 0.7f) else if (isThinking) Color(0xFFF9A825) else if (isListening) Color(0xFF00C853) else Color.Gray, label = "c2")
    val color3 by animateColorAsState(targetValue = if (isSpeaking) speakingBaseColor.copy(alpha = 0.4f) else if (isThinking) Color(0xFFFFEB3B) else if (isListening) Color(0xFF69F0AE) else Color.LightGray, label = "c3")

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
                    path.moveTo(0f, y)
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
