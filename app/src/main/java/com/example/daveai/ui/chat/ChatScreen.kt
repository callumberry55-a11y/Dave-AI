@file:OptIn(
    com.google.accompanist.permissions.ExperimentalPermissionsApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
package com.example.daveai.ui.chat

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.daveai.data.model.DaveMode
import com.example.daveai.ui.components.FluidButton
import com.example.daveai.ui.components.GlassSidebar
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralLinkDialog
import com.example.daveai.ui.components.NeuralMetadataHeader
import com.example.daveai.ui.components.NeuralPulseIndicator
import com.example.daveai.ui.components.NeuralTextField
import com.example.daveai.ui.components.NeuralThinkingIndicator
import com.example.daveai.ui.components.NeuralTopBar
import com.example.daveai.ui.components.StructuredContent
import com.example.daveai.ui.components.organicBlobShape
import com.example.daveai.ui.theme.DaveAITheme
import com.example.daveai.ui.theme.DaveBlue
import com.example.daveai.ui.theme.DaveGreen
import com.example.daveai.ui.theme.DavePurple
import com.example.daveai.ui.theme.GhostWhite
import com.example.daveai.ui.theme.NeonEmerald
import com.example.daveai.ui.theme.ObsidianDeep
import com.example.daveai.ui.theme.ObsidianSurface
import com.example.daveai.ui.theme.PulseCyan
import com.example.daveai.util.VoiceToTextManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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
    onEnterDashboard: () -> Unit = {},
    onEnterSanctum: () -> Unit = {},
    onEnterVault: () -> Unit = {},
    onEnterMarketplace: () -> Unit = {},
    onEnterPersonaEditor: () -> Unit = {},
    onEnterVision: () -> Unit = {},
    onEnterMultimedia: () -> Unit = {},
    onEnterIdentityVerification: () -> Unit = {},
    onBackToHub: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val thinkingStatus by viewModel.thinkingStatus.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.pendingRoute) {
        if (uiState.pendingRoute is com.example.daveai.ui.navigation.DaveRoute.IdentityVerification) {
            onEnterIdentityVerification()
            viewModel.clearPendingRoute()
        }
    }
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
    val contactsPermissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)
    
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
                FluidButton(
                    onClick = {
                        viewModel.deleteCurrentSession()
                        showDeleteConfirmation = false
                    },
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onError,
                ) {
                    Text("Deconstruct")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Preserve", color = MaterialTheme.colorScheme.primary)
                }
            },
            title = { Text("Wipe Neural Memory?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("This will permanently deconstruct this neural thread.") },
            shape = RoundedCornerShape(32.dp),
            containerColor = ObsidianDeep
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
            onToggleLock = viewModel::toggleMemoryLock,
            onSearch = viewModel::searchMemories
        )
    }

    if (isNeuralLinkDialogOpen) {
        NeuralLinkDialog(
            pairingCode = uiState.pairingCode,
            partnerName = uiState.partnerName,
            onGenerateCode = viewModel::generatePairingCode,
            onLinkPartner = viewModel::linkPartner,
            onUnlinkPartner = viewModel::unlinkPartner,
            onDismiss = { isNeuralLinkDialogOpen = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GlassSidebar(
                userProfile = uiState.userProfile,
                sessions = uiState.sessions,
                currentSessionId = uiState.currentSessionId,
                onSessionSelected = { id ->
                    viewModel.selectSession(id)
                    scope.launch { drawerState.close() }
                },
                onCreateNewChat = {
                    viewModel.createNewChat("Neural Link Alpha")
                    scope.launch { drawerState.close() }
                },
                onEnterVault = onEnterVault,
                onEnterSanctum = onEnterSanctum,
                onEnterRiddleRoom = onEnterRiddleRoom,
                onEnterTerminal = onEnterDashboard,
                onEnterMarketplace = onEnterMarketplace,
                onEnterPersonaEditor = onEnterPersonaEditor,
                onEnterVision = onEnterVision,
                onEnterMultimedia = onEnterMultimedia,
                onUpdateGlowStrength = viewModel::updateGlowStrength,
                onUpdateBlurIntensity = viewModel::updateBlurIntensity,
                onModeChange = viewModel::setMode,
                currentMode = uiState.currentMode,
                onLogout = onLogout,
                glowStrength = uiState.glowStrength,
                blurIntensity = uiState.blurIntensity
            )
        },
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NeuralTopBar(
                    title = "Dave AI",
                    onNavigationClick = { scope.launch { drawerState.open() } },
                    navigationIcon = Icons.Rounded.Menu,
                    actions = {
                        IconButton(onClick = { isNeuralLinkDialogOpen = true }) {
                            Icon(
                                imageVector = if (uiState.partnerId != null) Icons.Rounded.Link else Icons.Rounded.LinkOff,
                                contentDescription = "Neural Link",
                                tint = if (uiState.partnerId != null) NeonEmerald else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                        IconButton(onClick = { 
                            val markdown = viewModel.generateSessionMarkdown()
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, markdown)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export Neural Thread"))
                        }) {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = "Export", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                        }
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                        }
                    },
                    isProactive = uiState.messages.any { it.isFromDave },
                    hasNeuralActivity = uiState.consciousnessStream.any { System.currentTimeMillis() - it.timestamp < 300_000 }
                )
            },
            bottomBar = {
                ChatInputContainer(
                    uiState = uiState,
                    viewModel = viewModel,
                    filePicker = filePickerLauncher,
                    voiceManager = voiceToTextManager,
                    micPermission = micPermissionState,
                    isCollapsed = isDockCollapsed,
                    onToggleCollapse = { isDockCollapsed = it },
                    haptic = haptic
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    if (uiState.messages.isEmpty()) {
                        item {
                            DaveWelcomeCard(
                                suggestions = uiState.dynamicSuggestions,
                                onSuggestionClick = { viewModel.onInputTextChanged(it); viewModel.sendMessage() }
                            )
                        }
                    }

                    itemsIndexed(uiState.messages) { index, message ->
                        MessageBubble(
                            message = message, 
                            viewModel = viewModel, 
                            contactsPermissionState = contactsPermissionState
                        )
                    }

                    if (uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.padding(16.dp)) {
                                NeuralThinkingIndicator()
                            }
                        }
                    }

                    if (thinkingStatus.isNotBlank()) {
                        item {
                            Box(modifier = Modifier.padding(16.dp)) {
                                NeuralMetadataHeader(label = "STATUS", value = thinkingStatus)
                            }
                        }
                    }
                }

                AppFactoryOverlay(
                    progress = uiState.buildProgress,
                    logs = uiState.buildLogs,
                    blueprint = uiState.appBlueprint,
                    isBuilding = uiState.isBuildingApp,
                    onClose = viewModel::closeAppFactory
                )
            }
        }
    }
}

@Composable
fun AppFactoryOverlay(
    progress: Float,
    logs: List<String>,
    blueprint: List<com.example.daveai.util.BlueprintItem>,
    isBuilding: Boolean,
    onClose: () -> Unit
) {
    if (isBuilding) {
        NeuralCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            isGodMode = true
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Architecting Application", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = null) }
                }
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = NeonEmerald,
                    trackColor = NeonEmerald.copy(alpha = 0.1f)
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(logs) { log ->
                        Text(
                            text = "> $log",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = NeonEmerald.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
@androidx.media3.common.util.UnstableApi
@Composable
fun MessageBubble(
    message: ChatMessage,
    viewModel: ChatViewModel,
    contactsPermissionState: PermissionState
) {
    val haptic = LocalHapticFeedback.current
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
    
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_morph")
    val morph by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Reverse),
        label = "morph"
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
    
    val contentColor = if (isDark) GhostWhite else MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current
    val hapticManager = remember { com.example.daveai.util.DaveHapticManager(context) }
    
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(2000)
            isCopied = false
        }
    }

    val copyAction = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Dave AI Message", message.content))
        hapticManager.signalSuccess()
        isCopied = true
        Toast.makeText(context, "Neural signal encoded to clipboard.", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(message.id) {
        if (message.isFromDave) {
            hapticManager.signalMood(message.mood)
        }
    }

    // Detect Neural Call Action
    val callActionRegex = Regex("""\[ACTION:CALL:(.*?)\]""")
    val callMatch = callActionRegex.find(message.content)
    val phoneNumber = callMatch?.groupValues?.get(1)

    if (phoneNumber != null) {
        LaunchedEffect(phoneNumber) {
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${phoneNumber.replace(Regex("[^0-9+]"), "")}")
                }
                context.startActivity(dialIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Neural link failed. Telephony hardware unavailable.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .graphicsLayer {
                scaleX = entranceScale
                scaleY = entranceScale
                alpha = entranceAlpha
                translationY = (1f - entranceAlpha) * 20f
            },
        horizontalAlignment = alignment
    ) {
        if (!message.isFromDave) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(organicBlobShape(morph))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), organicBlobShape(morph))
                    .combinedClickable(
                        onClick = {},
                        onLongClick = copyAction
                    ),
                color = Color.Transparent
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = copyAction
                    )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(accentColor, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "DAVE :: NEURAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.8f),
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.weight(1f))
                    TTSPlaybackButton(message, viewModel, accentColor)
                }
                Spacer(Modifier.height(12.dp))
                
                // Remove the action tag from display text
                val cleanText = message.content.replace(callActionRegex, "").trim()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            if (message.mood != "NEUTRAL") {
                                val glowAlpha = 0.15f
                                drawRect(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(accentColor.copy(alpha = glowAlpha), Color.Transparent),
                                        center = center,
                                        radius = size.width
                                    )
                                )
                            }
                        }
                ) {
                    StructuredContent(
                        text = cleanText,
                        contentColor = contentColor
                    )
                }
                
                if (phoneNumber != null) {
                    Spacer(Modifier.height(12.dp))
                    FluidButton(
                        onClick = {
                            if (contactsPermissionState.status.isGranted) {
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${phoneNumber.replace(Regex("[^0-9+]"), "")}")
                                }
                                context.startActivity(dialIntent)
                            } else {
                                contactsPermissionState.launchPermissionRequest()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(if (contactsPermissionState.status.isGranted) "Initiate Voice Link" else "Grant Contacts Access", fontWeight = FontWeight.Bold)
                    }
                }

                if ((message.widgetType != WidgetType.NONE) && (message.widgetData != null)) {
                    Spacer(Modifier.height(16.dp))
                    DaveWidget(type = message.widgetType, data = message.widgetData)
                }
            }
        }
        
        // Message Actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = if (message.isFromDave) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = copyAction,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy, 
                    contentDescription = "Copy", 
                    tint = if (isCopied) NeonEmerald else accentColor.copy(alpha = 0.5f), 
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = { 
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message.content)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Intelligence Signal"))
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Rounded.Share, contentDescription = "Share", tint = accentColor.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
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
            .background(accentColor.copy(alpha = 0.1f))
            .clickable {
                if (uiState.isSpeaking) viewModel.stopSpeaking()
                else viewModel.speak(message.content, mood = message.mood)
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (uiState.isSpeaking) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
            contentDescription = "Speak",
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun AttachmentIndicator(color: Color, isImage: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isImage) Icons.Rounded.Image else Icons.Rounded.Description,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (isImage) "IMAGE" else "DATA",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun CodeBlockRenderer(content: String, contentColor: Color) {
    val parts = content.split("```")
    Column {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 0) {
                StructuredContent(text = part.trim(), contentColor = contentColor)
            } else {
                val lines = part.trim().split("\n")
                val lang = lines.firstOrNull() ?: ""
                val code = lines.drop(1).joinToString("\n")
                EliteCodeTerminal(language = lang, code = code)
            }
        }
    }
}

@Composable
fun DaveIsTypingIndicator(status: String) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeuralPulseIndicator()
        Spacer(Modifier.width(12.dp))
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getAttachedFileFromUri(context: Context, uri: Uri): AttachedFile? {
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val name = it.getString(nameIndex)
            val type = contentResolver.getType(uri) ?: "application/octet-stream"
            
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            val base64 = bytes?.let { b -> Base64.encodeToString(b, Base64.DEFAULT) }
            
            AttachedFile(uri = uri, name = name, type = type, base64Data = base64)
        } else null
    }
}

@Composable
fun EliteCodeTerminal(language: String, code: String) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        color = ObsidianSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonEmerald,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Dave Code", code))
                        Toast.makeText(context, "Code linked to clipboard.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = NeonEmerald.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = code,
                modifier = Modifier
                    .padding(16.dp)
                    .horizontalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = GhostWhite.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun DaveWidget(type: WidgetType, data: String) {
    val json = remember(data) { try { JSONObject(data) } catch (_: Exception) { JSONObject() } }
    
    when (type) {
        WidgetType.MAP -> MapWidget(json)
        WidgetType.HARDWARE -> HardwareWidget(json)
        WidgetType.POETRY -> PoetryWidget(json)
        WidgetType.MEDIA -> MediaWidget(json)
        else -> Text("WIDGET :: $type [DATA_ENCRYPTED]", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun PoetryWidget(data: JSONObject) {
    val title = data.optString("title", "Neural Echo")
    val author = data.optString("author", "Dave")
    val lines = data.optJSONArray("lines")
    val content = data.optString("content", "")

    NeuralCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text("by $author", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(12.dp))
            
            if (lines != null) {
                for (i in 0 until lines.length()) {
                    Text(lines.optString(i), style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Serif)
                }
            } else {
                Text(content, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Serif)
            }
        }
    }
}

@Composable
fun MediaWidget(data: JSONObject) {
    val url = data.optString("url", "")
    val id = data.optString("id", "")
    val type = data.optString("type", "IMAGE")

    NeuralCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (type == "IMAGE") Icons.Rounded.Image else Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (type == "IMAGE") "Visual Synthesis" else "Audio Composition",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            
            if (type == "IMAGE" && url.isNotBlank()) {
                AsyncImage(
                    model = url,
                    contentDescription = "Generated Image",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                Text("Content ID: $id", style = MaterialTheme.typography.bodyMedium)
                Text("Processing on neural mainframe...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                if (url.isNotBlank()) {
                    BouncyButton(
                        onClick = { /* Open URL */ },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Stream Audio")
                    }
                }
            }
        }
    }
}

@Composable
fun MapWidget(data: JSONObject) {
    val lat = data.optDouble("lat", 0.0)
    val lng = data.optDouble("lng", 0.0)
    val label = data.optString("label", "Target Location")

    NeuralCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Map, contentDescription = null, tint = NeonEmerald)
                Spacer(Modifier.width(12.dp))
                Text("NEURAL MAP LINK", style = MaterialTheme.typography.labelMedium, color = NeonEmerald)
            }
            Spacer(Modifier.height(16.dp))
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text("Coordinates: $lat, $lng", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun HardwareWidget(data: JSONObject) {
    val battery = data.optInt("battery", 0)
    val temp = data.optDouble("temp", 0.0)

    NeuralCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("SYSTEM DIAGNOSTICS", style = MaterialTheme.typography.labelSmall, color = PulseCyan)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("ENERGY", style = MaterialTheme.typography.labelSmall)
                    Text("$battery%", style = MaterialTheme.typography.headlineSmall, color = if (battery < 20) Color.Red else NeonEmerald)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("THERMAL", style = MaterialTheme.typography.labelSmall)
                    Text("${temp}°C", style = MaterialTheme.typography.headlineSmall, color = if (temp > 45) Color.Red else PulseCyan)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryVaultSheet(
    memories: List<SemanticMemory>,
    onDismiss: () -> Unit,
    onAddEntry: (String, String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onStrengthenEntry: (Long) -> Unit,
    onArchiveEntry: (Long, Boolean) -> Unit,
    onEditEntry: (Long, String) -> Unit,
    onToggleLock: (Long) -> Unit,
    onSearch: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ObsidianDeep,
        dragHandle = { BottomSheetDefaults.DragHandle(color = NeonEmerald.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Memory, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Text("NEURAL VAULT", style = MaterialTheme.typography.headlineMedium, color = GhostWhite)
            }
            
            Spacer(Modifier.height(16.dp))
            
            var searchQuery by remember { mutableStateOf("") }
            NeuralTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    onSearch(it)
                },
                label = "Search semantic context...",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(24.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(memories) { memory ->
                    NeuralCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = memory.memoryType.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonEmerald
                                )
                                Spacer(Modifier.weight(1f))
                                if (memory.isLocked) {
                                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(text = memory.content, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { onToggleLock(memory.id) }) {
                                    Icon(if (memory.isLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock, contentDescription = null)
                                }
                                IconButton(onClick = { onDeleteEntry(memory.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .blur(40.dp)
                    .background(NeonEmerald.copy(alpha = 0.4f), CircleShape)
            )
            
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = NeonEmerald
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = "DAVE :: ACTIVE",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            color = GhostWhite
        )
        
        Text(
            text = "Digital Assistant & Vocal Entity",
            style = MaterialTheme.typography.bodyMedium,
            color = GhostWhite.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(48.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(suggestions) { suggestion ->
                FluidButton(
                    onClick = { onSuggestionClick(suggestion) },
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = NeonEmerald
                ) {
                    Text(suggestion, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun MessageMedia(url: String, type: MediaType, modifier: Modifier) {
    when (type) {
        MediaType.IMAGE -> {
            AsyncImage(
                model = url,
                contentDescription = "Dave Vision",
                modifier = modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            )
        }
        MediaType.VIDEO -> VideoPlayer(url = url, modifier = modifier)
        else -> {}
    }
}

@Composable
fun VideoPlayer(url: String, modifier: Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = modifier.clip(RoundedCornerShape(20.dp))
    )
}

@Composable
fun ChatInputContainer(
    uiState: ChatUiState,
    viewModel: ChatViewModel,
    filePicker: ActivityResultLauncher<String>,
    voiceManager: VoiceToTextManager,
    micPermission: PermissionState,
    isCollapsed: Boolean,
    onToggleCollapse: (Boolean) -> Unit,
    haptic: HapticFeedback
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp)
    ) {
        NeuralCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            containerColor = ObsidianSurface.copy(alpha = 0.95f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { filePicker.launch("*/*") }) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = "Attach", tint = NeonEmerald)
                }
                
                TextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::onInputTextChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Synchronize...", modifier = Modifier.alpha(0.4f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = NeonEmerald
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif)
                )

                val showSend = uiState.inputText.isNotBlank()
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isListening) NeonEmerald else MaterialTheme.colorScheme.primary)
                        .clickable {
                            if (showSend) {
                                viewModel.sendMessage()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                if (micPermission.status.isGranted) {
                                    if (uiState.isListening) voiceManager.stopListening()
                                    else voiceManager.startListening()
                                } else {
                                    micPermission.launchPermissionRequest()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showSend) Icons.Rounded.Send else if (uiState.isListening) Icons.Rounded.Mic else Icons.Rounded.MicNone,
                        contentDescription = if (showSend) "Send" else "Action",
                        tint = if (uiState.isListening) ObsidianDeep else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        shape = RoundedCornerShape(12.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.White.copy(alpha = 0.05f),
            labelColor = GhostWhite,
            leadingIconContentColor = NeonEmerald
        ),
        border = AssistChipDefaults.assistChipBorder(borderColor = Color.White.copy(alpha = 0.1f), enabled = true)
    )
}

@Composable
fun DaveDock(
    uiState: ChatUiState,
    onActionClick: (String) -> Unit,
    onToggleVault: () -> Unit,
    onEnterSanctum: () -> Unit,
    onEnterTerminal: () -> Unit,
    onModeChange: (DaveMode) -> Unit,
    onEnterRiddle: () -> Unit,
    isMoodReactive: Boolean
) {
    // Dock implementation
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    DaveAITheme {
        Box(Modifier.fillMaxSize().background(ObsidianDeep)) {
            // Preview content
        }
    }
}
