package com.example.daveai.ui.landing

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.theme.BetaAccent
import com.example.daveai.ui.theme.BetaBackground
import com.example.daveai.ui.theme.BetaPrimary
import com.example.daveai.ui.theme.BetaSecondary

@Composable
fun BetaLandingScreen(
    onNavigateToChat: () -> Unit,
    intelligenceVersion: String = "V13.1.1"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beta_aura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BetaBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DAVE BETA",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(BetaPrimary, BetaSecondary)
                    ),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            )
            
            Spacer(Modifier.height(8.dp))
            
            Surface(
                color = BetaAccent.copy(alpha = 0.1f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, BetaAccent.copy(alpha = 0.3f))
            ) {
                Text(
                    text = intelligenceVersion,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = BetaAccent
                )
            }

            Spacer(Modifier.height(64.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(BetaPrimary.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                )
                
                Surface(
                    onClick = onNavigateToChat,
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Brush.sweepGradient(listOf(BetaPrimary, BetaSecondary, BetaPrimary)))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.BlurOn,
                            contentDescription = "Initialize Beta",
                            modifier = Modifier.size(48.dp),
                            tint = BetaSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(64.dp))

            Text(
                text = "SYSTEM READY",
                style = MaterialTheme.typography.bodyLarge,
                color = BetaSecondary.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}
