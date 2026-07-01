package com.example.daveai.ui.landing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.components.DynamicAura
import com.example.daveai.ui.components.GlassSidebar
import com.example.daveai.ui.components.NeuralTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    viewModel: ChatViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToRiddle: () -> Unit,
    onEnterVault: () -> Unit,
    onEnterSanctum: () -> Unit,
    onEnterDashboard: () -> Unit,
    onEnterMarketplace: () -> Unit,
    onEnterPersonaEditor: () -> Unit,
    onEnterVision: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current

    var isCoreActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isCoreActive = true
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
                onSessionSelected = { sessionId ->
                    viewModel.selectSession(sessionId)
                    onNavigateToChat()
                    scope.launch { drawerState.close() }
                },
                onCreateNewChat = {
                    viewModel.createNewChat()
                    onNavigateToChat()
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
                    onNavigateToRiddle()
                    scope.launch { drawerState.close() }
                },
                onEnterTerminal = {
                    onEnterDashboard()
                    scope.launch { drawerState.close() }
                },
                onEnterMarketplace = {
                    onEnterMarketplace()
                    scope.launch { drawerState.close() }
                },
                onEnterPersonaEditor = {
                    onEnterPersonaEditor()
                    scope.launch { drawerState.close() }
                },
                onEnterVision = {
                    onEnterVision()
                    scope.launch { drawerState.close() }
                },
                onUpdateGlowStrength = viewModel::updateGlowStrength,
                onUpdateBlurIntensity = viewModel::updateBlurIntensity,
                onModeChange = viewModel::setMode,
                currentMode = uiState.currentMode,
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                NeuralTopBar(
                    title = "DAVE HUB",
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    navigationIcon = Icons.Rounded.Menu
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                // Background Glow
                val glowStrength = com.example.daveai.ui.components.LocalGlowStrength.current
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f * glowStrength * 2f),
                                    Color.Transparent
                                ),
                                radius = 1000f
                            )
                        )
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .height(450.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Orbits
                            OrbitingElement(
                                angleOffset = 0f,
                                label = "VAULT",
                                isActive = isCoreActive,
                                onClick = onNavigateToRiddle,
                                color = MaterialTheme.colorScheme.tertiary,
                                radius = 160.dp
                            )

                            OrbitingElement(
                                angleOffset = 180f,
                                label = "SYSTEM",
                                isActive = isCoreActive,
                                onClick = onNavigateToChat,
                                color = MaterialTheme.colorScheme.primary,
                                radius = 160.dp
                            )

                            // Central Aura Core
                            DynamicAura(
                                isSpeaking = uiState.isSpeaking,
                                intensity = uiState.cyberIntensity
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickActionsWidget(
                                onToggleFlashlight = { viewModel.toggleFlashlight() },
                                onToggleDnd = { viewModel.toggleDnd() },
                                onOpenTerminal = onEnterDashboard
                            )

                            uiState.dailyPoem?.let { poem ->
                                PoetryWidget(
                                    title = poem.title,
                                    content = poem.content,
                                    author = poem.author
                                )
                            }

                            SystemStatsWidget(
                                cpuUsage = 0.42f, // Mock for now
                                ramUsage = 0.68f,
                                batteryLevel = 85
                            )

                            NeuralPulseWidget()

                            NewsBriefingWidget(
                                headlines = listOf(
                                    "Quantum Neural Networks achieved 99% accuracy.",
                                    "Neural Guard v88: AXON_VANGUARD protocol active.",
                                    "Dave OS Kernel upgrade finalized."
                                )
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(32.dp))
                        Text(
                            text = "NEURAL LINK ESTABLISHED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DAVE OS :: BP46.2026.16",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            com.example.daveai.ui.components.BetaBadge()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuraCore(
    isActive: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val glowStrength = com.example.daveai.ui.components.LocalGlowStrength.current
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(1000),
        label = "alpha"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "pressScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(pressScale)
            .alpha(coreAlpha)
    ) {
        // Outer Glow Layers
        repeat(3) { index ->
            val glowScale by infiniteTransition.animateFloat(
                initialValue = 1.2f + (index * 0.2f),
                targetValue = 1.6f + (index * 0.3f),
                animationSpec = infiniteRepeatable(
                    animation = tween(4000 + (index * 1000), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glowScale$index"
            )
            
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(glowScale)
                    .graphicsLayer { alpha = (0.1f * glowStrength * 1.5f) / (index + 1) } // Softened glow
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, Color.Transparent)
                        )
                    )
            )
        }

        // Main Interaction Surface
        Surface(
            modifier = Modifier
                .size(160.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            color = Color.Transparent,
            shape = CircleShape
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.92f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                )
                
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = "Initialize Dave",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OrbitingElement(
    angleOffset: Float,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    color: Color,
    radius: androidx.compose.ui.unit.Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbit")
    val orbitDuration = 12000
    
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(orbitDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val currentAngle = (angle + angleOffset) * (Math.PI / 180f).toFloat()
    
    val xOffset = radius * cos(currentAngle)
    val yOffset = radius * sin(currentAngle)

    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(tween(1500)) + scaleIn(tween(1000)),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .offset(x = xOffset, y = yOffset)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                        .graphicsLayer {
                            shadowElevation = 10f
                            spotShadowColor = color
                        }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
