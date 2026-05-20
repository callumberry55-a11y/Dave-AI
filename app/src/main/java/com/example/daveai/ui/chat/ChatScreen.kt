package com.example.daveai.ui.chat

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.daveai.data.repository.UserProfile
import com.example.daveai.ui.theme.DaveAITheme
import com.example.daveai.util.VoiceToTextManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
    onEnterLiveMode: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showDeleteConfirmation by remember { mutableStateOf(value = false) }
    var isDockCollapsed by remember { mutableStateOf(value = false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

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
    val finalText by voiceToTextManager.finalText.collectAsState()

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            val location = locationHelper.getCurrentLocationName()
            viewModel.updateLocation(location)
        } else {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    // Update input text as speech is recognized
    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank()) {
            viewModel.onInputTextChanged(spokenText)
        }
    }

    // Continuous Voice Loop Handlers
    val app = context.applicationContext as com.example.daveai.DaveApplication
    val isDaveSpeaking by app.voiceManager.isSpeaking.collectAsState()

    // When continuous voice is ON, if Dave stops speaking, start listening again automatically.
    LaunchedEffect(isDaveSpeaking, uiState.isContinuousVoiceMode) {
        if (uiState.isContinuousVoiceMode && !isDaveSpeaking && !uiState.isLoading) {
            // Slight delay so he doesn't hear his own echo or cut the user off instantly
            kotlinx.coroutines.delay(500)
            if (micPermissionState.status.isGranted) {
                voiceToTextManager.startListening()
            }
        }
    }

    // If Dave is in continuous mode and we just finished listening (and we have text), send it!
    LaunchedEffect(isListening) {
        if (!isListening && uiState.isContinuousVoiceMode && uiState.inputText.isNotBlank() && !uiState.isLoading) {
            viewModel.sendMessage()
        }
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
                // Multi-modal support check
                val isImage = file.type.startsWith("image/")
                val isPdf = file.type == "application/pdf"
                
                // Allow any file. Text-based files will be parsed inside the repository.
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

    if (uiState.isVaultOpen) {
        MemoryVaultSheet(
            profile = uiState.userProfile,
            onDismiss = { viewModel.toggleVault(open = false) },
            onUpdateEntry = viewModel::updateVaultEntry,
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    item {
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
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center,
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
                                "Elite Projects",
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SmallProjectButton(Icons.Rounded.Terminal, "Code", Modifier.weight(1f)) { viewModel.createNewChat("CODE"); scope.launch { drawerState.close() } }
                                SmallProjectButton(Icons.Rounded.Brush, "Art", Modifier.weight(1f)) { viewModel.createNewChat("ART"); scope.launch { drawerState.close() } }
                                SmallProjectButton(Icons.Rounded.Language, "Lang", Modifier.weight(1f)) { viewModel.createNewChat("LANGUAGE"); scope.launch { drawerState.close() } }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SmallProjectButton(Icons.Rounded.MusicNote, "Music", Modifier.weight(1f)) { viewModel.createNewChat("MUSIC"); scope.launch { drawerState.close() } }
                                SmallProjectButton(Icons.Rounded.FitnessCenter, "Fit", Modifier.weight(1f)) { viewModel.createNewChat("FITNESS"); scope.launch { drawerState.close() } }
                                SmallProjectButton(Icons.Rounded.MonetizationOn, "Fin", Modifier.weight(1f)) { viewModel.createNewChat("FINANCE"); scope.launch { drawerState.close() } }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SmallProjectButton(Icons.Rounded.Flight, "Travel", Modifier.weight(1f)) { viewModel.createNewChat("TRAVEL"); scope.launch { drawerState.close() } }
                                SmallProjectButton(Icons.Rounded.SportsEsports, "Game", Modifier.weight(1f)) { viewModel.createNewChat("GAMING"); scope.launch { drawerState.close() } }
                                SmallProjectButton(Icons.Rounded.School, "Lesson", Modifier.weight(1f)) { viewModel.createNewChat("LESSONS"); scope.launch { drawerState.close() } }
                            }

                            Text(
                                "My Conversations",
                                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
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

                            // God Mode Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                                    .clickable { viewModel.toggleGodMode() }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Psychology, 
                                        contentDescription = null, 
                                        tint = if (uiState.isGodMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("God Mode", style = MaterialTheme.typography.labelLarge)
                                        Text("Uncapped Intelligence", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = uiState.isGodMode,
                                    onCheckedChange = { viewModel.toggleGodMode() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.tertiary)
                                )
                            }

                            // Ghost Mode Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                                    .clickable { viewModel.toggleGhostMode() }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.VisibilityOff, 
                                        contentDescription = null, 
                                        tint = if (uiState.isGhostMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Ghost Mode", style = MaterialTheme.typography.labelLarge)
                                        Text("Off-the-record chat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = uiState.isGhostMode,
                                    onCheckedChange = { viewModel.toggleGhostMode() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.error)
                                )
                            }

                            // Continuous Voice Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                                    .clickable { viewModel.toggleContinuousVoiceMode() }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Mic, 
                                        contentDescription = null, 
                                        tint = if (uiState.isContinuousVoiceMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Continuous Voice", style = MaterialTheme.typography.labelLarge)
                                        Text("Hands-free conversational loop", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = uiState.isContinuousVoiceMode,
                                    onCheckedChange = { viewModel.toggleContinuousVoiceMode() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.secondary)
                                )
                            }

                            // Assistant Settings
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                                    .clickable {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
                                        } catch (_: Exception) {
                                            try {
                                                context.startActivity(Intent("android.settings.VOICE_CONTROL_SETTINGS"))
                                            } catch (_: Exception) {
                                                android.util.Log.e("ChatScreen", "Failed to open assistant settings")
                                            }
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.SmartButton, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Default Assistant", style = MaterialTheme.typography.labelLarge)
                                    Text("Tap to set Dave as default", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // User Stats
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable { 
                                        scope.launch { drawerState.close() }
                                        viewModel.toggleVault(open = true)
                                    },
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Rounded.Memory, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Memory Vault", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            text = "Permanent Facts",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(uiState.sessions) { _, session ->
                        NavigationDrawerItem(
                            label = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            session.title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (session.sessionId == uiState.currentSessionId) MaterialTheme.colorScheme.primary else Color.Unspecified
                                        )
                                        session.summary?.let { summary ->
                                            Text(
                                                summary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
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

                    item {
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
        }
    ) {
    Scaffold(
            topBar = {
                // Glassmorphism Top Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                        Color.Transparent
                                    )
                                )
                            )
                    ) {
                        TopAppBar(
                            title = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Dave AI", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "BETA",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text("⚡️", style = MaterialTheme.typography.titleMedium)
                                }
                            },
                            navigationIcon = {
                                BouncyIconButton(icon = Icons.Rounded.Menu) { scope.launch { drawerState.open() } }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent
                            ),
                            actions = {
                                // Live mode disabled
                                // IconButton(onClick = onEnterLiveMode) {
                                //     Icon(Icons.Rounded.Mic, contentDescription = "Live Voice Mode", tint = MaterialTheme.colorScheme.secondary)
                                // }
                                IconButton(onClick = onEnterRiddleRoom) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = "Riddle Room", tint = MaterialTheme.colorScheme.tertiary)
                                }
                                BouncyIconButton(icon = Icons.Rounded.Delete) { showDeleteConfirmation = true }
                            }
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent
        ) { padding ->
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
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    item {
                        DaveWelcomeCard(
                            suggestions = uiState.dynamicSuggestions,
                        ) { suggestion ->
                            val cleanSuggestion = suggestion.substringAfter(" ").trim()
                            viewModel.onInputTextChanged(cleanSuggestion)
                            viewModel.sendMessage()
                        }
                    }
                }
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
                            MessageBubble(message, viewModel)
                        }
                    }
                    if (uiState.isLoading) {
                        item {
                            DaveIsTypingIndicator()
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
    var isExpanded by remember { mutableStateOf(value = false) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Expandable Quick Actions
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(Icons.Rounded.AttachFile, "File", onAttachClicked)
                QuickActionChip(Icons.Rounded.Memory, "Vault", onVaultClick)
                QuickActionChip(Icons.Rounded.Psychology, "Mode") {
                    val nextMode = DaveMode.entries[(uiState.currentMode.ordinal + 1) % DaveMode.entries.size]
                    onModeChange(nextMode)
                }
                QuickActionChip(Icons.Rounded.AutoAwesome, "Rewrite") {
                    if (uiState.inputText.isNotBlank()) {
                        onTextChanged("rewrite this: ${uiState.inputText}")
                        onSendClicked()
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            border = if (isLoading) 
                        androidx.compose.foundation.BorderStroke(
                            2.dp, 
                            Brush.sweepGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                     else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actions Button
                IconButton(
                    onClick = { 
                        isExpanded = !isExpanded
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer { rotationZ = if (isExpanded) 45f else 0f }
                ) {
                    Icon(
                        Icons.Rounded.Add, 
                        contentDescription = "Actions",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Mode Selector
                val modeColor = when(uiState.currentMode) {
                    DaveMode.RESEARCHER -> Color(0xFF64B5F6)
                    DaveMode.CREATIVE -> Color(0xFFBA68C8)
                    DaveMode.HACKER -> Color(0xFF4CAF50)
                    DaveMode.ANALYST -> Color(0xFFFFB74D)
                    DaveMode.GAMER -> Color(0xFFE57373)
                    else -> MaterialTheme.colorScheme.primary
                }
                Surface(
                    onClick = { 
                        val nextMode = DaveMode.entries[(uiState.currentMode.ordinal + 1) % DaveMode.entries.size]
                        onModeChange(nextMode)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier.padding(start = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = modeColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = uiState.currentMode.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = modeColor
                    )
                }

                // Input Field
                TextField(
                    value = uiState.inputText,
                    onValueChange = onTextChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            "Ask Dave...", 
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ) 
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 6,
                    enabled = !isLoading
                )

                // Voice / Send
                Box(contentAlignment = Alignment.Center) {
                    if (uiState.inputText.isBlank() && !uiState.isLoading) {
                        RecordingMicButton(
                            isListening = uiState.isListening,
                            onClick = onMicClicked
                        )
                    } else {
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.85f else 1.1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
                            label = "scale"
                        )

                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    enabled = !isLoading,
                                    onClick = onSendClicked
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
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
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun SmallProjectButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val alignment = if (message.isFromDave) Alignment.Start else Alignment.End
    
    val bubbleBg = if (message.isFromDave) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    } else {
        MaterialTheme.colorScheme.primary
    }
    
    val contentColor = if (message.isFromDave) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }
    
    val shape = if (message.isFromDave) {
        RoundedCornerShape(4.dp, 24.dp, 24.dp, 24.dp)
    } else {
        RoundedCornerShape(24.dp, 4.dp, 24.dp, 24.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = shape,
            shadowElevation = 1.dp, // Flow design prefers subtle inner depth rather than harsh drops
            color = bubbleBg.copy(alpha = if (message.isFromDave) 0.6f else 0.9f), // Glassy for Dave, solid for user
            border = if (message.isFromDave) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp)
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

                // Code Terminal Integration
                if (message.content.contains("```")) {
                    val parts = message.content.split("```")
                    parts.forEachIndexed { index, part ->
                        if ((index % 2) == 1) {
                            val language = part.substringBefore("\n").trim()
                            val code = part.substringAfter("\n").trim()
                            EliteCodeTerminal(language = language, code = code)
                        } else if (part.isNotBlank()) {
                            StructuredContent(part.trim(), contentColor)
                        }
                    }
                } else {
                    StructuredContent(message.content, contentColor)
                }

                // Hyper-Local Widget Integration
                if ((message.widgetType != WidgetType.NONE) && (message.widgetData != null)) {
                    Spacer(Modifier.height(12.dp))
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
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
                
                if (message.isFromDave) {
                    if (message.actions.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(message.actions) { action ->
                                BouncyButton(
                                    onClick = {
                                        viewModel.onInputTextChanged(action)
                                        viewModel.sendMessage()
                                    },
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(action, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

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
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else if ((dragAmount < -50) && isDockCollapsed) {
                        onCollapseChange(false)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                onSendClicked = viewModel::sendMessage,
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
                    .clickable {
                        onCollapseChange(false)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
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
                            .background(
                                color = if (uiState.isLoading) 
                                            MaterialTheme.colorScheme.primary.copy(alpha = pillAlpha)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), 
                                shape = CircleShape
                            )
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
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("⚡️", style = MaterialTheme.typography.displaySmall)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "I'm Dave.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )
        Text(
            "Your Elite AI Partner",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            "Try asking me to:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(12.dp))
        
        suggestions.forEach { suggestion ->
            val haptic = LocalHapticFeedback.current
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSuggestionClick(suggestion)
                    },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = suggestion,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DaveIsTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_container")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    Row(
        modifier = Modifier
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 4.dp)
            )
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 4.dp)
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModernTypingDots()
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            "Dave is thinking...",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ModernTypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "modern_dots")
    
    @Composable
    fun Dot(index: Int) {
        val delay = index * 200
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotScale"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotAlpha"
        )
        
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(8.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    ),
                    shape = CircleShape
                )
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
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
        // Set a 35MB limit to prevent OutOfMemoryError on extremely large files, but allow most PDFs/images
        val sizeLimit = 35 * 1024 * 1024
        var bytes = inputStream?.readBytes()
        if (bytes != null && bytes.size > sizeLimit) {
            bytes = null
            fileName = "$fileName (File too large, over 35MB)"
        }
        val base64 = bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        AttachedFile(uri, fileName, mimeType, base64)
    } catch (_: Exception) {
        null
    }
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
                Icon(
                    Icons.Rounded.Terminal,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = language.ifEmpty { "CODE" }.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Dave Code", code)))
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Rounded.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Run",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFFD4D4D4),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun DaveWidget(type: WidgetType, data: String) {
    val json = try { JSONObject(data) } catch (_: Exception) { null } ?: return
    
    when (type) {
        WidgetType.MAP -> MapWidget(json)
        WidgetType.HARDWARE -> HardwareWidget(json)
        else -> {}
    }
}

@Composable
fun MapWidget(data: JSONObject) {
    val places = data.optJSONArray("places") ?: return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Dave's Map Intelligence", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        
        // Mock Map View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Navigation, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(Modifier.height(12.dp))
        
        for (i in 0 until minOf(places.length(), 2)) {
            val place = places.getJSONObject(i)
            ListItem(
                headlineContent = { Text(place.optString("name"), fontWeight = FontWeight.Bold) },
                supportingContent = { Text(place.optString("address"), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { /* Navigate */ }
            )
        }
    }
}

@Composable
fun HardwareWidget(data: JSONObject) {
    val type = data.optString("type")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            .padding(16.dp)
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
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(level / 100f)
                        .fillMaxHeight()
                        .background(if (level > 20) MaterialTheme.colorScheme.primary else Color.Red)
                )
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
            Text("AICORE: ACTIVE", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryVaultSheet(
    profile: UserProfile?,
    onDismiss: () -> Unit,
    onUpdateEntry: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                "Dave's Memory Vault",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Dave keeps permanent facts about you here to be more helpful.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(24.dp))
            
            profile?.preferences?.forEach { (key, value) ->
                ListItem(
                    headlineContent = { Text(key.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                    supportingContent = { Text(value, style = MaterialTheme.typography.bodyLarge) },
                    trailingContent = { 
                        IconButton(onClick = { onUpdateEntry(key, "") }) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.graphicsLayer { alpha = 0.5f })
            }
            
            if (profile?.preferences.isNullOrEmpty()) {
                Text(
                    "Dave hasn't learned anything yet. Start chatting!",
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(24.dp))
            Text("Add Manual Entry", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newKey,
                    onValueChange = { newKey = it },
                    label = { Text("Fact Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    label = { Text("Detail") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            BouncyButton(
                onClick = {
                    if (newKey.isNotBlank() && newValue.isNotBlank()) {
                        onUpdateEntry(newKey, newValue)
                        newKey = ""
                        newValue = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lock Into Memory")
            }
        }
    }
}

@Composable
fun StructuredContent(text: String, contentColor: Color) {
    val lines = text.split("\n")
    var inTable = false
    val tableBuffer = mutableListOf<String>()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // Detect if line is part of a markdown table
            if (trimmedLine.contains("|") && !trimmedLine.startsWith("```")) {
                inTable = true
                tableBuffer.add(trimmedLine)
            } else {
                // If we were parsing a table and just exited it, render the table
                if (inTable) {
                    if (tableBuffer.size >= 2) {
                        Spacer(Modifier.height(8.dp))
                        EliteDataGrid(tableBuffer.toList())
                        Spacer(Modifier.height(8.dp))
                    } else if (tableBuffer.size == 1) {
                        // Not a real table, just render the line as text
                        Text(
                            text = tableBuffer.first(),
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 24.sp,
                                letterSpacing = 0.25.sp
                            )
                        )
                    }
                    tableBuffer.clear()
                    inTable = false
                }
                
                // Render normal text lines
                if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")) {
                    EliteBulletPoint(trimmedLine.substring(2), contentColor)
                } else if (trimmedLine.isNotEmpty() && !trimmedLine.contains("---")) {
                    Text(
                        text = trimmedLine,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp,
                            letterSpacing = 0.25.sp
                        )
                    )
                }
            }
        }
        
        // Render any remaining table at the end of the text
        if (inTable) {
            if (tableBuffer.size >= 2) {
                Spacer(Modifier.height(8.dp))
                EliteDataGrid(tableBuffer.toList())
            } else if (tableBuffer.size == 1) {
                Text(
                    text = tableBuffer.first(),
                    color = contentColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        letterSpacing = 0.25.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EliteBulletPoint(text: String, color: Color) {
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Rounded.PlayArrow, 
            contentDescription = null, 
            modifier = Modifier.size(14.dp).padding(top = 4.dp),
            tint = color.copy(alpha = 0.7f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp)
        )
    }
}

@Composable
fun EliteDataGrid(lines: List<String>) {
    val data = lines.asSequence().map { line ->
        line.split("|").asSequence().filter { it.isNotBlank() }.map { it.trim() }.toList()
    }.filter { it.isNotEmpty() && !it.all { cell -> cell.contains("-") } }.toList()

    if (data.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        data.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (rowIndex == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        style = if (rowIndex == 0) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
                        color = if (rowIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                }
            }
            if (rowIndex < data.lastIndex) {
                HorizontalDivider(modifier = Modifier.graphicsLayer { alpha = 0.3f })
            }
        }
    }
}

@androidx.media3.common.util.UnstableApi
@Preview(showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    DaveAITheme {
        ChatScreen(
            viewModel = viewModel(),
            onLogout = {},
        ) {}
    }
}
