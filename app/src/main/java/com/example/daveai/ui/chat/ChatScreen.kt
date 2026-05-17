package com.example.daveai.ui.chat

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.daveai.ui.theme.DaveAITheme
import com.example.daveai.util.VoiceToTextManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@UnstableApi
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onLogout: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showDeleteConfirmation by remember { mutableStateOf(value = false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val voiceToTextManager = remember { VoiceToTextManager(context) }
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    val isListening by voiceToTextManager.isListening.collectAsState()
    val spokenText by voiceToTextManager.spokenText.collectAsState()

    // Sync voice manager state to ViewModel
    LaunchedEffect(isListening) {
        viewModel.setIsListening(isListening)
    }

    // Update input text as speech is recognized
    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank()) {
            viewModel.onInputTextChanged(spokenText)
        }
    }

    DisposableEffect(Unit) {
        onDispose { voiceToTextManager.destroy() }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            getAttachedFileFromUri(context, it)?.let { file ->
                // Multi-modal support check
                val isImage = file.type.startsWith("image/")
                val isPdf = file.type == "application/pdf"
                
                if (isImage || isPdf) {
                    viewModel.addAttachment(file)
                } else {
                    // Let Dave explain the limitation instead of cluttering the text field
                    viewModel.addAttachment(file)
                }
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
            onDismissRequest = { /* No-op to avoid state mutation during recomposition if needed, but usually we set to false here */ showDeleteConfirmation = false },
            title = { Text("Clear Conversation?", fontWeight = FontWeight.Bold) },
            text = { Text("This will wipe Dave's memory of this chat.") },
            confirmButton = {
                BouncyButton(
                    onClick = {
                        viewModel.deleteCurrentSession()
                        showDeleteConfirmation = false
                    },
                    containerColor = MaterialTheme.colorScheme.error,
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
            shape = RoundedCornerShape(28.dp),
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
            ) {
                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    // Profile Header
                    uiState.userProfile?.let { profile ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profile.displayName?.take(1)?.uppercase() ?: "D",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        profile.displayName ?: "Explorer",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        profile.role ?: "Elite User",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "My Conversations",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    BouncyButton(
                        onClick = {
                            viewModel.createNewChat()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("New Chat")
                    }

                    // Fast Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                            .clickable { viewModel.toggleFastMode() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Speed, 
                                contentDescription = null, 
                                tint = if (uiState.isFastMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Fast Mode", style = MaterialTheme.typography.labelLarge)
                                Text("Using Claude 4.7", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = uiState.isFastMode,
                            onCheckedChange = { viewModel.toggleFastMode() }
                        )
                    }

                    // User Stats
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "APP STATS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Users", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = uiState.totalAppUsers.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    
                    LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                        itemsIndexed(uiState.sessions) { _, session ->
                            NavigationDrawerItem(
                                label = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            session.title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                    onClick = { viewModel.deleteSession(session.sessionId) },
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Delete Chat",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        },
                        selected = session.sessionId == uiState.currentSessionId,
                        onClick = {
                            viewModel.selectSession(session.sessionId)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        shape = RoundedCornerShape(16.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }

            // Logout Button
            NavigationDrawerItem(
                label = { Text("Logout", fontWeight = FontWeight.Bold) },
                selected = false,
                onClick = onLogout,
                icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                shape = RoundedCornerShape(16.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedTextColor = MaterialTheme.colorScheme.error,
                    unselectedIconColor = MaterialTheme.colorScheme.error,
                ),
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
) {
    Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { 
                        Column {
                            Text("Dave AI", fontWeight = FontWeight.Black)
                            Text("Always energetic!", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    navigationIcon = {
                        BouncyIconButton(icon = Icons.Rounded.Menu) { scope.launch { drawerState.open() } }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    ),
                    actions = {
                        BouncyIconButton(icon = Icons.Rounded.Delete) { showDeleteConfirmation = true }
                    }
                )
            },
            bottomBar = {
                ChatInputBar(
                    inputText = uiState.inputText,
                    attachedFiles = uiState.attachedFiles,
                    isListening = uiState.isListening,
                    onTextChanged = viewModel::onInputTextChanged,
                    onSendClicked = viewModel::sendMessage,
                    onAttachClicked = { filePickerLauncher.launch("*/*") },
                    onMicClicked = {
                        if (micPermissionState.status.isGranted) {
                            if (isListening) voiceToTextManager.stopListening()
                            else voiceToTextManager.startListening()
                        } else {
                            micPermissionState.launchPermissionRequest()
                        }
                    },
                    onRemoveAttachment = viewModel::removeAttachment,
                    isLoading = uiState.isLoading
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(bottom = 4.dp), // Minimal bottom padding for messages
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 80.dp), // Adjust internal padding
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Tighter spacing
                ) {
                    itemsIndexed(
                        items = uiState.messages,
                        key = { index, _ -> index }
                    ) { _, message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { 50 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                            ) + fadeIn()
                        ) {
                            MessageBubble(message)
                        }
                    }
                    if (uiState.isLoading) {
                        item {
                            DaveIsTypingIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val alignment = if (message.isFromDave) Alignment.Start else Alignment.End
    
    val containerBrush = if (message.isFromDave) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        )
    }
    
    val contentColor = if (message.isFromDave) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }
    
    val shape = if (message.isFromDave) {
        RoundedCornerShape(24.dp, 24.dp, 24.dp, 4.dp)
    } else {
        RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = shape,
            shadowElevation = 2.dp, // Reduced elevation for stability/cleanliness
            modifier = Modifier.widthIn(max = 340.dp) // Wider bubbles for readability
        ) {
            Column(
                modifier = Modifier
                    .background(containerBrush)
                    .padding(horizontal = 16.dp, vertical = 10.dp) // Optimized bubble internal padding
            ) {
                if (message.hasAttachment) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.AttachFile, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp),
                            tint = contentColor.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "ATTACHMENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = message.content,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        letterSpacing = 0.25.sp
                    )
                )

                if (message.mediaUrl != null) {
                    Spacer(Modifier.height(12.dp))
                    @OptIn(UnstableApi::class)
                    MessageMedia(
                        url = message.mediaUrl,
                        type = message.mediaType,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
                
                if (message.isFromDave) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Dave AI", message.content)))
                                }
                            },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(18.dp),
                                tint = contentColor.copy(alpha = 0.5f)
                            )
                        }
                        
                        Spacer(Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, message.content)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Dave's response"))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(18.dp),
                                tint = contentColor.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@UnstableApi
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

@UnstableApi
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

@Composable
fun ChatInputBar(
    inputText: String,
    attachedFiles: List<AttachedFile>,
    isListening: Boolean,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onAttachClicked: () -> Unit,
    onMicClicked: () -> Unit,
    onRemoveAttachment: (AttachedFile) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 80.dp)
    ) {
        if (attachedFiles.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attachedFiles) { file ->
                    FileChip(file = file) { onRemoveAttachment(file) }
                }
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                        RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BouncyIconButton(icon = Icons.Rounded.AttachFile, onClick = onAttachClicked)
                
                TextField(
                    value = inputText,
                    onValueChange = onTextChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask Dave / Share files...", color = MaterialTheme.colorScheme.outline) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 5,
                    enabled = !isLoading
                )
                
                RecordingMicButton(
                    isListening = isListening,
                    onClick = onMicClicked
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.85f else if (inputText.isNotBlank() || attachedFiles.isNotEmpty()) 1.1f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if ((inputText.isNotBlank() || attachedFiles.isNotEmpty()) && !isLoading) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = (inputText.isNotBlank() || attachedFiles.isNotEmpty()) && !isLoading,
                            onClick = onSendClicked
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = if ((inputText.isNotBlank() || attachedFiles.isNotEmpty()) && !isLoading) 
                                   MaterialTheme.colorScheme.onPrimary 
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingMicButton(
    isListening: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp)
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
        }
        
        BouncyIconButton(
            icon = if (isListening) Icons.Rounded.Mic else Icons.Rounded.MicNone,
            onClick = onClick
        )
    }
}

@Composable
fun FileChip(file: AttachedFile, onRemove: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(file.name, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Rounded.Close, 
                contentDescription = "Remove", 
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .clickable { onRemove() }
            )
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
fun DaveIsTypingIndicator() {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            TypingDots()
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Dave is thinking...",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun TypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val delay = 200
    
    @Composable
    fun Dot(index: Int) {
        val yOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, delayMillis = index * delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "yOffset"
        )
        
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .size(6.dp)
                .graphicsLayer(translationY = yOffset)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
    }

    Row {
        Dot(0)
        Dot(1)
        Dot(2)
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
    } catch (_: Exception) {
        null
    }
}

@UnstableApi
@Preview(showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    DaveAITheme {
        ChatScreen(
            viewModel = viewModel(),
        ) {}
    }
}
