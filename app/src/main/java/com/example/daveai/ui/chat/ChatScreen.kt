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
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.daveai.ui.components.GlassButton
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralMetadataHeader
import com.example.daveai.ui.components.NeuralThinkingIndicator
import com.example.daveai.ui.components.NeuralTopBar
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
    onEnterSanctum: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isDockCollapsed by remember { mutableStateOf(false) }
    var selectedDrawerTab by remember { mutableIntStateOf(0) } 
    var isPersonalizationPageOpen by remember { mutableStateOf(false) }
    
    var buildTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    
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

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            viewModel.updateCustomWallpaper(it.toString())
        }
    }

    val locationHelper = remember { com.example.daveai.util.LocationHelper(context) }

    val isListening by voiceToTextManager.isListening.collectAsState()
    val spokenText by voiceToTextManager.spokenText.collectAsState()

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            val location = locationHelper.getCurrentLocationName()
            viewModel.updateLocation(location)
        } else {
            locationPermissionState.launchMultiplePermissionRequest()
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
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    if (isPersonalizationPageOpen) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { isPersonalizationPageOpen = false }) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Personalization",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                NeuralCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Aura Color", style = MaterialTheme.typography.titleSmall)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Mood Aura", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(Modifier.width(4.dp))
                                                Switch(
                                                    checked = uiState.isMoodReactive,
                                                    onCheckedChange = { viewModel.toggleMoodReactivity(it) },
                                                    modifier = Modifier.scale(0.7f)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            val colors = listOf(0xFF00E676, 0xFF2979FF, 0xFFD500F9, 0xFFFFD600)
                                            colors.forEach { colorHex ->
                                                val color = Color(colorHex)
                                                val isSelected = uiState.primaryColor == colorHex.toInt()
                                                Surface(
                                                    modifier = Modifier.size(40.dp),
                                                    shape = CircleShape,
                                                    color = color,
                                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null,
                                                    onClick = { viewModel.updatePrimaryColor(colorHex.toInt()) }
                                                ) {
                                                    if (isSelected) {
                                                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                NeuralCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Visual Voice", style = MaterialTheme.typography.titleSmall)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("MODERN", "MONO", "SERIF").forEach { style ->
                                                FilterChip(
                                                    selected = uiState.typographyStyle == style,
                                                    onClick = { viewModel.updateTypographyStyle(style) },
                                                    label = { Text(style, fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                NeuralCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Animation Pulse", style = MaterialTheme.typography.titleSmall)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Slider(
                                            value = uiState.meshAnimationSpeed,
                                            onValueChange = { viewModel.updateAnimationSpeed(it) },
                                            valueRange = 0.1f..3f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary,
                                                activeTrackColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                NeuralCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Wallpaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Background Layer", style = MaterialTheme.typography.titleSmall)
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("System Wallpaper", style = MaterialTheme.typography.bodySmall)
                                            Switch(
                                                checked = uiState.useSystemWallpaper,
                                                onCheckedChange = { viewModel.toggleSystemWallpaper(it) }
                                            )
                                        }
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp).graphicsLayer { alpha = 0.2f })
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Custom Image", style = MaterialTheme.typography.bodySmall)
                                            Row {
                                                if (uiState.customWallpaperUri != null) {
                                                    IconButton(onClick = { viewModel.updateCustomWallpaper(null) }) {
                                                        Icon(Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                IconButton(onClick = { 
                                                    wallpaperPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                                                }) {
                                                    Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                NeuralCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Neural Persona", style = MaterialTheme.typography.titleSmall)
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val personas = listOf("HACKER", "ZEN", "STRATEGIST")
                                            items(personas) { persona ->
                                                val isSelected = uiState.digitalPersona == persona
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { viewModel.updateDigitalPersona(persona) },
                                                    label = { Text(persona, fontSize = 10.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                NeuralCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Brush, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Cyber Intensity", style = MaterialTheme.typography.titleSmall)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Slider(
                                            value = uiState.cyberIntensity,
                                            onValueChange = { viewModel.updateCyberIntensity(it) },
                                            valueRange = 0f..1f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary,
                                                activeTrackColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                                Spacer(Modifier.height(32.dp))
                            }
                        }
                    } else {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                                uiState.userProfile?.let { profile ->
                                    NeuralCard(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                modifier = Modifier.size(52.dp).clip(CircleShape).background(
                                                        brush = Brush.sweepGradient(
                                                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.primary)
                                                        )
                                                    ).padding(2.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val initial = profile.displayName?.take(1)?.uppercase() ?: "D"
                                                    Text(
                                                        text = initial,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        style = MaterialTheme.typography.headlineSmall,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    profile.displayName ?: "Explorer",
                                                    fontWeight = FontWeight.Black,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    letterSpacing = (-0.5).sp
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(6.dp).background(Color.Green, CircleShape))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        profile.role ?: "Elite User",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                val tabs = listOf(
                                    Triple("Chats", Icons.Rounded.ChatBubbleOutline, 0),
                                    Triple("Projects", Icons.Rounded.GridView, 1),
                                    Triple("Settings", Icons.Rounded.Settings, 2)
                                )
                                TabRow(
                                    selectedTabIndex = selectedDrawerTab,
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    indicator = { tabPositions ->
                                        if (selectedDrawerTab < tabPositions.size) {
                                            TabRowDefaults.SecondaryIndicator(
                                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedDrawerTab]),
                                                color = MaterialTheme.colorScheme.primary,
                                                height = 3.dp
                                            )
                                        }
                                    },
                                    divider = {}
                                ) {
                                    tabs.forEach { (title, icon, index) ->
                                        Tab(
                                            selected = selectedDrawerTab == index,
                                            onClick = { selectedDrawerTab = index },
                                            text = { 
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        icon, 
                                                        contentDescription = null, 
                                                        modifier = Modifier.size(16.dp),
                                                        tint = if (selectedDrawerTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        title, 
                                                        fontWeight = if (selectedDrawerTab == index) FontWeight.Bold else FontWeight.Normal,
                                                        style = MaterialTheme.typography.labelLarge
                                                    ) 
                                                }
                                            }
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.height(16.dp))

                                if (selectedDrawerTab == 1) {
                                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                        ProjectCategoryHeader("LOGIC PROTOCOLS", MaterialTheme.colorScheme.primary)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SmallProjectButton(Icons.Rounded.Terminal, "Code", "System logic & hacking", Modifier.weight(1f)) { viewModel.createNewChat("CODE"); scope.launch { drawerState.close() } }
                                            SmallProjectButton(Icons.Rounded.Brush, "Art", "Creative visual synthesis", Modifier.weight(1f)) { viewModel.createNewChat("ART"); scope.launch { drawerState.close() } }
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        ProjectCategoryHeader("LIFESTYLE SYNC", MaterialTheme.colorScheme.secondary)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SmallProjectButton(Icons.Rounded.MusicNote, "Music", "Sonic vibe analysis", Modifier.weight(1f)) { viewModel.createNewChat("MUSIC"); scope.launch { drawerState.close() } }
                                            SmallProjectButton(Icons.Rounded.FitnessCenter, "Fit", "Biometric optimization", Modifier.weight(1f)) { viewModel.createNewChat("FITNESS"); scope.launch { drawerState.close() } }
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        ProjectCategoryHeader("INTEL & GROWTH", MaterialTheme.colorScheme.tertiary)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SmallProjectButton(Icons.Rounded.MonetizationOn, "Fin", "Market pulse monitoring", Modifier.weight(1f)) { viewModel.createNewChat("FINANCE"); scope.launch { drawerState.close() } }
                                            SmallProjectButton(Icons.Rounded.School, "Learn", "Module mastery cycle", Modifier.weight(1f)) { viewModel.createNewChat("LESSONS"); scope.launch { drawerState.close() } }
                                        }
                                        Spacer(Modifier.height(24.dp))
                                    }
                                }
                            }
                        }

                        if (selectedDrawerTab == 0) {
                            item {
                                GlassButton(
                                    onClick = {
                                        viewModel.createNewChat()
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("New Chat")
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
                        }

                        if (selectedDrawerTab == 2) {
                            item {
                                NavigationDrawerItem(
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text("Personalization", style = MaterialTheme.typography.labelLarge)
                                                Text("Aura, Voice & Backgrounds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    selected = false,
                                    onClick = { isPersonalizationPageOpen = true },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                                    )
                                )

                                NavigationDrawerItem(
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text("The Sanctum", style = MaterialTheme.typography.labelLarge)
                                                Text("Dave's Virtual Server Core", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    selected = false,
                                    onClick = { onEnterSanctum() },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                                    )
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)).clickable { viewModel.toggleFastMode() }.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Speed, contentDescription = null, tint = if (uiState.isFastMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Fast Mode", style = MaterialTheme.typography.labelLarge)
                                            Text("Using Claude 4.7", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Switch(checked = uiState.isFastMode, onCheckedChange = { viewModel.toggleFastMode() })
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)).clickable { viewModel.toggleGodMode() }.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Psychology, contentDescription = null, tint = if (uiState.isGodMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("God Mode", style = MaterialTheme.typography.labelLarge)
                                            Text("Uncapped Intelligence", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Switch(checked = uiState.isGodMode, onCheckedChange = { viewModel.toggleGodMode() }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.tertiary))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)).clickable { viewModel.toggleGhostMode() }.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.VisibilityOff, contentDescription = null, tint = if (uiState.isGhostMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Ghost Mode", style = MaterialTheme.typography.labelLarge)
                                            Text("Off-the-record chat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Switch(checked = uiState.isGhostMode, onCheckedChange = { viewModel.toggleGhostMode() }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.error))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)).clickable { viewModel.toggleIrishAccent(!uiState.useIrishAccent) }.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Mic, contentDescription = null, tint = if (uiState.useIrishAccent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Irish Accent", style = MaterialTheme.typography.labelLarge)
                                            Text("Cheeky OpenAI Voice", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Switch(checked = uiState.useIrishAccent, onCheckedChange = { viewModel.toggleIrishAccent(it) })
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)).clickable {
                                        try { context.startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)) } catch (_: Exception) {
                                            try { context.startActivity(Intent("android.settings.VOICE_CONTROL_SETTINGS")) } catch (_: Exception) { android.util.Log.e("ChatScreen", "Failed to open assistant settings") }
                                        }
                                    }.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Rounded.SmartButton, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("Default Assistant", style = MaterialTheme.typography.labelLarge)
                                        Text("Tap to set Dave as default", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { 
                                        scope.launch { drawerState.close() }
                                        viewModel.toggleVault(open = true)
                                    },
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(20.dp),
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Memory Vault", style = MaterialTheme.typography.labelSmall)
                                            Text(text = "Permanent Facts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

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

                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Build: AP37.2026.11",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clickable {
                                            val now = System.currentTimeMillis()
                                            if (now - lastTapTime > 2000) {
                                                buildTapCount = 1
                                            } else {
                                                buildTapCount++
                                            }
                                            lastTapTime = now

                                            if (buildTapCount in 7..9) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "You are now ${10 - buildTapCount} steps away from entering the mainframe.",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            } else if (buildTapCount >= 10) {
                                                buildTapCount = 0
                                                onEnterTerminal()
                                            }
                                        },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
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
                        IconButton(onClick = onEnterRiddleRoom) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = "Riddle Room", tint = MaterialTheme.colorScheme.tertiary)
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
                        ) { index, message ->
                            val hapticLocal = LocalHapticFeedback.current
                            LaunchedEffect(uiState.messages.size) {
                                if (index == uiState.messages.size - 1) {
                                    hapticLocal.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }

                            AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + 
                                        expandVertically(expandFrom = Alignment.Top) + fadeIn()
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

                // Phase 18: Dave App Factory Overlay
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
                // Visual Mock Preview
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
                    onClick = { /* Could open the file or share */ },
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
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
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
                    } else if (uiState.messages.isNotEmpty()) {
                        val lastMessage = uiState.messages.last().content
                        onTextChanged("rewrite this: $lastMessage")
                        onSendClicked()
                    }
                }
            }
        }

        val rotation by animateFloatAsState(
            targetValue = if (isExpanded) 135f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "rotation"
        )

        NeuralCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            shape = RoundedCornerShape(32.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        isExpanded = !isExpanded
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer { rotationZ = rotation }
                ) {
                    Icon(
                        Icons.Rounded.Add, 
                        contentDescription = "Actions",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                val modeColor = when(uiState.currentMode) {
                    DaveMode.RESEARCHER -> Color(0xFF64B5F6)
                    DaveMode.CREATIVE -> Color(0xFFBA68C8)
                    DaveMode.HACKER -> Color(0xFF4CAF50)
                    DaveMode.ANALYST -> Color(0xFFFFB74D)
                    DaveMode.GAMER -> Color(0xFFE57373)
                    DaveMode.VISIONARY -> Color(0xFF00BCD4)
                    DaveMode.SOCIOLOGIST -> Color(0xFF9C27B0)
                    DaveMode.APP_FACTORY -> Color(0xFF00E676)
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
        contentColor = MaterialTheme.colorScheme.onSurface,
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
fun ProjectCategoryHeader(title: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(width = 12.dp, height = 2.dp).background(color, RoundedCornerShape(1.dp)))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SmallProjectButton(icon: ImageVector, label: String, description: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    NeuralCard(
        modifier = modifier
            .height(100.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(24.dp), 
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description, 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    lineHeight = 12.sp
                )
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
    
    val bubbleBg = if (message.isFromDave) {
        when (message.mood) {
            "EMPATHETIC" -> DavePurple
            "HYPED" -> DaveGreen
            "URGENT" -> Color(0xFFE53935)
            "CALM" -> DaveBlue
            else -> MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
        }
    } else {
        MaterialTheme.colorScheme.primary
    }
    
    val contentColor = if (message.isFromDave) {
        if (message.mood != "NEUTRAL") Color.White else MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }
    
    val shape = if (message.isFromDave) {
        RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = alignment
    ) {
        // Technical Header
        val headerLabel = if (message.isFromDave) "OS_DAVE" else "USER_ELITE"
        val headerValue = remember(message.content) {
            if (message.isFromDave) {
                "LATENCY: ${kotlin.random.Random.nextInt(5, 40)}ms"
            } else {
                listOf("SIGNAL: SECURE", "SIGNAL: LOCKED", "ENCRYPTION: ACTIVE").random()
            }
        }
        
        NeuralMetadataHeader(
            label = headerLabel,
            value = headerValue,
            color = if (message.isFromDave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        val uiState by viewModel.uiState.collectAsState()
        NeuralCard(
            shape = shape,
            containerColor = bubbleBg,
            isGodMode = uiState.isGodMode && message.isFromDave,
            modifier = Modifier.widthIn(max = 340.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                if (message.hasAttachment) {
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (message.isFromDave) Icons.Rounded.CheckCircle else Icons.Rounded.AttachFile, 
                            contentDescription = null, 
                            modifier = Modifier.size(14.dp),
                            tint = if (message.isFromDave) Color(0xFF4ADE80) else contentColor.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (message.isFromDave) "NEURAL ANALYSIS COMPLETE" else "UPLOADING SIGNAL...",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (message.isFromDave) Color(0xFF4ADE80) else contentColor.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp
                        )
                    }
                }

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

                // Footer Metadata
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timestamp = remember(message) {
                        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    }
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.4f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp
                    )

                    if (message.isFromDave) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Dave AI", message.content)))
                                    }
                                },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(14.dp),
                                    tint = contentColor.copy(alpha = 0.4f)
                                )
                            }
                            
                            Spacer(Modifier.width(4.dp))

                            val contextLocal = LocalContext.current
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message.content)
                                    }
                                    contextLocal.startActivity(Intent.createChooser(intent, "Share Dave's response"))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(14.dp),
                                    tint = contentColor.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
                
                if (message.isFromDave && message.actions.isNotEmpty()) {
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
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(action, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
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
            color = MaterialTheme.colorScheme.primaryContainer,
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
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
                kotlinx.coroutines.delay(index * 100L)
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
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
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

@Composable
fun DaveIsTypingIndicator() {
    val statuses = listOf(
        "PARSING_INTENT",
        "SYNCING_VAULT",
        "ANALYZING_CONTEXT",
        "SYNTHESIZING_RESPONSE",
        "OPTIMIZING_AURA",
        "QUERYING_MAINFRAME"
    )
    
    var currentStatusIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            currentStatusIndex = (currentStatusIndex + 1) % statuses.size
        }
    }

    Row(
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeuralCard(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 4.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeuralThinkingIndicator(modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(end = 16.dp)) {
                    Text(
                        text = "DAVE IS THINKING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "[ ${statuses[currentStatusIndex]} ]",
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
                modifier = Modifier.clickable { }
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
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(level / 100f).fillMaxHeight().background(if (level > 20) MaterialTheme.colorScheme.primary else Color.Red)
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
    memories: List<com.example.daveai.data.db.SemanticMemory>,
    onDismiss: () -> Unit,
    onAddEntry: (String, String) -> Unit,
    onDeleteEntry: (Long) -> Unit
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
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Text("Dave's Memory Vault", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text("Dave securely encrypts and stores facts about you here to be more helpful.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            memories.forEach { memory ->
                ListItem(
                    headlineContent = { Text(memory.memoryType.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                    supportingContent = { Text(memory.content, style = MaterialTheme.typography.bodyLarge) },
                    trailingContent = { 
                        IconButton(onClick = { onDeleteEntry(memory.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.graphicsLayer { alpha = 0.5f })
            }
            if (memories.isEmpty()) {
                Text("Dave hasn't learned anything yet. Start chatting!", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(24.dp))
            Text("Add Manual Entry", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = newKey, onValueChange = { newKey = it }, label = { Text("Fact Type") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(value = newValue, onValueChange = { newValue = it }, label = { Text("Fact") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            }
            Spacer(Modifier.height(16.dp))
            BouncyButton(
                onClick = {
                    if (newKey.isNotBlank() && newValue.isNotBlank()) {
                        onAddEntry(newKey, newValue)
                        newKey = ""
                        newValue = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Lock Into Memory") }
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
            if (trimmedLine.contains("|") && !trimmedLine.startsWith("```")) {
                inTable = true
                tableBuffer.add(trimmedLine)
            } else {
                if (inTable) {
                    if (tableBuffer.size >= 2) {
                        Spacer(Modifier.height(8.dp))
                        EliteDataGrid(tableBuffer.toList())
                        Spacer(Modifier.height(8.dp))
                    } else if (tableBuffer.size == 1) {
                        Text(text = tableBuffer.first(), color = contentColor, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.25.sp))
                    }
                    tableBuffer.clear()
                    inTable = false
                }
                if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")) {
                    EliteBulletPoint(trimmedLine.substring(2), contentColor)
                } else if (trimmedLine.isNotEmpty() && !trimmedLine.contains("---")) {
                    Text(text = trimmedLine, color = contentColor, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.25.sp))
                }
            }
        }
        if (inTable) {
            if (tableBuffer.size >= 2) {
                Spacer(Modifier.height(8.dp))
                EliteDataGrid(tableBuffer.toList())
            } else if (tableBuffer.size == 1) {
                Text(text = tableBuffer.first(), color = contentColor, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.25.sp))
            }
        }
    }
}

@Composable
fun EliteBulletPoint(text: String, color: Color) {
    Row(modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp).padding(top = 4.dp), tint = color.copy(alpha = 0.7f))
        Spacer(Modifier.width(8.dp))
        Text(text = text, color = color, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp))
    }
}

@Composable
fun EliteDataGrid(lines: List<String>) {
    val data = lines.asSequence().map { line ->
        line.split("|").asSequence().filter { it.isNotBlank() }.map { it.trim() }.toList()
    }.filter { it.isNotEmpty() && !it.all { cell -> cell.contains("-") } }.toList()
    if (data.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))) {
        data.forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth().background(if (rowIndex == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { cell ->
                    Text(text = cell, style = if (rowIndex == 0) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall, color = if (rowIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
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
