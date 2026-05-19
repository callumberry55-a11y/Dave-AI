package com.example.daveai.ui.riddle

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.chat.BouncyButton
import com.example.daveai.util.VoiceToTextManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RiddleScreen(
    viewModel: RiddleViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val gold = Color(0xFFFFD700)
    val darkBg = Color(0xFF0A0214) // Deep, deep purple/black
    val cardBg = Color(0xFF1E0B36) // Dark mysterious purple
    val accentPurple = Color(0xFF4A148C)

    var offsetX by remember { mutableStateOf(0.dp) }
    val animatedOffsetX by animateDpAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = 0.2f, stiffness = 1500f),
        label = "shake"
    )

    LaunchedEffect(uiState.errorTrigger) {
        if (uiState.errorTrigger > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            offsetX = 10.dp
            delay(50)
            offsetX = 0.dp
        }
    }

    LaunchedEffect(uiState.isSolved) {
        if (uiState.isSolved) {
            focusManager.clearFocus()
        }
    }

    val voiceToTextManager = remember { VoiceToTextManager(context) }
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val isListening by voiceToTextManager.isListening.collectAsState()
    val spokenText by voiceToTextManager.spokenText.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.currentRiddle == null) {
            viewModel.loadNextRiddle()
        }
    }

    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank()) {
            viewModel.onInputChanged(spokenText)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceToTextManager.destroy()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(darkBg, Color.Black)
            )
        ),
        topBar = {
            TopAppBar(
                title = { Text("The Riddle Vault", fontWeight = FontWeight.Black, color = gold, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scoreboard Dock
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = cardBg.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentPurple)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "TIER: ${uiState.tierName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = gold,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "STREAK: 🔥 ${uiState.streak}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${uiState.solvedCount} / ${uiState.totalCount}",
                        style = MaterialTheme.typography.labelLarge,
                        color = gold.copy(alpha = 0.7f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Riddle Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset { IntOffset(animatedOffsetX.roundToPx(), 0) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, gold.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        Text("Opening the Vault...", color = Color.White)
                    } else if (uiState.currentRiddle == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFF4CAF50))
                            Spacer(Modifier.height(16.dp))
                            Text("VAULT CONQUERED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    } else {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = gold
                            )
                            Spacer(Modifier.height(24.dp))
                            // Decrypting Text Animation
                            var textReveal by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
                            LaunchedEffect(uiState.isSolved) {
                                if (uiState.isSolved) {
                                    androidx.compose.animation.core.animate(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                                    ) { value, _ -> textReveal = value }
                                } else {
                                    textReveal = 0f
                                }
                            }

                            Text(
                                if (uiState.isSolved) uiState.currentRiddle!!.question else uiState.currentRiddle!!.question.map { c -> 
                                    if (c.isLetter() && (Math.random() > (1 - textReveal))) '*' else c
                                }.joinToString(""),
                                color = Color.White.copy(alpha = if (uiState.isSolved) 0.5f else 1f),
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                            )
                            
                            AnimatedVisibility(
                                visible = uiState.showHint,
                                enter = fadeIn() + slideInVertically { 20 }
                            ) {
                                Surface(
                                    modifier = Modifier.padding(top = 32.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = darkBg.copy(alpha = 0.8f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, gold.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        "\"${uiState.currentRiddle!!.hint}\"",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                        color = gold.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Input Container
            Column(modifier = Modifier.fillMaxWidth()) {
                if ((!uiState.isSolved) && (uiState.currentRiddle != null)) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TextButton(
                            onClick = { viewModel.toggleHint() },
                            colors = ButtonDefaults.textButtonColors(contentColor = gold)
                        ) {
                            Text("💡 Request Hint", fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = viewModel::onInputChanged,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Speak the password...", color = gold.copy(alpha = 0.4f), fontFamily = androidx.compose.ui.text.font.FontFamily.Serif) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = cardBg.copy(alpha = 0.5f),
                                unfocusedContainerColor = cardBg.copy(alpha = 0.5f),
                                focusedTextColor = gold,
                                unfocusedTextColor = gold,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        
                        Spacer(Modifier.width(12.dp))

                        val micColor by animateColorAsState(if (isListening) Color(0xFFE53935) else cardBg, label = "micColor")
                        val micIconTint = if (isListening) Color.White else gold
                        val micScale by animateFloatAsState(if (isListening) 1.2f else 1f, label = "micScale")

                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .scale(micScale),
                            shape = CircleShape,
                            color = micColor,
                            border = androidx.compose.foundation.BorderStroke(2.dp, if (isListening) Color(0xFFE53935) else gold.copy(alpha = 0.5f)),
                            onClick = {
                                if (micPermissionState.status.isGranted) {
                                    if (isListening) {
                                        voiceToTextManager.stopListening()
                                    } else {
                                        voiceToTextManager.startListening()
                                    }
                                } else {
                                    micPermissionState.launchPermissionRequest()
                                }
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isListening) Icons.Rounded.Mic else Icons.Rounded.MicNone,
                                    contentDescription = "Mic",
                                    tint = micIconTint
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    BouncyButton(
                        onClick = { viewModel.submitAnswer() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        containerColor = gold,
                        contentColor = darkBg
                    ) {
                        Text("Speak Friend and Enter", fontWeight = FontWeight.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontSize = 16.sp)
                    }
                } else if (uiState.isSolved) {
                    BouncyButton(
                        onClick = { viewModel.loadNextRiddle() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        containerColor = accentPurple,
                        contentColor = gold
                    ) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Proceed Deeper", fontWeight = FontWeight.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
