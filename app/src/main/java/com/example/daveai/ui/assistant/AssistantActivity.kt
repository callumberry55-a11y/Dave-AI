package com.example.daveai.ui.assistant

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daveai.DaveApplication
import com.example.daveai.MainActivity
import com.example.daveai.data.repository.SettingsRepository
import com.example.daveai.ui.components.*
import com.example.daveai.ui.theme.*
import com.example.daveai.util.DaveHapticManager
import com.example.daveai.util.VoiceToTextManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as DaveApplication
        val chatRepository = app.chatRepository
        val settingsRepository = app.settingsRepository
        val hapticManager = DaveHapticManager(this)

        setContent {
            val primaryColorInt by settingsRepository.primaryColor.collectAsState(initial = SettingsRepository.DEFAULT_COLOR)
            val cyberIntensity by settingsRepository.cyberIntensity.collectAsState(initial = 0.8f)
            val primaryColor = Color(primaryColorInt)

            CompositionLocalProvider(LocalCyberIntensity provides cyberIntensity) {
                DaveAITheme(primaryColorOverride = primaryColor) {
                    val assistantViewModel: AssistantViewModel = viewModel {
                        AssistantViewModel(chatRepository)
                    }

                    val onColorPicked: (Int) -> Unit = { pickedColor ->
                        hapticManager.signalSuccess()
                        chatRepository.getScope().launch {
                            settingsRepository.setPrimaryColor(pickedColor)
                        }
                    }
                    
                    AssistantAuraOverlay(
                        viewModel = assistantViewModel,
                        onDismiss = { finish() },
                        onColorPicked = onColorPicked,
                        onNavigateToChat = {
                            val intent = Intent(this@AssistantActivity, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AssistantAuraOverlay(
    viewModel: AssistantViewModel,
    onDismiss: () -> Unit,
    onColorPicked: (Int) -> Unit,
    onNavigateToChat: () -> Unit,
) {
    val context = LocalContext.current
    val hapticManager = remember { DaveHapticManager(context) }
    val voiceManager = remember { VoiceToTextManager(context) }
    val isListening by voiceManager.isListening.collectAsState()
    val spokenText by voiceManager.spokenText.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        voiceManager.startListening()
    }

    // Morphing background pulse
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "scale"
    )

    LaunchedEffect(isListening) {
        if (!isListening && spokenText.isNotBlank() && (!uiState.isThinking) && (uiState.daveResponse == null)) {
            hapticManager.pulseThinking()
            viewModel.onUserSpeechFinished(spokenText)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
            .background(ObsidianDeep.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        // Neural Aura Glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(auraScale)
                .blur(60.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
        )

        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(initialScale = 0.8f) + fadeIn(),
            exit = scaleOut(targetScale = 1.2f) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Status Header
                Text(
                    text = when {
                        isListening -> "Listening"
                        uiState.isThinking -> "Thinking"
                        uiState.daveResponse != null -> "Dave"
                        else -> "Awake"
                    }.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 4.sp
                )
                
                Spacer(Modifier.height(48.dp))

                // Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isThinking) {
                        NeuralPulseIndicator()
                    } else {
                        Text(
                            text = when {
                                uiState.daveResponse != null -> uiState.daveResponse!!
                                spokenText.isNotBlank() -> spokenText
                                else -> "How can I assist?"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = GhostWhite,
                            lineHeight = 34.sp
                        )
                    }
                }

                Spacer(Modifier.height(64.dp))

                // Fluid Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (Build.VERSION.SDK_INT >= 37) {
                            onColorPicked(Color(0xFF00FFCC).toArgb())
                        }
                    }) {
                        Icon(Icons.Rounded.Colorize, contentDescription = null, tint = GhostWhite.copy(alpha = 0.4f))
                    }
                    
                    Spacer(Modifier.width(32.dp))

                    LiquidMicButton(
                        isListening = isListening,
                        onClick = {
                            if (isListening) voiceManager.stopListening()
                            else {
                                viewModel.reset()
                                voiceManager.startListening()
                            }
                        }
                    )

                    Spacer(Modifier.width(32.dp))

                    IconButton(onClick = onNavigateToChat) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = GhostWhite.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidMicButton(
    isListening: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_morph")
    val morph by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "morph"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(organicBlobShape(morph))
            .background(if (isListening) MaterialTheme.colorScheme.primary else GhostWhite.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isListening) Icons.Rounded.Mic else Icons.Rounded.MicNone,
            contentDescription = null,
            tint = if (isListening) ObsidianDeep else GhostWhite,
            modifier = Modifier.size(32.dp)
        )
    }
}
