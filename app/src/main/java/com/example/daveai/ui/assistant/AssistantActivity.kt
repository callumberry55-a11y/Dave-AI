package com.example.daveai.ui.assistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daveai.DaveApplication
import com.example.daveai.MainActivity
import com.example.daveai.data.repository.SettingsRepository
import com.example.daveai.ui.chat.BouncyButton
import com.example.daveai.ui.components.LocalCyberIntensity
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralPulseIndicator
import com.example.daveai.ui.theme.DaveAITheme
import com.example.daveai.util.VoiceToTextManager
import kotlinx.coroutines.delay

class AssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val chatRepository = (application as DaveApplication).chatRepository
        val settingsRepository = SettingsRepository(this)

        // Start Dave's Sanctum Server
        com.example.daveai.service.DaveServerService.start(this)

        setContent {
            val primaryColorInt by settingsRepository.primaryColor.collectAsState(initial = SettingsRepository.DEFAULT_COLOR)
            val cyberIntensity by settingsRepository.cyberIntensity.collectAsState(initial = 0.8f)
            val primaryColor = Color(primaryColorInt)

            CompositionLocalProvider(LocalCyberIntensity provides cyberIntensity) {
                DaveAITheme(primaryColorOverride = primaryColor) {
                    val assistantViewModel: AssistantViewModel = viewModel {
                        AssistantViewModel(chatRepository)
                    }
                    
                    AssistantPillOverlay(
                        viewModel = assistantViewModel,
                        onDismiss = { finish() },
                    ) {
                        val intent = Intent(this@AssistantActivity, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun AssistantPillOverlay(
    viewModel: AssistantViewModel,
    onDismiss: () -> Unit,
    onNavigateToChat: () -> Unit,
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceToTextManager(context) }
    val isListening by voiceManager.isListening.collectAsState()
    val spokenText by voiceManager.spokenText.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var visible by remember { mutableStateOf(value = false) }

    LaunchedEffect(Unit) {
        visible = true
        voiceManager.startListening()
    }

    DisposableEffect(Unit) {
        onDispose { voiceManager.destroy() }
    }

    // Trigger Dave's response when speech finishes
    LaunchedEffect(isListening) {
        if (!isListening && spokenText.isNotBlank() && (!uiState.isThinking) && (uiState.daveResponse == null)) {
            viewModel.onUserSpeechFinished(spokenText)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { 
                visible = false
                onDismiss() 
            }
            .background(Color.Black.copy(alpha = 0.5f)), // Slightly darker scrim
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            ) + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            NeuralCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(32.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
                        .padding(24.dp),
                ) {
                    // Header / Status
                    var headerVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(200)
                        headerVisible = true
                    }
                    
                    AnimatedVisibility(
                        visible = headerVisible,
                        enter = fadeIn() + slideInVertically { -20 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = when {
                                    isListening -> "Listening..."
                                    uiState.isThinking -> "Thinking..."
                                    uiState.daveResponse != null -> "Dave AI"
                                    else -> "Hi, I'm Dave"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(Modifier.weight(1f))
                            
                            if (uiState.daveResponse != null) {
                                IconButton(
                                    onClick = onNavigateToChat,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = "Open Full Chat",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                            
                            IconButton(
                                onClick = { 
                                    visible = false
                                    onDismiss() 
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Close, 
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Main Content Area
                    var contentVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(350)
                        contentVisible = true
                    }

                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn() + slideInVertically { 20 }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 400.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (uiState.isThinking) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    NeuralPulseIndicator()
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "Analyzing...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = when {
                                        uiState.daveResponse != null -> uiState.daveResponse!!
                                        spokenText.isNotBlank() -> spokenText
                                        else -> "How can I help you today?"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = if (uiState.daveResponse != null) 16.sp else 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 28.sp,
                                    fontWeight = if (uiState.daveResponse == null) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Bottom Bar (Mic / Re-ask)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.daveResponse != null) {
                            BouncyButton(
                                onClick = {
                                    viewModel.reset()
                                    voiceManager.startListening()
                                },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(Icons.Rounded.Mic, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Ask Another", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            NeuralAssistantMic(
                                isListening = isListening,
                                onClick = {
                                    if (isListening) voiceManager.stopListening()
                                    else voiceManager.startListening()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NeuralAssistantMic(
    isListening: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neural_mic")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        // Outer Rotating Ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer { rotationZ = rotation }
                .drawBehind {
                    if (isListening) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF00E676), // Green
                                    Color(0xFF2979FF), // Blue
                                    Color(0xFFD500F9), // Purple
                                    Color(0xFF00E676)
                                )
                            ),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }
        )

        // Pulsing Waves
        if (isListening) {
            repeat(2) { i ->
                val delay = i * 600
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, delayMillis = delay, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "wave$i"
                )
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, delayMillis = delay, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "alpha$i"
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(scale)
                        .graphicsLayer { this.alpha = alpha }
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                )
            }
        }

        // Inner Mic Button
        Surface(
            modifier = Modifier
                .size(64.dp)
                .scale(if (isListening) waveScale.coerceAtMost(1.1f) else 1f),
            shape = CircleShape,
            color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = 8.dp,
            onClick = onClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Mic",
                    modifier = Modifier.size(28.dp),
                    tint = if (isListening) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
