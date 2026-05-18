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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
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
import androidx.compose.ui.text.style.TextOverflow
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
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
            ) + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .padding(horizontal = 12.dp)
                    .clickable(enabled = false) { } 
            ) {
                // Dock Style Pill
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .animateContentSize()
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.95f),
                    shadowElevation = 12.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dave Energy Indicator
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isThinking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text("⚡️", style = MaterialTheme.typography.titleLarge)
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            // Interaction Content
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        isListening -> "Dave is listening..."
                                        uiState.isThinking -> "Dave is thinking..."
                                        uiState.daveResponse != null -> "Dave AI"
                                        else -> "Elite AI Partner"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = when {
                                        uiState.daveResponse != null -> uiState.daveResponse!!
                                        spokenText.isNotBlank() -> spokenText
                                        else -> "How can I help, boss?"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = if (uiState.daveResponse != null) 5 else 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (uiState.daveResponse == null) {
                                // Pulse Mic Button
                                AssistantMicButton(isListening = isListening) {
                                    if (isListening) voiceManager.stopListening()
                                    else voiceManager.startListening()
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        viewModel.reset()
                                        voiceManager.startListening()
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.Mic, 
                                        contentDescription = "Ask Another",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(onClick = onNavigateToChat) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = "Open Full Chat",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Spacer(Modifier.width(4.dp))

                            IconButton(
                                onClick = { 
                                    visible = false
                                    onDismiss() 
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.Close, 
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
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
