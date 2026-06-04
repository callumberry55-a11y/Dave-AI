package com.example.daveai.ui.riddle

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.daveai.ui.components.GlassSidebar
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralTextField
import com.example.daveai.ui.components.NeuralTopBar
import com.example.daveai.util.VoiceToTextManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RiddleTierBackground(tier: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "tier_anim")
    val color = when(tier) {
        "CASUAL" -> Color(0xFF00FF88)
        "EXPLORER" -> Color(0xFF00A2FF)
        "MASTER" -> Color(0xFF9D00FF)
        "ELITE" -> Color(0xFFFFE600)
        "LEGENDARY" -> Color(0xFF00F2FF)
        else -> MaterialTheme.colorScheme.primary
    }

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.2f))
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                    center = center,
                    radius = canvasWidth * scale
                ),
                radius = canvasWidth,
                center = center
            )
            
            // Add some "digital grain" or particles
            repeat(10) {
                drawCircle(
                    color = color.copy(alpha = alpha * 0.5f),
                    radius = (2..6).random().toFloat(),
                    center = androidx.compose.ui.geometry.Offset(
                        x = (0..canvasWidth.toInt()).random().toFloat(),
                        y = (0..canvasHeight.toInt()).random().toFloat()
                    )
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RiddleScreen(
    viewModel: RiddleViewModel,
    onBack: () -> Unit,
    onEnterVault: () -> Unit = {},
    onEnterSanctum: () -> Unit = {},
    onEnterTerminal: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val blurRadius by animateDpAsState(
        targetValue = if (drawerState.targetValue == DrawerValue.Open) (16 * uiState.blurIntensity).dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "background_blur"
    )

    val gold = MaterialTheme.colorScheme.tertiary
    val accentPurple = MaterialTheme.colorScheme.primary
    val progress = if (uiState.totalCount > 0) uiState.solvedCount.toFloat() / uiState.totalCount else 0f

    var entryVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        entryVisible = true
    }

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GlassSidebar(
                userProfile = uiState.userProfile,
                sessions = uiState.sessions,
                currentSessionId = null,
                glowStrength = uiState.glowStrength,
                blurIntensity = uiState.blurIntensity,
                onSessionSelected = { _ -> 
                    onBack()
                    scope.launch { drawerState.close() }
                },
                onCreateNewChat = {
                    viewModel.createNewChat()
                    onBack()
                    scope.launch { drawerState.close() }
                },
                onEnterVault = {
                    onEnterVault()
                    scope.launch { drawerState.close() }
                },
                onEnterSanctum = {
                    onEnterSanctum()
                    scope.launch { drawerState.close() }
                },
                onEnterRiddleRoom = {
                    scope.launch { drawerState.close() }
                },
                onEnterTerminal = {
                    onEnterTerminal()
                    scope.launch { drawerState.close() }
                },
                onUpdateGlowStrength = viewModel::updateGlowStrength,
                onUpdateBlurIntensity = viewModel::updateBlurIntensity,
                onLogout = {
                    scope.launch { drawerState.close() }
                    viewModel.logout()
                    onLogout()
                }
            )
        },
        gesturesEnabled = true
    ) {
        // Riddle Entrance Animation (Glitch)
    var glitchTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.currentRiddle?.id) {
        if (uiState.currentRiddle != null) {
            glitchTrigger++
        }
    }
    val glitchScale by animateFloatAsState(
        targetValue = if (glitchTrigger > 0) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow),
        label = "glitch_scale"
    )
    val glitchAlpha by animateFloatAsState(
        targetValue = if (glitchTrigger > 0) 1f else 0f,
        animationSpec = tween(400),
        label = "glitch_alpha"
    )

    Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = blurRadius),
            topBar = {
                NeuralTopBar(
                    title = "The Riddle Vault",
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    navigationIcon = Icons.Rounded.Menu,
                    actions = {
                        androidx.compose.material3.IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = gold)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scoreboard Dock
                AnimatedVisibility(
                    visible = entryVisible,
                    enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn()
                ) {
                    NeuralCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                    color = MaterialTheme.colorScheme.onSurface,
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
                            
                            Spacer(Modifier.height(12.dp))
                            
                            // Neural Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .fillMaxHeight()
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                colors = listOf(accentPurple, gold)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Riddle Display Card
                AnimatedVisibility(
                    visible = entryVisible,
                    enter = slideInVertically(initialOffsetY = { 200 }, animationSpec = spring(dampingRatio = 0.6f)) + fadeIn()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Tier-specific Background Graphics
                        RiddleTierBackground(tier = uiState.tierName)
                        
                        NeuralCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp)
                                .offset { IntOffset(animatedOffsetX.roundToPx(), 0) }
                                .graphicsLayer {
                                    scaleX = glitchScale
                                    scaleY = glitchScale
                                    alpha = glitchAlpha
                                    rotationX = (1f - glitchAlpha) * 10f
                                },
                            shape = RoundedCornerShape(24.dp),
                            containerColor = Color.Black.copy(alpha = 0.4f), // More transparent to show background
                            isGodMode = uiState.isSolved
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoading) {
                                    Text("Opening the Vault...", color = MaterialTheme.colorScheme.onSurface)
                                } else if (uiState.currentRiddle == null) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                    val pulseScale by infiniteTransition.animateFloat(
                                        initialValue = 0.95f,
                                        targetValue = 1.05f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "pulse_scale"
                                    )
                                    val pulseAlpha by infiniteTransition.animateFloat(
                                        initialValue = 0.7f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "pulse_alpha"
                                    )
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.scale(pulseScale).graphicsLayer(alpha = pulseAlpha)
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(72.dp), tint = Color(0xFF4CAF50))
                                        Spacer(Modifier.height(16.dp))
                                        Text("VAULT CONQUERED", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 2.sp)
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
                                                animate(
                                                    initialValue = 0f,
                                                    targetValue = 1f,
                                                    animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
                                                ) { value, _ -> textReveal = value }
                                            } else {
                                                textReveal = 0f
                                            }
                                        }

                                        Text(
                                            text = if (uiState.isSolved) uiState.currentRiddle!!.question else {
                                                uiState.currentRiddle!!.question.mapIndexed { _, c -> 
                                                    val seed = remember(uiState.currentRiddle!!.id) { (0..100).random() }
                                                    if (c.isLetter() && seed > 70) '?' else c
                                                }.joinToString("")
                                            },
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (uiState.isSolved) textReveal.coerceAtLeast(0.5f) else 1f),
                                            fontSize = 24.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 36.sp,
                                            fontWeight = FontWeight.Light,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                            modifier = Modifier.graphicsLayer {
                                                if (uiState.isSolved) {
                                                    scaleX = 0.95f + (textReveal * 0.08f)
                                                    scaleY = 0.95f + (textReveal * 0.08f)
                                                    shadowElevation = textReveal * 15f
                                                    ambientShadowColor = gold
                                                    spotShadowColor = gold
                                                }
                                            }
                                        )
                                        
                                        AnimatedVisibility(
                                            visible = uiState.showHint,
                                            enter = fadeIn() + slideInVertically { 20 }
                                        ) {
                                            NeuralCard(
                                                modifier = Modifier.padding(top = 32.dp),
                                                shape = RoundedCornerShape(16.dp),
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
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Input Container
                Column(modifier = Modifier.fillMaxWidth()) {
                    if ((!uiState.isSolved) && (uiState.currentRiddle != null)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleHint() 
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = gold)
                            ) {
                                Text("💡 Request Hint", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.skipRiddle() 
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Text("⏭️ Skip Riddle", fontSize = 12.sp)
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NeuralTextField(
                                value = uiState.inputText,
                                onValueChange = viewModel::onInputChanged,
                                label = "Speak the password...",
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(Modifier.width(12.dp))

                            val micColor by animateColorAsState(if (isListening) Color(0xFFE53935) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), label = "micColor")
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
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.submitAnswer() 
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            containerColor = gold.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ) {
                            Text("Speak Friend and Enter", fontWeight = FontWeight.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontSize = 16.sp)
                        }
                    } else if (uiState.isSolved) {
                        BouncyButton(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.loadNextRiddle() 
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            containerColor = accentPurple.copy(alpha = 0.6f),
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
}
