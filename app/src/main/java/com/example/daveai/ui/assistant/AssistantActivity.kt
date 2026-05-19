package com.example.daveai.ui.assistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daveai.DaveApplication
import com.example.daveai.MainActivity
import com.example.daveai.ui.theme.DaveAITheme
import com.example.daveai.util.VoiceToTextManager

class AssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val chatRepository = (application as DaveApplication).chatRepository

        setContent {
            DaveAITheme {
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp)
                    .clickable(enabled = false) {}, // Prevent dismiss when clicking the sheet
                shape = RoundedCornerShape(28.dp), // Like Gemini's bottom sheet
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp).copy(alpha = 0.95f),
                shadowElevation = 16.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
                        .padding(24.dp),
                ) {
                    // Header / Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dave Sparkle Icon
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
                        
                        // Close / Expand Buttons
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

                    Spacer(Modifier.height(24.dp))

                    // Main Content Area (User query / Dave Response)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp, max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (uiState.isThinking) {
                            // Thinking indicator
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Analyzing request...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                fontSize = if (uiState.daveResponse != null) 16.sp else 20.sp, // Larger font for input, standard for response
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Bottom Bar (Mic / Re-ask)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // The Pixel Assistant glowing edge underneath the mic
                        val infiniteTransition = rememberInfiniteTransition(label = "glowTransition")
                        val glowAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "glowAlpha"
                        )

                        // If response exists, we show a "re-ask" button. Otherwise, we show the active listening mic.
                        if (uiState.daveResponse != null) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                onClick = {
                                    viewModel.reset()
                                    voiceManager.startListening()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Mic, 
                                        contentDescription = "Ask Another",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Ask something else",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Background Glow matching Gemini
                                Box(
                                    modifier = Modifier
                                        .size(if (isListening) 80.dp else 64.dp)
                                        .clip(CircleShape)
                                        .graphicsLayer {
                                            alpha = if (isListening) glowAlpha else 0.3f
                                        }
                                        .background(
                                            Brush.sweepGradient(
                                                colors = listOf(
                                                    Color(0xFF4285F4), // Google Blue
                                                    Color(0xFFEA4335), // Google Red
                                                    Color(0xFFFBBC05), // Google Yellow
                                                    Color(0xFF34A853), // Google Green
                                                    Color(0xFF4285F4)  // Google Blue (wrap around)
                                                )
                                            )
                                        )
                                )

                                AssistantMicButton(isListening = isListening) {
                                    if (isListening) voiceManager.stopListening()
                                    else voiceManager.startListening()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssistantMicButton(isListening: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                if (isListening) {
                    scaleX = scale
                    scaleY = scale
                }
            }
            .padding(2.dp),
        shape = CircleShape,
        color = if (isListening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = null,
                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
