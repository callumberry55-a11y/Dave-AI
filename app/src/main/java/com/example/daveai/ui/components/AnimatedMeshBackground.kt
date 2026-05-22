package com.example.daveai.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedMeshBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00E676), // Default DaveGreen
    useSystemWallpaper: Boolean = false,
    customWallpaperUri: String? = null,
    animationSpeed: Float = 1.0f,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val duration = (20000 / animationSpeed).toInt().coerceIn(1000, 60000)
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val backgroundAlpha = if (useSystemWallpaper || customWallpaperUri != null) 0.15f else 1f
    val blobAlphaMultiplier = if (useSystemWallpaper || customWallpaperUri != null) 0.4f else 1f

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = backgroundAlpha))) {
        if (customWallpaperUri != null) {
            AsyncImage(
                model = customWallpaperUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.6f
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val angle = time * 2f * Math.PI.toFloat()

            // Derived colors
            val color1 = primaryColor
            val color2 = Color(
                red = (primaryColor.red * 0.8f).coerceIn(0f, 1f),
                green = (primaryColor.green * 0.9f).coerceIn(0f, 1f),
                blue = (primaryColor.blue * 0.7f).coerceIn(0f, 1f),
                alpha = 1f
            )

            // Blob 1: Primary
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1.copy(alpha = 0.3f * blobAlphaMultiplier), Color.Transparent),
                    center = Offset(
                        x = (canvasWidth / 2f) + ((canvasWidth / 3f) * cos(angle)),
                        y = (canvasHeight / 2f) + ((canvasHeight / 4f) * sin(angle))
                    ),
                    radius = canvasWidth * 0.8f
                ),
                radius = canvasWidth,
                center = Offset(
                    x = (canvasWidth / 2f) + ((canvasWidth / 3f) * cos(angle)),
                    y = (canvasHeight / 2f) + ((canvasHeight / 4f) * sin(angle))
                )
            )

            // Blob 2: Derived secondary
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2.copy(alpha = 0.2f * blobAlphaMultiplier), Color.Transparent),
                    center = Offset(
                        x = (canvasWidth / 2f) + ((canvasWidth / 4f) * sin(angle * 1.5f)),
                        y = (canvasHeight / 2f) + ((canvasHeight / 3f) * cos(angle * 0.5f))
                    ),
                    radius = canvasWidth * 0.7f
                ),
                radius = canvasWidth,
                center = Offset(
                    x = (canvasWidth / 2f) + ((canvasWidth / 4f) * sin(angle * 1.5f)),
                    y = (canvasHeight / 2f) + ((canvasHeight / 3f) * cos(angle * 0.5f))
                )
            )

            // Blob 3: Accent
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.1f * blobAlphaMultiplier), Color.Transparent),
                    center = Offset(
                        x = (canvasWidth / 2f) + ((canvasWidth / 2.5f) * cos(angle * 0.7f + 1f)),
                        y = (canvasHeight / 2f) + ((canvasHeight / 2.5f) * sin(angle * 1.2f - 2f))
                    ),
                    radius = canvasWidth * 0.9f
                ),
                radius = canvasWidth,
                center = Offset(
                    x = (canvasWidth / 2f) + ((canvasWidth / 2.5f) * cos(angle * 0.7f + 1f)),
                    y = (canvasHeight / 2f) + ((canvasHeight / 2.5f) * sin(angle * 1.2f - 2f))
                )
            )
        }
        content()
    }
}
