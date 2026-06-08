package com.example.daveai.ui.chat

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.daveai.ui.components.GlassButton
import com.example.daveai.ui.components.GlassSidebar
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralThinkingIndicator
import com.example.daveai.ui.components.NeuralTopBar
import com.example.daveai.ui.components.StructuredContent
import com.example.daveai.ui.theme.DaveAITheme
import com.example.daveai.ui.theme.DaveBlue
import com.example.daveai.ui.theme.DaveGreen
import com.example.daveai.ui.theme.DavePurple
import com.example.daveai.util.VoiceToTextManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@androidx.media3.common.util.UnstableApi
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onLogout: () -> Unit,
    onEnterRiddleRoom: () -> Unit = {},
    onEnterLiveMode: () -> Unit = {},
    onEnterTerminal: () -> Unit = {},
    onEnterSanctum: () -> Unit = {},
    onEnterVault: () -> Unit = {},
    onEnterMarketplace: () -> Unit = {},
    onEnterPersonaEditor: () -> Unit = {},
    onBackToHub: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val thinkingStatus by viewModel.thinkingStatus.collectAsState()
    val listState = rememberLazyListState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isDockCollapsed by remember { mutableStateOf(false) }
    var isNeuralLinkDialogOpen by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val blurRadius by animateDpAsState(
        targetValue = if (drawerState.targetValue == DrawerValue.Open) (16 * uiState.blurIntensity).dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "background_blur"
    )

    val voiceToTextManager = remember { VoiceToTextManager(context) }
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    val locationPermissionState = com.google.accompanist.permissions.rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
    )

    val locationHelper = remember { com.example.daveai.util.LocationHelper(context) }

    val isListening by voiceToTextManager.isListening.collectAsState()
    val spokenText by voiceToTextManager.spokenText.collectAsState()

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            val location = locationHelper.getCurrentLocationName()
            viewModel.updateLocation(location)
        }
    }

    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank()) {
            viewModel.onInputTextChanged(spokenText)
        }
    }

    LaunchedEffect(isListening) {
        viewModel.setIsListening(isListening)
    }

    DisposableEffect(Unit) {
        onDispose { 
            voiceToTextManager.destroy() 
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            getAttachedFileFromUri(context, it)?.let { file ->
                viewModel.addAttachment(file)
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            confirmButton = {
                GlassButton(
                    onClick = {
                        viewModel.deleteCurrentSession()
                        showDeleteConfirmation = false
                    },
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onError,
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Keep it")
                }
            },
            title = { Text("Clear Conversation?", fontWeight = FontWeight.Bold) },
            text = { Text("This will wipe Dave's memory of this chat.") },
            shape = RoundedCornerShape(28.dp),
        )
    }

    if (uiState.isVaultOpen) {
        MemoryVaultSheet(
            memories = uiState.semanticMemories,
            onDismiss = { viewModel.toggleVault(open = false) },
            onAddEntry = viewModel::addSemanticMemory,
            onDeleteEntry = viewModel::deleteSemanticMemory,
            onStrengthenEntry = viewModel::strengthenSemanticMemory,
            onArchiveEntry = viewModel::archiveSemanticMemory,
            onEditEntry = viewModel::editSemanticMemory,
            onToggleLock = viewModel::toggleMemoryLock
        )
    }

    if (isNeuralLinkDialogOpen) {
        com.example.daveai.ui.components.NeuralLinkDialog(
            pairingCode = uiState.pairingCode,
            onDismiss = { isNeuralLinkDialogOpen = false },
            onRequestCode = viewModel::generatePairingCode,
            onLinkPartner = viewModel::linkPartner
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GlassSidebar(
                userProfile = uiState.userProfile,
                sessions = uiState.sessions,
                currentSessionId = uiState.currentSessionId,
                glowStrength = uiState.glowStrength,
                blurIntensity = uiState.blurIntensity,
                onSessionSelected = { sessionId ->
                    viewModel.selectSession(sessionId)
                    scope.launch { drawerState.close() }
                },
                onCreateNewChat = {
                    viewModel.createNewChat()
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
                    onEnterRiddleRoom()
                    scope.launch { drawerState.close() }
                },
                onEnterTerminal = {
                    onEnterTerminal()
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
                onUpdateGlowStrength = viewModel::updateGlowStrength,
                onUpdateBlurIntensity = viewModel::updateBlurIntensity,
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = blurRadius),
            topBar = {
                NeuralTopBar(
                    title = "Dave AI",
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    navigationIcon = Icons.Rounded.Menu,
                    isProactive = true,
                    actions = {
                        if (uiState.userProfile?.role == "Master Developer") {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    "ARCHITECT",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        BouncyIconButton(icon = Icons.Rounded.Delete) { showDeleteConfirmation = true }
                    }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.messages.isEmpty() && !uiState.isLoading) {
                            item {
                                DaveWelcomeCard(
                                    suggestions = uiState.dynamicSuggestions,
                                ) { suggestion ->
                                    val cleanSuggestion = suggestion.substringAfter(" ").trim()
                                    viewModel.onInputTextChanged(cleanSuggestion)
                                    viewModel.sendMessage(muteVoice = true)
                                }
                            }
                        }
                        itemsIndexed(
                            items = uiState.messages,
                            key = { index, _ -> index }
                        ) { index, message ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically { it / 2 }
                            ) {
                                MessageBubble(message, viewModel)
                            }
                        }
                        if (uiState.isLoading) {
                            item {
                                DaveIsTypingIndicator(thinkingStatus)
                            }
                        }
                    }

                    ChatInputContainer(
                        uiState = uiState,
                        viewModel = viewModel,
                        filePickerLauncher = filePickerLauncher,
                        voiceToTextManager = voiceToTextManager,
                        micPermissionState = micPermissionState,
                        isDockCollapsed = isDockCollapsed,
                        onCollapseChange = { isDockCollapsed = it },
                        haptic = haptic
                    )
                }

                if (uiState.isBuildingApp || uiState.isShowingPreview) {
                    AppFactoryOverlay(
                        progress = uiState.buildProgress,
                        logs = uiState.buildLogs,
                        blueprint = uiState.appBlueprint,
                        isShowingPreview = uiState.isShowingPreview,
                        onDismiss = viewModel::closeAppFactory
                    )
                }
            }
        }
    }
}


@Composable
fun AppFactoryOverlay(
    progress: Float, 
    logs: List<String>, 
    blueprint: List<com.example.daveai.util.BlueprintItem>,
    isShowingPreview: Boolean,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { if (isShowingPreview) onDismiss() }
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
                .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Terminal, contentDescription = null, tint = Color(0xFF00E676))
                    Spacer(Modifier.width(12.dp))
                    Text("DAVE_OS :: APP_FACTORY", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                if (isShowingPreview) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))

            if (isShowingPreview) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.Terminal, contentDescription = null, tint = Color.Black, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("SYSTEM PREVIEW", color = Color.Black, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                    Text("Architected by Dave AI", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Color(0xFF00E676),
                    trackColor = Color.DarkGray
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (blueprint.isNotEmpty()) {
                Text("SYSTEM BLUEPRINT:", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(blueprint) { item ->
                        Surface(
                            color = Color.DarkGray,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(item.type.uppercase(), color = Color(0xFF00E676), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text(item.name, color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.height(if (isShowingPreview) 100.dp else 200.dp).fillMaxWidth()
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        color = if (log.startsWith("SUCCESS")) Color(0xFF00E676) else Color.LightGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            
            if (isShowingPreview) {
                Spacer(Modifier.height(16.dp))
                GlassButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF00E676).copy(alpha = 0.2f),
                    contentColor = Color(0xFF00E676)
                ) {
                    Text("DOWNLOAD PROJECT (.ZIP)")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@androidx.media3.common.util.UnstableApi
@Composable
fun MessageBubble(
    message: ChatMessage,
    viewModel: ChatViewModel
) {
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val alignment = if (message.isFromDave) Alignment.Start else Alignment.End
    val isDark = isSystemInDarkTheme()

    var entranceTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entranceTrigger = true }
    
    val entranceScale by animateFloatAsState(
        targetValue = if (entranceTrigger) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "msg_scale"
    )
    val entranceAlpha by animateFloatAsState(
        targetValue = if (entranceTrigger) 1f else 0f,
        animationSpec = tween(500),
        label = "msg_alpha"
    )
    
    val accentColor = if (message.isFromDave) {
        when (message.mood) {
            "EMPATHETIC" -> DavePurple
            "HYPED" -> DaveGreen
            "URGENT" -> Color(0xFFE53935)
            "CALM" -> DaveBlue
            else -> MaterialTheme.colorScheme.primary
        }
    } else {
        MaterialTheme.colorScheme.secondary
    }
    
    val contentColor = if (message.isFromDave) {
        if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    } else {
        if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .graphicsLayer {
                scaleX = entranceScale
                scaleY = entranceScale
                alpha = entranceAlpha
                translationY = (1f - entranceAlpha) * 20f
            },
        horizontalAlignment = alignment
    ) {
        // Organic Glow Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            if (message.isFromDave) {
                Box(modifier = Modifier.size(6.dp).background(accentColor, CircleShape).graphicsLayer { alpha = 0.8f })
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = if (message.isFromDave) "DAVE :: CORE" else "USER :: ELITE",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            if (!message.isFromDave) {
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.size(6.dp).background(accentColor, CircleShape).graphicsLayer { alpha = 0.8f })
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (message.isFromDave) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (message.isFromDave) Arrangement.Start else Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (message.isFromDave) {
                    TTSPlaybackButton(message, viewModel, accentColor)
                    Spacer(Modifier.width(16.dp)) // More space
                }

                NeuralCard(
                    modifier = Modifier.widthIn(max = 280.dp),
                    containerColor = if (message.isFromDave) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) 
                    else 
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                    shape = if (message.isFromDave) 
                        RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
                    else 
                        RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        if (message.hasAttachment) {
                            AttachmentIndicator(accentColor, message.isFromDave)
                        }

                        if (message.content.contains("```")) {
                            CodeBlockRenderer(message.content, contentColor)
                        } else {
                            StructuredContent(text = message.content, contentColor = contentColor)
                        }

                        if ((message.widgetType != WidgetType.NONE) && (message.widgetData != null)) {
                            Spacer(Modifier.height(14.dp))
                            DaveWidget(type = message.widgetType, data = message.widgetData)
                        }

                        if (message.mediaUrl != null) {
                            Spacer(Modifier.height(12.dp))
                            MessageMedia(
                                url = message.mediaUrl,
                                type = message.mediaType,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        }
                        
                        // Message Actions
                        Row(
                            modifier = Modifier.padding(top = 14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Dave", message.content))) }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = accentColor.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TTSPlaybackButton(
    message: ChatMessage,
    viewModel: ChatViewModel,
    accentColor: Color
) {
    val uiState by viewModel.uiState.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "tts_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(if (uiState.isSpeaking) pulseScale else 1f)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(accentColor.copy(alpha = if (uiState.isSpeaking) 0.8f else 0.12f), Color.Transparent)
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape)
            .clickable {
                if (uiState.isSpeaking) viewModel.stopSpeaking()
                else viewModel.speak(message.content)
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (uiState.isSpeaking) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
            contentDescription = null,
            tint = if (uiState.isSpeaking) Color.White else accentColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun AttachmentIndicator(accentColor: Color, isDave: Boolean) {
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isDave) Icons.Rounded.CheckCircle else Icons.Rounded.AttachFile, 
            contentDescription = null, 
            modifier = Modifier.size(14.dp),
            tint = accentColor.copy(alpha = 0.9f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isDave) "SIGNAL ANALYZED" else "UPLOADING...",
            style = MaterialTheme.typography.labelSmall,
            color = accentColor.copy(alpha = 0.9f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CodeBlockRenderer(content: String, contentColor: Color) {
    val parts = content.split("```")
    parts.forEachIndexed { index, part ->
        if ((index % 2) == 1) {
            val language = part.substringBefore("\n").trim()
            val code = part.substringAfter("\n").trim()
            EliteCodeTerminal(language = language, code = code)
        } else if (part.isNotBlank()) {
            StructuredContent(text = part.trim(), contentColor = contentColor)
        }
    }
}

@Composable
fun DaveIsTypingIndicator(status: String = "") {
    val statuses = listOf("PARSING_INTENT", "SYNCING_VAULT", "ANALYZING_CONTEXT", "SYNTHESIZING_RESPONSE", "OPTIMIZING_AURA", "QUERYING_MAINFRAME")
    var currentStatusIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            currentStatusIndex = (currentStatusIndex + 1) % statuses.size
        }
    }
    val displayStatus = if (status.isNotEmpty()) status else "[ ${statuses[currentStatusIndex]} ]"

    Row(
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeuralThinkingIndicator(modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "DAVE IS THINKING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Crossfade(targetState = displayStatus, label = "status_fade") { statusText ->
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun getAttachedFileFromUri(context: Context, uri: Uri): AttachedFile? {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
    var fileName = "unknown"
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) fileName = cursor.getString(nameIndex)
        }
    }
    return try {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        val base64 = bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        AttachedFile(uri, fileName, mimeType, base64)
    } catch (_: Exception) { null }
}

@Composable
fun EliteCodeTerminal(language: String, code: String) {
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D2D))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Terminal, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = language.ifEmpty { "CODE" }.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.LightGray, fontWeight = FontWeight.Bold)
            }
            Row {
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Dave Code", code))) }
                }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy Code", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                }
            }
        }
        Text(text = code, modifier = Modifier.padding(12.dp), color = Color(0xFFD4D4D4), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
    }
}

@Composable
fun DaveWidget(type: WidgetType, data: String) {
    val json = try { JSONObject(data) } catch (_: Exception) { null } ?: return
    when (type) {
        WidgetType.MAP -> MapWidget(json)
        WidgetType.HARDWARE -> HardwareWidget(json)
        WidgetType.FINANCE -> com.example.daveai.ui.chat.FinanceWidget(json)
        WidgetType.FITNESS -> com.example.daveai.ui.chat.FitnessWidget(json)
        WidgetType.SPOTIFY -> com.example.daveai.ui.chat.SpotifyWidget(json)
        WidgetType.NEWS -> com.example.daveai.ui.chat.NewsWidget(json)
        WidgetType.CALENDAR -> com.example.daveai.ui.chat.CalendarWidget(json)
        WidgetType.USAGE -> com.example.daveai.ui.chat.UsageWidget(json)
        else -> {}
    }
}

@Composable
fun MapWidget(data: JSONObject) {
    val places = data.optJSONArray("places") ?: return
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)).padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Dave's Map Intelligence", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        for (i in 0 until minOf(places.length(), 2)) {
            val place = places.getJSONObject(i)
            ListItem(
                headlineContent = { Text(place.optString("name"), fontWeight = FontWeight.Bold) },
                supportingContent = { Text(place.optString("address"), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
fun HardwareWidget(data: JSONObject) {
    val type = data.optString("type")
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)).padding(16.dp)
    ) {
        if (type == "battery") {
            val level = data.optInt("value")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Energy Core", style = MaterialTheme.typography.labelSmall)
                    Text("$level% Charge", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                }
            }
        } else {
            val isTensor = data.optBoolean("isTensor")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text("Specs Scanned", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text("CHIP: ${if (isTensor) "Google Tensor G4 (TPU)" else "Standard ARM"}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryVaultSheet(
    memories: List<com.example.daveai.data.db.SemanticMemory>,
    onDismiss: () -> Unit,
    onAddEntry: (String, String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onStrengthenEntry: (Long) -> Unit,
    onArchiveEntry: (Long, Boolean) -> Unit,
    onEditEntry: (Long, String) -> Unit,
    onToggleLock: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var editingText by remember { mutableStateOf("") }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = listOf("ALL", "BIO", "PROJECT", "PREFERENCE", "KNOWLEDGE")

    val filteredMemories = memories.filter { 
        (selectedCategory == "ALL" || it.memoryType == selectedCategory) &&
        (it.content.contains(searchQuery, true) || it.memoryType.contains(searchQuery, true))
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp, start = 16.dp, end = 16.dp)) {
            Text("Neural Memory Vault", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text("Permanent data shards stored locally.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(Modifier.height(16.dp))
            
            // Search and Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search memories...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    AssistChip(
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            labelColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ),
                        border = if (isSelected) {
                            AssistChipDefaults.assistChipBorder(enabled = true, borderColor = MaterialTheme.colorScheme.primary, borderWidth = 1.dp)
                        } else {
                            AssistChipDefaults.assistChipBorder(enabled = true, borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), borderWidth = 1.dp)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                if (filteredMemories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No matching data shards found.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }

                filteredMemories.forEach { memory ->
                    ListItem(
                        headlineContent = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(memory.memoryType.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("INTEL: ${memory.importance}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                if (memory.isLocked) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        },
                        supportingContent = { 
                            Column {
                                if (editingId == memory.id) {
                                    OutlinedTextField(
                                        value = editingText, 
                                        onValueChange = { editingText = it }, 
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                                        trailingIcon = { 
                                            IconButton(onClick = { onEditEntry(memory.id, editingText); editingId = null }) { 
                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) 
                                            } 
                                        }
                                    )
                                } else {
                                    Text(memory.content, style = MaterialTheme.typography.bodyLarge) 
                                }
                                Text(
                                    "Accessed ${memory.accessCount} times", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        },
                        trailingContent = { 
                            Row {
                                IconButton(onClick = { onToggleLock(memory.id) }) { 
                                    Icon(
                                        if (memory.isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen, 
                                        contentDescription = "Toggle Lock",
                                        tint = if (memory.isLocked) MaterialTheme.colorScheme.tertiary else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    ) 
                                }
                                IconButton(onClick = { editingId = memory.id; editingText = memory.content }) { 
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp)) 
                                }
                                IconButton(onClick = { onDeleteEntry(memory.id) }) { 
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) 
                                }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.graphicsLayer { alpha = 0.3f })
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Manual Uplink", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = newKey, onValueChange = { newKey = it }, label = { Text("Type") }, modifier = Modifier.weight(0.4f), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(value = newValue, onValueChange = { newValue = it }, label = { Text("Data Shard") }, modifier = Modifier.weight(0.6f), shape = RoundedCornerShape(12.dp))
            }
            Spacer(Modifier.height(16.dp))
            BouncyButton(onClick = { if (newKey.isNotBlank() && newValue.isNotBlank()) { onAddEntry(newKey, newValue); newKey = ""; newValue = "" } }, modifier = Modifier.fillMaxWidth()) { Text("Commit to Core") }
        }
    }
}

@Composable
fun BouncyIconButton(icon: ImageVector, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "iconScale"
    )

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        interactionSource = interactionSource,
        modifier = Modifier.scale(scale)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun BouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "btnScale"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}

@Composable
fun DaveWelcomeCard(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "welcome_shimmer")
        val shimmerAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shimmerAlpha"
        )

        Surface(
            modifier = Modifier.size(88.dp).graphicsLayer { alpha = shimmerAlpha },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("⚡️", style = MaterialTheme.typography.displaySmall)
            }
        }
        Spacer(Modifier.height(20.dp))
        
        Text(
            "I'm Dave.",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.graphicsLayer { alpha = shimmerAlpha }
        )
        Text(
            "Your Elite AI Partner",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(Modifier.height(40.dp))
        
        Text(
            "Quick Directives",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(16.dp))
        
        suggestions.forEachIndexed { index, suggestion ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 100L)
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically { it / 2 } + fadeIn()
            ) {
                GlassButton(
                    onClick = { onSuggestionClick(suggestion) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        text = suggestion,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun MessageMedia(
    url: String,
    type: MediaType,
    modifier: Modifier = Modifier
) {
    when (type) {
        MediaType.IMAGE -> {
            AsyncImage(
                model = url,
                contentDescription = "Generated Image",
                modifier = modifier.background(Color.Black.copy(alpha = 0.1f))
            )
        }
        MediaType.VIDEO -> {
            VideoPlayer(
                videoUrl = url,
                modifier = modifier
            )
        }
        else -> {}
    }
}

@androidx.media3.common.util.UnstableApi
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatInputContainer(
    uiState: ChatUiState,
    viewModel: ChatViewModel,
    filePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    voiceToTextManager: VoiceToTextManager,
    micPermissionState: com.google.accompanist.permissions.PermissionState,
    isDockCollapsed: Boolean,
    onCollapseChange: (Boolean) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if ((dragAmount > 50) && !isDockCollapsed) {
                        onCollapseChange(true)
                    } else if ((dragAmount < -50) && isDockCollapsed) {
                        onCollapseChange(false)
                    }
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = !isDockCollapsed,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            DaveDock(
                uiState = uiState,
                onTextChanged = viewModel::onInputTextChanged,
                onSendClicked = { viewModel.sendMessage(muteVoice = true) },
                onAttachClicked = { filePickerLauncher.launch("*/*") },
                onMicClicked = {
                    if (micPermissionState.status.isGranted) {
                        if (uiState.isListening) voiceToTextManager.stopListening()
                        else voiceToTextManager.startListening()
                    } else {
                        micPermissionState.launchPermissionRequest()
                    }
                },
                onModeChange = viewModel::setMode,
                onVaultClick = { viewModel.toggleVault(open = true) },
                isLoading = uiState.isLoading
            )
        }

        AnimatedVisibility(
            visible = isDockCollapsed,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .width(100.dp)
                    .height(12.dp)
                    .clickable { onCollapseChange(false) },
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pillBreathing")
                    val pillAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.7f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pillAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = pillAlpha), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DaveDock(
    uiState: ChatUiState,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onAttachClicked: () -> Unit,
    onMicClicked: () -> Unit,
    onModeChange: (DaveMode) -> Unit,
    onVaultClick: () -> Unit,
    isLoading: Boolean,
) {
    val haptic = LocalHapticFeedback.current
    var isFocused by remember { mutableStateOf(false) }
    val focusColor = MaterialTheme.colorScheme.primary
    
    val dockGlowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.15f else 0.05f,
        animationSpec = tween(500),
        label = "glowAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { QuickActionChip(Icons.Rounded.AttachFile, "Signal", onAttachClicked) }
                item { QuickActionChip(Icons.Rounded.Memory, "Vault", onVaultClick) }
                item { 
                    QuickActionChip(Icons.Rounded.Psychology, uiState.currentMode.name) {
                        val nextMode = DaveMode.entries[(uiState.currentMode.ordinal + 1) % DaveMode.entries.size]
                        onModeChange(nextMode)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .drawWithContent {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(focusColor.copy(alpha = dockGlowAlpha), Color.Transparent)
                        )
                    )
                    drawContent()
                }
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, focusColor.copy(alpha = 0.4f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    if (isSystemInDarkTheme()) Color.Black.copy(alpha = 0.3f) 
                    else Color.White.copy(alpha = 0.5f)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = uiState.inputText,
                    onValueChange = { 
                        onTextChanged(it)
                        isFocused = it.isNotEmpty()
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            "Neural Link...", 
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        ) 
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        unfocusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                    ),
                    maxLines = 4,
                    enabled = !isLoading
                )

                if (uiState.inputText.isBlank() && !uiState.isLoading) {
                    IconButton(onClick = onMicClicked) {
                        Icon(
                            if (uiState.isListening) Icons.Rounded.Mic else Icons.Rounded.MicNone,
                            contentDescription = null,
                            tint = if (uiState.isListening) Color.Red else focusColor
                        )
                    }
                } else {
                    IconButton(
                        onClick = onSendClicked,
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            contentDescription = null,
                            tint = focusColor,
                            modifier = Modifier.scale(1.2f)
                        )
                    }
                }
            }
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Preview(showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    DaveAITheme {
        ChatScreen(viewModel = viewModel(), onLogout = {})
    }
}
