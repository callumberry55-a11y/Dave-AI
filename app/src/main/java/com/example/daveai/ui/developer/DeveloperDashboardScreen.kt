package com.example.daveai.ui.developer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Input
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.components.NeuralTopBar
import com.example.daveai.data.db.SecurityEvent
import com.example.daveai.data.db.ChatMessageEntity
import java.text.SimpleDateFormat
import java.util.*

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
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("OVERVIEW", "USERS", "MONITORING", "LOGS", "FIRESTORE")

    Scaffold(
        topBar = {
            NeuralTopBar(
                title = "DEVELOPER DASHBOARD",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                isProactive = true
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black,
                contentColor = Color(0xFF00E676),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF00E676)
                    )
                },
                divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.1f)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                when (selectedTab) {
                    0 -> OverviewTab(totalUsers, allUsers)
                    1 -> UsersTab(allUsers, onElevate = viewModel::elevateUser, onDelete = viewModel::deleteUser)
                    2 -> MonitoringTab(inTokens, outTokens, recentUsage)
                    3 -> LogsTab(recentEvents, serverLogs, onClear = viewModel::clearLogs)
                    4 -> FirestoreTab(allUsers, globalStats)
                }
            }
        }
    }
}

@Composable
fun OverviewTab(totalUsers: Long, allUsers: List<Map<String, Any>>) {
    val operaUsers = allUsers.count { it["network"] == "Opera" }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DashboardCard(
            title = "SYSTEM STATUS",
            value = "OPERATIONAL",
            icon = Icons.Rounded.Verified,
            color = Color(0xFF00E676)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = "TOTAL USERS",
                value = totalUsers.toString(),
                icon = Icons.Rounded.Group,
                color = Color(0xFF2979FF)
            )
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = "OPERA NETWORK",
                value = operaUsers.toString(),
                icon = Icons.Rounded.Public,
                color = Color(0xFFFF5252)
            )
        }
        
        AnimatedVisibility(visible = operaUsers > 0) {
            val lastOperaFeedback = allUsers.find { it["network"] == "Opera" }?.get("feedback") as? String ?: "No feedback"
            Surface(
                color = Color(0xFFFF5252).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = Color(0xFFFF5252))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("REAL-TIME FEEDBACK (OPERA)", color = Color(0xFFFF5252), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(lastOperaFeedback, color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "MAINFRAME TELEMETRY",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
        
        // Mock Telemetry Data
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFF111111), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TelemetryRow("Database Latency", "12ms", 0.1f)
                TelemetryRow("Neural Response", "850ms", 0.6f)
                TelemetryRow("Vanguard Uptime", "99.99%", 0.95f)
            }
        }
    }
}

@Composable
fun TelemetryRow(label: String, value: String, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(120.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(4.dp),
            color = Color(0xFF00E676),
            trackColor = Color.White.copy(alpha = 0.05f)
        )
        Text(value, color = Color(0xFF00E676), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun UsersTab(users: List<Map<String, Any>>, onElevate: (String) -> Unit, onDelete: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(users) { user ->
            val uid = user["uid"] as? String ?: ""
            val email = user["email"] as? String ?: "Anonymous"
            val role = user["role"] as? String ?: "Explorer"
            val lastLogin = user["lastLogin"] as? com.google.firebase.Timestamp
            
            Surface(
                color = Color(0xFF111111),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (role == "Master Developer") Color(0xFF00E676).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(email, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Role: $role", color = if (role == "Master Developer") Color(0xFF00E676) else Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        lastLogin?.let {
                            val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(it.toDate())
                            Text("Last seen: $date", color = Color.White.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Row {
                        if (role != "Master Developer") {
                            IconButton(onClick = { onElevate(uid) }) {
                                Icon(Icons.Rounded.Shield, contentDescription = "Elevate", tint = Color(0xFF00E676))
                            }
                        } else {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = "Verified", tint = Color(0xFF00E676), modifier = Modifier.size(24.dp).align(Alignment.CenterVertically))
                        }
                        IconButton(onClick = { onDelete(uid) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonitoringTab(inTokens: Long, outTokens: Long, recentUsage: List<ChatMessageEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("AI RESOURCE CONSUMPTION", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = "INPUT TOKENS",
                value = String.format(Locale.US, "%,d", inTokens),
                icon = Icons.AutoMirrored.Rounded.Input,
                color = Color(0xFF00E676)
            )
            DashboardCard(
                modifier = Modifier.weight(1f),
                title = "OUTPUT TOKENS",
                value = String.format(Locale.US, "%,d", outTokens),
                icon = Icons.Rounded.Output,
                color = Color(0xFF2979FF)
            )
        }

        // Token Trend Graph
        Surface(
            color = Color(0xFF111111),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("TOKEN TREND (LAST 50 EVENTS)", color = Color.White, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(16.dp))
                
                val points = recentUsage.map { (it.inputTokens + it.outputTokens).toFloat() }.reversed()
                if (points.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        val path = Path()
                        val stepX = size.width / (points.size.coerceAtLeast(2) - 1)
                        val maxY = points.maxOrNull()?.coerceAtLeast(100f) ?: 100f
                        
                        points.forEachIndexed { index, value ->
                            val x = index * stepX
                            val y = size.height - (value / maxY * size.height)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        
                        drawPath(path, color = Color(0xFF00E676), style = Stroke(width = 2.dp.toPx()))
                        
                        // Reference lines
                        repeat(4) { i ->
                            val y = size.height * (i / 3f)
                            drawLine(Color.White.copy(alpha = 0.1f), start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y))
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No usage data yet.", color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun LogsTab(securityEvents: List<SecurityEvent>, serverLogs: List<String>, onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Server Logs Card (New)
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = Color(0xFF0A0A0A),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF2979FF).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("REAL-TIME SERVER DUMP", color = Color(0xFF2979FF), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = Color(0xFF2979FF).copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(serverLogs.reversed()) { log ->
                        Text(
                            text = log,
                            color = Color(0xFFB0BEC5),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }

        // Security Events Card
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            color = Color(0xFF0A0A0A),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFFFAB40).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("SECURITY PROTOCOL EVENTS", color = Color(0xFFFFAB40), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) {
                        Text("CLEAR", color = Color.Red, fontSize = 10.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(securityEvents) { event ->
                        val date = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp))
                        Text(
                            text = "[$date] ${event.eventType}",
                            color = when (event.severity) {
                                "ERROR" -> Color.Red
                                "WARNING" -> Color(0xFFFFAB40)
                                else -> Color(0xFF00E676)
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FirestoreTab(users: List<Map<String, Any>>, globalStats: Map<String, Any>) {
    var selectedPath by remember { mutableStateOf("/users") }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedPath == "/users",
                onClick = { selectedPath = "/users" },
                label = { Text("users") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF00E676))
            )
            FilterChip(
                selected = selectedPath == "/stats/global",
                onClick = { selectedPath = "/stats/global" },
                label = { Text("stats/global") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF00E676))
            )
        }

        Text("FIRESTORE EXPLORER :: $selectedPath", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedPath == "/users") {
                items(users) { user ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            user.toString().replace(", ", ",\n "),
                            color = Color(0xFF00E676),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            globalStats.toString().replace(", ", ",\n "),
                            color = Color(0xFF00E676),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF111111),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}
