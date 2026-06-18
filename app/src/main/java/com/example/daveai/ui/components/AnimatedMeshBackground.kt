package com.example.daveai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedMeshBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00FF9D), // Liquid Emerald
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

    val backgroundAlpha = if (useSystemWallpaper || customWallpaperUri != null) 0.12f else 1f
    val blobAlphaMultiplier = if (useSystemWallpaper || customWallpaperUri != null) 0.3f else 0.8f

    val scope = rememberCoroutineScope()
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    var rippleCenter by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = backgroundAlpha))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    rippleCenter = offset
                    scope.launch {
                        rippleRadius.snapTo(0f)
                        rippleAlpha.snapTo(0.4f)
                        launch { rippleRadius.animateTo(size.width * 1.5f, tween(1200, easing = LinearOutSlowInEasing)) }
                        launch { rippleAlpha.animateTo(0f, tween(1200, easing = LinearOutSlowInEasing)) }
                    }
                }
            }
    ) {
        if (customWallpaperUri != null) {
            AsyncImage(
                model = customWallpaperUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val angle = time * 2f * Math.PI.toFloat()

            // Derived colors for Liquid Neural theme
            val color1 = primaryColor
            val color2 = Color(0xFF00D1FF) // Pulse Cyan
            val color3 = Color(0xFF9D00FF) // Electric Violet

            // Blob 1: Primary Emerald
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color1.copy(alpha = 0.3f * blobAlphaMultiplier), Color.Transparent),
                    center = Offset(
                        x = (canvasWidth / 2f) + ((canvasWidth / 3f) * cos(angle)),
                        y = (canvasHeight / 2f) + ((canvasHeight / 4f) * sin(angle))
                    ),
                    radius = canvasWidth * 1.3f
                ),
                radius = canvasWidth * 1.3f,
                center = Offset(
                    x = (canvasWidth / 2f) + ((canvasWidth / 3f) * cos(angle)),
                    y = (canvasHeight / 2f) + ((canvasHeight / 4f) * sin(angle))
                )
            )

            // Blob 2: Cyan Pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color2.copy(alpha = 0.25f * blobAlphaMultiplier), Color.Transparent),
                    center = Offset(
                        x = (canvasWidth / 2f) + ((canvasWidth / 4f) * sin(angle * 1.3f)),
                        y = (canvasHeight / 2f) + ((canvasHeight / 3f) * cos(angle * 0.7f))
                    ),
                    radius = canvasWidth * 1.1f
                ),
                radius = canvasWidth * 1.1f,
                center = Offset(
                    x = (canvasWidth / 2f) + ((canvasWidth / 4f) * sin(angle * 1.3f)),
                    y = (canvasHeight / 2f) + ((canvasHeight / 3f) * cos(angle * 0.7f))
                )
            )

            // Blob 3: Violet Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color3.copy(alpha = 0.2f * blobAlphaMultiplier), Color.Transparent),
                    center = Offset(
                        x = (canvasWidth / 2f) + ((canvasWidth / 2.5f) * cos(angle * 0.4f + 2f)),
                        y = (canvasHeight / 2f) + ((canvasHeight / 2.5f) * sin(angle * 1.6f - 1f))
                    ),
                    radius = canvasWidth * 0.9f
                ),
                radius = canvasWidth * 0.9f,
                center = Offset(
                    x = (canvasWidth / 2f) + ((canvasWidth / 2.5f) * cos(angle * 0.4f + 2f)),
                    y = (canvasHeight / 2f) + ((canvasHeight / 2.5f) * sin(angle * 1.6f - 1f))
                )
            )
            
            // Draw Interactive Ripple
            if (rippleAlpha.value > 0f && rippleRadius.value > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = rippleAlpha.value), Color.Transparent),
                        center = rippleCenter,
                        radius = rippleRadius.value
                    ),
                    radius = rippleRadius.value,
                    center = rippleCenter
                )
            }
        }
        content()
    }
}
