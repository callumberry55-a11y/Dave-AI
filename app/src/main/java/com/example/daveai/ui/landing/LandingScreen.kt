package com.example.daveai.ui.landing

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.riddle.RiddleViewModel

@Composable
fun LandingScreen(
    riddleViewModel: RiddleViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToRiddle: () -> Unit,
    onNavigateToLessons: () -> Unit
) {
    val riddleState by riddleViewModel.uiState.collectAsState()
    val gold = Color(0xFFFFB300)
    val darkBg = Color(0xFF121212)
    val cardBg = Color(0xFF1E1E1E)

    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        
        // Header
        Column {
            Text(
                text = "Welcome back, boss",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "● DAVE IS READY",
                color = gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Central Aura Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing Aura
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(auraScale)
                    .clip(CircleShape)
                    .background(gold.copy(alpha = auraAlpha))
            )
            
            // Main Button
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToChat() },
                color = gold,
                shape = CircleShape,
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = "Tap to talk to Dave",
                        modifier = Modifier.size(48.dp),
                        tint = darkBg
                    )
                }
            }
        }

        // Grid
        Row(modifier = Modifier.fillMaxWidth()) {
            LandingCard(
                modifier = Modifier.weight(1f),
                title = "🧩 Vault",
                titleColor = gold,
                subtitle = "Streak: 🔥 ${riddleState.streak}\nProgress: ${riddleState.solvedCount}/${riddleState.totalCount}",
                onClick = onNavigateToRiddle,
                cardBg = cardBg
            )
            
            Spacer(Modifier.width(16.dp))

            LandingCard(
                modifier = Modifier.weight(1f),
                title = "📚 Lessons",
                titleColor = Color.White,
                subtitle = "Resume current\nuniversity module",
                onClick = onNavigateToLessons,
                cardBg = cardBg
            )
        }
    }
}

@Composable
private fun LandingCard(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color,
    subtitle: String,
    onClick: () -> Unit,
    cardBg: Color
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = Color(0xFF888888),
                fontSize = 13.sp
            )
        }
    }
}
