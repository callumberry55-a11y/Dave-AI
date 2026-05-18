package com.example.daveai.ui.riddle

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.rounded.Bolt
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
import androidx.compose.ui.draw.clip
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
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val gold = Color(0xFFFFB300)
    val darkBg = Color(0xFF121212)
    val cardBg = Color(0xFF1E1E1E)

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
        containerColor = darkBg,
        topBar = {
            TopAppBar(
                title = { Text("The Riddle Vault", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBg
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TIER: ${uiState.tierName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = gold,
                    fontWeight = FontWeight.Bold,
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
                    color = Color(0xFF888888),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Riddle Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset { IntOffset(animatedOffsetX.roundToPx(), 0) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                Icons.Rounded.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = gold
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                uiState.currentRiddle!!.question,
                                color = Color.White,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                            
                            AnimatedVisibility(
                                visible = uiState.showHint,
                                enter = fadeIn() + slideInVertically { 20 }
                            ) {
                                Surface(
                                    modifier = Modifier.padding(top = 24.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF2C2C2C)
                                ) {
                                    Text(
                                        uiState.currentRiddle!!.hint,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = gold,
                                        textAlign = TextAlign.Center
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
                            placeholder = { Text("Type your guess here...", color = Color(0xFF666666)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = gold,
                                unfocusedIndicatorColor = Color(0xFF333333)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(Modifier.width(8.dp))

                        val micColor by animateColorAsState(if (isListening) Color.Red else gold, label = "micColor")
                        val micScale by animateFloatAsState(if (isListening) 1.2f else 1f, label = "micScale")

                        Surface(
                            modifier = Modifier
                                .size(50.dp)
                                .scale(micScale)
                                .clip(CircleShape)
                                .background(micColor),
                            color = micColor,
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
                                    tint = if (isListening) Color.White else darkBg
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    BouncyButton(
                        onClick = { viewModel.submitAnswer() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        containerColor = gold,
                        contentColor = darkBg
                    ) {
                        Text("Unlock", fontWeight = FontWeight.Bold)
                    }
                } else if (uiState.isSolved) {
                    BouncyButton(
                        onClick = { viewModel.loadNextRiddle() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Next Riddle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
