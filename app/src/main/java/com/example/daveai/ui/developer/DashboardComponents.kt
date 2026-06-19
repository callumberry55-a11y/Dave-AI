package com.example.daveai.ui.developer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.theme.GhostWhite
import com.example.daveai.ui.theme.NeonEmerald
import com.example.daveai.ui.theme.ObsidianSurface

@Composable
fun ModernDashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ObsidianSurface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title.uppercase(),
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                value,
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun CyberGraph(
    points: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val width = size.width
            val height = size.height
            
            // Draw grid
            val gridLines = 6
            repeat(gridLines) { i ->
                val x = width * (i / (gridLines - 1).toFloat())
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }
            repeat(4) { i ->
                val y = height * (i / 3f)
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            if (points.isNotEmpty()) {
                val path = Path()
                val stepX = width / (points.size.coerceAtLeast(2) - 1)
                val maxY = points.maxOrNull()?.coerceAtLeast(10f) ?: 10f
                
                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - (value / maxY * height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                
                // Area fill
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
            }
        }
    }
}

@Composable
fun SystemHeartbeat() {
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(NeonEmerald, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
                .border(1.dp, NeonEmerald, CircleShape)
        )
    }
}

@Composable
fun NeuralBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "neural_bg")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Background particles
        repeat(15) { i ->
            val x = (width * (0.2f + 0.6f * ((i * 73L) % 100 / 100f) + 0.1f * kotlin.math.sin(time * 2 * Math.PI + i).toFloat()))
            val y = (height * (0.2f + 0.6f * ((i * 37L) % 100 / 100f) + 0.1f * kotlin.math.cos(time * 2 * Math.PI + i).toFloat()))
            
            drawCircle(
                color = NeonEmerald.copy(alpha = 0.05f),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
            
            // Connections
            if (i > 0) {
                val prevX = (width * (0.2f + 0.6f * (((i - 1) * 73L) % 100 / 100f) + 0.1f * kotlin.math.sin(time * 2 * Math.PI + (i - 1)).toFloat()))
                val prevY = (height * (0.2f + 0.6f * (((i - 1) * 37L) % 100 / 100f) + 0.1f * kotlin.math.cos(time * 2 * Math.PI + (i - 1)).toFloat()))
                
                drawLine(
                    color = NeonEmerald.copy(alpha = 0.02f),
                    start = Offset(x, y),
                    end = Offset(prevX, prevY),
                    strokeWidth = 1f
                )
            }
        }
    }
}

@Composable
fun MatrixLogItem(log: String, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally() + fadeIn()
    ) {
        Row(modifier = Modifier.padding(vertical = 2.dp)) {
            androidx.compose.material3.Text(
                text = ">",
                color = NeonEmerald,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.Text(
                text = log,
                color = GhostWhite.copy(alpha = 0.8f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}
