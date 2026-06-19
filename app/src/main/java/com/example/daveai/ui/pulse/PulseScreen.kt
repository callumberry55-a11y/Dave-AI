package com.example.daveai.ui.pulse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralTopBar
import com.example.daveai.ui.theme.ObsidianDeep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // In a real scenario, these would come from a repository/DB populated by WorkManager
    val mockBriefing = remember {
        listOf(
            PulseItem("Weather", "Current: 22°C. Clear skies. Perfect for neural processing.", Icons.Rounded.Cloud, Color(0xFF00D2FF)),
            PulseItem("Headlines", "Tech breakthroughs in on-device AI acceleration reported today.", Icons.Rounded.Newspaper, Color(0xFF00FF88)),
            PulseItem("System", "Dave's core is at 100% capacity. All services operational.", Icons.Rounded.Timeline, Color(0xFFA64DFF))
        )
    }

    Scaffold(
        topBar = {
            NeuralTopBar(
                title = "DAILY PULSE",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack
            )
        },
        containerColor = ObsidianDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                "MORNING BRIEFING :: STATUS OPTIMIZED",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockBriefing) { item ->
                    PulseCard(item)
                }
            }
        }
    }
}

data class PulseItem(
    val title: String,
    val content: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)

@Composable
fun PulseCard(item: PulseItem) {
    NeuralCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(item.icon, contentDescription = null, tint = item.accentColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    item.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = item.accentColor,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    item.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
