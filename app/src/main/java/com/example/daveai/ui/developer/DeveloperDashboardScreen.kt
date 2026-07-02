package com.example.daveai.ui.developer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Input
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Output
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.data.db.ChatMessageEntity
import com.example.daveai.data.db.SecurityEvent
import com.example.daveai.ui.components.NeuralTopBar
import com.example.daveai.ui.theme.GhostWhite
import com.example.daveai.ui.theme.NeonEmerald
import com.example.daveai.ui.theme.ObsidianSurface
import com.example.daveai.ui.theme.PulseCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeveloperDashboardScreen(
    viewModel: DeveloperDashboardViewModel,
    onBack: () -> Unit
) {
    val totalUsers by viewModel.totalUsersCount.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()
    val serverLogs by viewModel.serverLogs.collectAsState()
    val inTokens by viewModel.totalInputTokens.collectAsState()
    val outTokens by viewModel.totalOutputTokens.collectAsState()
    val recentUsage by viewModel.recentUsage.collectAsState()
    val globalStats by viewModel.globalStats.collectAsState()
    val thoughts by viewModel.consciousnessStream.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("OVERVIEW", "USERS", "MONITORING", "THOUGHTS", "LOGS", "FIRESTORE")

    Scaffold(
        topBar = {
            NeuralTopBar(
                title = "DEVELOPER DASHBOARD",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                isProactive = true
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NeuralBackground()
            
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = NeonEmerald,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonEmerald
                        )
                    },
                    divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.05f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Normal,
                                    letterSpacing = 1.sp,
                                    color = if (selectedTab == index) NeonEmerald else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp)) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        },
                        label = "tab_content"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> OverviewTab(totalUsers, allUsers)
                            1 -> UsersTab(allUsers, onElevate = viewModel::elevateUser, onDelete = viewModel::deleteUser)
                            2 -> MonitoringTab(inTokens, outTokens, recentUsage)
                            3 -> ThoughtsTab(thoughts)
                            4 -> LogsTab(recentEvents, serverLogs, onClear = viewModel::clearLogs)
                            5 -> FirestoreTab(allUsers, globalStats)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThoughtsTab(thoughts: List<com.example.daveai.data.model.Thought>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(thoughts) { thought ->
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (thought.type) {
                                        com.example.daveai.data.model.ThoughtType.REFLECTION -> DavePurple
                                        com.example.daveai.data.model.ThoughtType.PLANNING -> DaveBlue
                                        com.example.daveai.data.model.ThoughtType.EMOTION -> Color(0xFFE53935)
                                        else -> NeonEmerald
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            thought.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(thought.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun OverviewTab(totalUsers: Long, allUsers: List<Map<String, Any>>) {
    val operaUsers = allUsers.count { it["network"] == "Opera" }
    
    var entranceTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entranceTrigger = true }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        AnimatedVisibility(
            visible = entranceTrigger,
            enter = slideInVertically { -20 } + fadeIn(tween(600))
        ) {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SystemHeartbeat()
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("SYSTEM STATUS", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
                        Text("OPERATIONAL :: VANGUARD_SYNC_ACTIVE", color = NeonEmerald, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AnimatedVisibility(
                visible = entranceTrigger,
                enter = slideInVertically { 20 } + fadeIn(tween(600, delayMillis = 100)),
                modifier = Modifier.weight(1f)
            ) {
                ModernDashboardCard(
                    title = "Total Users",
                    value = totalUsers.toString(),
                    icon = Icons.Rounded.Group,
                    color = PulseCyan
                )
            }
            AnimatedVisibility(
                visible = entranceTrigger,
                enter = slideInVertically { 20 } + fadeIn(tween(600, delayMillis = 200)),
                modifier = Modifier.weight(1f)
            ) {
                ModernDashboardCard(
                    title = "Opera Grid",
                    value = operaUsers.toString(),
                    icon = Icons.Rounded.Public,
                    color = Color(0xFFFF5252)
                )
            }
        }
        
        AnimatedVisibility(
            visible = operaUsers > 0 && entranceTrigger,
            enter = expandVertically() + fadeIn(tween(600, delayMillis = 300)),
            exit = shrinkVertically() + fadeOut()
        ) {
            val lastOperaFeedback = allUsers.find { it["network"] == "Opera" }?.get("feedback") as? String ?: "No feedback"
            Surface(
                color = Color(0xFFFF5252).copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = Color(0xFFFF5252))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("REAL-TIME FEEDBACK (OPERA SOURCE)", color = Color(0xFFFF5252), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                        Text(lastOperaFeedback, color = GhostWhite, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = entranceTrigger,
            enter = fadeIn(tween(600, delayMillis = 400))
        ) {
            Text(
                "MAINFRAME TELEMETRY",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
        
        AnimatedVisibility(
            visible = entranceTrigger,
            enter = slideInVertically { 40 } + fadeIn(tween(600, delayMillis = 500))
        ) {
            Surface(
                color = ObsidianSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernTelemetryRow("Mainframe Latency", "12ms", 0.1f, NeonEmerald)
                    ModernTelemetryRow("Neural Link Response", "850ms", 0.6f, PulseCyan)
                    ModernTelemetryRow("Vanguard Integrity", "99.99%", 0.95f, NeonEmerald)
                }
            }
        }
    }
}

@Composable
fun ThoughtsTab(thoughts: List<com.example.daveai.data.model.Thought>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(thoughts) { thought ->
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (thought.type) {
                                        com.example.daveai.data.model.ThoughtType.REFLECTION -> DavePurple
                                        com.example.daveai.data.model.ThoughtType.PLANNING -> DaveBlue
                                        com.example.daveai.data.model.ThoughtType.EMOTION -> Color(0xFFE53935)
                                        else -> NeonEmerald
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            thought.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(thought.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ModernTelemetryRow(label: String, value: String, progress: Float, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, color = GhostWhite.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            Text(value, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = color,
            trackColor = Color.White.copy(alpha = 0.05f)
        )
    }
}

@Composable
fun ThoughtsTab(thoughts: List<com.example.daveai.data.model.Thought>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(thoughts) { thought ->
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (thought.type) {
                                        com.example.daveai.data.model.ThoughtType.REFLECTION -> DavePurple
                                        com.example.daveai.data.model.ThoughtType.PLANNING -> DaveBlue
                                        com.example.daveai.data.model.ThoughtType.EMOTION -> Color(0xFFE53935)
                                        else -> NeonEmerald
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            thought.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(thought.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun UsersTab(users: List<Map<String, Any>>, onElevate: (String) -> Unit, onDelete: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        itemsIndexed(users) { index, user ->
            var entranceTrigger by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(index * 100L)
                entranceTrigger = true
            }

            AnimatedVisibility(
                visible = entranceTrigger,
                enter = slideInHorizontally { -40 } + fadeIn(tween(600))
            ) {
                val uid = user["uid"] as? String ?: ""
                val email = user["email"] as? String ?: "Anonymous"
                val role = user["role"] as? String ?: "Explorer"
                val lastLogin = user["lastLogin"] as? com.google.firebase.Timestamp
                
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (role == "Master Developer") NeonEmerald.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).background(if (role == "Master Developer") NeonEmerald.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (role == "Master Developer") Icons.Rounded.Security else Icons.Rounded.Person,
                                contentDescription = null,
                                tint = if (role == "Master Developer") NeonEmerald else GhostWhite.copy(alpha = 0.5f)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(email, color = GhostWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(role.uppercase(), color = if (role == "Master Developer") NeonEmerald else PulseCyan, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            lastLogin?.let {
                                val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(it.toDate())
                                Text("LAST ACTIVE: $date", color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row {
                            if (role != "Master Developer") {
                                IconButton(onClick = { onElevate(uid) }) {
                                    Icon(Icons.Rounded.Shield, contentDescription = "Elevate", tint = NeonEmerald)
                                }
                            }
                            IconButton(onClick = { onDelete(uid) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThoughtsTab(thoughts: List<com.example.daveai.data.model.Thought>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(thoughts) { thought ->
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (thought.type) {
                                        com.example.daveai.data.model.ThoughtType.REFLECTION -> DavePurple
                                        com.example.daveai.data.model.ThoughtType.PLANNING -> DaveBlue
                                        com.example.daveai.data.model.ThoughtType.EMOTION -> Color(0xFFE53935)
                                        else -> NeonEmerald
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            thought.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(thought.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MonitoringTab(inTokens: Long, outTokens: Long, recentUsage: List<ChatMessageEntity>) {
    var entranceTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entranceTrigger = true }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        AnimatedVisibility(
            visible = entranceTrigger,
            enter = fadeIn(tween(600))
        ) {
            Text("AI RESOURCE CONSUMPTION", color = Color.White.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AnimatedVisibility(
                visible = entranceTrigger,
                enter = scaleIn(tween(600, delayMillis = 100)),
                modifier = Modifier.weight(1f)
            ) {
                ModernDashboardCard(
                    title = "Input Tokens",
                    value = String.format(Locale.US, "%,d", inTokens),
                    icon = Icons.AutoMirrored.Rounded.Input,
                    color = NeonEmerald
                )
            }
            AnimatedVisibility(
                visible = entranceTrigger,
                enter = scaleIn(tween(600, delayMillis = 200)),
                modifier = Modifier.weight(1f)
            ) {
                ModernDashboardCard(
                    title = "Output Tokens",
                    value = String.format(Locale.US, "%,d", outTokens),
                    icon = Icons.Rounded.Output,
                    color = PulseCyan
                )
            }
        }

        AnimatedVisibility(
            visible = entranceTrigger,
            enter = slideInVertically { 40 } + fadeIn(tween(600, delayMillis = 300))
        ) {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("TOKEN TREND (LAST 50 EVENTS)", color = GhostWhite, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(20.dp))
                    
                    val points = recentUsage.map { (it.inputTokens + it.outputTokens).toFloat() }.reversed()
                    if (points.isNotEmpty()) {
                        CyberGraph(points = points, color = NeonEmerald)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Text("WAITING FOR SIGNAL DATA...", color = Color.White.copy(alpha = 0.2f), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThoughtsTab(thoughts: List<com.example.daveai.data.model.Thought>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(thoughts) { thought ->
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (thought.type) {
                                        com.example.daveai.data.model.ThoughtType.REFLECTION -> DavePurple
                                        com.example.daveai.data.model.ThoughtType.PLANNING -> DaveBlue
                                        com.example.daveai.data.model.ThoughtType.EMOTION -> Color(0xFFE53935)
                                        else -> NeonEmerald
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            thought.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(thought.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LogsTab(securityEvents: List<SecurityEvent>, serverLogs: List<String>, onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Surface(
            modifier = Modifier.weight(1.2f).fillMaxWidth(),
            color = Color.Black,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PulseCyan.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("REAL-TIME SERVER DUMP", color = PulseCyan, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Icon(Icons.Rounded.Terminal, contentDescription = null, tint = PulseCyan.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(serverLogs.reversed()) { index, log ->
                        MatrixLogItem(log = log, index = index)
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = Color.Black,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFFAB40).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SECURITY PROTOCOL EVENTS", color = Color(0xFFFFAB40), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) {
                        Text("PURGE", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(securityEvents) { event ->
                        val date = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp))
                        Text(
                            text = "[$date] ${event.eventType}",
                            color = when (event.severity) {
                                "ERROR" -> Color.Red
                                "WARNING" -> Color(0xFFFFAB40)
                                else -> NeonEmerald
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThoughtsTab(thoughts: List<com.example.daveai.data.model.Thought>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(thoughts) { thought ->
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (thought.type) {
                                        com.example.daveai.data.model.ThoughtType.REFLECTION -> DavePurple
                                        com.example.daveai.data.model.ThoughtType.PLANNING -> DaveBlue
                                        com.example.daveai.data.model.ThoughtType.EMOTION -> Color(0xFFE53935)
                                        else -> NeonEmerald
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            thought.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(thought.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FirestoreTab(users: List<Map<String, Any>>, globalStats: Map<String, Any>) {
    var selectedPath by remember { mutableStateOf("/users") }
    
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModernFilterChip(
                selected = selectedPath == "/users",
                onClick = { selectedPath = "/users" },
                label = "USERS"
            )
            ModernFilterChip(
                selected = selectedPath == "/stats/global",
                onClick = { selectedPath = "/stats/global" },
                label = "GLOBAL STATS"
            )
        }

        Text("FIRESTORE EXPLORER :: $selectedPath", color = Color.White.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (selectedPath == "/users") {
                items(users) { user ->
                    FirestoreDataCard(content = user.toString())
                }
            } else {
                item {
                    FirestoreDataCard(content = globalStats.toString())
                }
            }
        }
    }
}

@Composable
fun ThoughtsTab(thoughts: List<com.example.daveai.data.model.Thought>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(thoughts) { thought ->
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (thought.type) {
                                        com.example.daveai.data.model.ThoughtType.REFLECTION -> DavePurple
                                        com.example.daveai.data.model.ThoughtType.PLANNING -> DaveBlue
                                        com.example.daveai.data.model.ThoughtType.EMOTION -> Color(0xFFE53935)
                                        else -> NeonEmerald
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            thought.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(thought.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ModernFilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    Surface(
        onClick = onClick,
        color = if (selected) NeonEmerald.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) NeonEmerald else Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selected) NeonEmerald else Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Normal
        )
    }
}

@Composable
fun ThoughtsTab(thoughts: List<com.example.daveai.data.model.Thought>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(thoughts) { thought ->
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (thought.type) {
                                        com.example.daveai.data.model.ThoughtType.REFLECTION -> DavePurple
                                        com.example.daveai.data.model.ThoughtType.PLANNING -> DaveBlue
                                        com.example.daveai.data.model.ThoughtType.EMOTION -> Color(0xFFE53935)
                                        else -> NeonEmerald
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            thought.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(thought.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FirestoreDataCard(content: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.05f))
    ) {
        Text(
            content.replace(", ", ",\n "),
            modifier = Modifier.padding(16.dp),
            color = NeonEmerald.copy(alpha = 0.8f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun ThoughtsTab(thoughts: List<com.example.daveai.data.model.Thought>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(thoughts) { thought ->
            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (thought.type) {
                                        com.example.daveai.data.model.ThoughtType.REFLECTION -> DavePurple
                                        com.example.daveai.data.model.ThoughtType.PLANNING -> DaveBlue
                                        com.example.daveai.data.model.ThoughtType.EMOTION -> Color(0xFFE53935)
                                        else -> NeonEmerald
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            thought.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(thought.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        thought.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
