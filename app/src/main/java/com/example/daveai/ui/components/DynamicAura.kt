package com.example.daveai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DynamicAura(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    intensity: Float = 1f,
    isSpeaking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSpeaking) 1.2f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 500 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    val reactiveOffset by animateOffsetAsState(
        targetValue = touchOffset,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "reactive"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        touchOffset = Offset(offset.x - size.width / 2, offset.y - size.height / 2)
                    }
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2) + (reactiveOffset * 0.2f)
            val radius = (size.minDimension / 3) * pulseScale

            // Draw multiple layers of neural rings
            for (i in 1..3) {
                val layerPhase = phase + (i * Math.PI.toFloat() / 2)
                val layerIntensity = intensity * (1f / i)
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.4f * layerIntensity), Color.Transparent),
                        center = center,
                        radius = radius * (1f + 0.2f * i)
                    ),
                    center = center,
                    radius = radius * (1f + 0.1f * i)
                )

                // Neural filaments
                val points = 60
                val angleStep = (2 * Math.PI / points).toFloat()
                for (j in 0 until points) {
                    val angle = j * angleStep
                    val noise = sin(angle * 5 + layerPhase) * 10f * intensity
                    val start = Offset(
                        center.x + (radius - 10 + noise) * cos(angle),
                        center.y + (radius - 10 + noise) * sin(angle)
                    )
                    val end = Offset(
                        center.x + (radius + 10 + noise) * cos(angle),
                        center.y + (radius + 10 + noise) * sin(angle)
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = 0.6f * layerIntensity),
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Central Core
            drawCircle(
                color = primaryColor,
                center = center,
                radius = radius * 0.1f
            )
        }
    }
}
