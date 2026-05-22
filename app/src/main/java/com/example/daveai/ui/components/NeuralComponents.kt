package com.example.daveai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.theme.glassBlack
import com.example.daveai.ui.theme.glassBlackBorder
import com.example.daveai.ui.theme.glassWhite
import com.example.daveai.ui.theme.glassWhiteBorder

val LocalCyberIntensity = compositionLocalOf { 0.8f }

@Composable
fun NeuralTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val cyberIntensity = LocalCyberIntensity.current

    NeuralCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f * cyberIntensity)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            isError = isError,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun NeuralTopBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {},
    isProactive: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onNavigationClick != null && navigationIcon != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            imageVector = navigationIcon,
                            contentDescription = "Navigate",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.25f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                    
                    if (isProactive) {
                        val activeColor = MaterialTheme.colorScheme.primary
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(activeColor, CircleShape)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "PROACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = activeColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                actions()
            }
            HorizontalDivider(
                modifier = Modifier.graphicsLayer { alpha = 0.2f },
                color = if (isDark) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun NeuralCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    containerColor: Color? = null,
    isGodMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cyberIntensity = LocalCyberIntensity.current
    val baseBgColor = containerColor ?: if (isDark) glassBlack else glassWhite
    val borderColor = if (isDark) glassBlackBorder else glassWhiteBorder
    val highlightColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.4f)
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val infiniteTransition = rememberInfiniteTransition(label = "neural_glow")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowOffset"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(baseBgColor)
            .drawWithContent {
                drawContent()
                
                // 1. Subtle Glass Noise Texture
                val noiseAlpha = (if (isDark) 0.03f else 0.05f) * cyberIntensity
                if (noiseAlpha > 0.001f) {
                    repeat(20) {
                        drawCircle(
                            color = Color.White.copy(alpha = noiseAlpha),
                            radius = (2..5).random().toFloat(),
                            center = androidx.compose.ui.geometry.Offset(
                                (0..size.width.toInt()).random().toFloat(),
                                (0..size.height.toInt()).random().toFloat()
                            )
                        )
                    }
                }

                // 2. God Mode Neural Glow
                if (isGodMode || cyberIntensity > 0.9f) {
                    val glowAlpha = if (isGodMode) 0.25f else (cyberIntensity - 0.9f) * 0.5f
                    val glowBrush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, primaryColor.copy(alpha = glowAlpha), Color.Transparent),
                        start = androidx.compose.ui.geometry.Offset(glowOffset, 0f),
                        end = androidx.compose.ui.geometry.Offset(glowOffset + 400f, 400f)
                    )
                    clipRect {
                        drawRect(brush = glowBrush)
                    }
                }
            }
            // Inner Highlight Border (Rim Light)
            .border(
                width = 0.8.dp,
                brush = Brush.linearGradient(
                    0.0f to highlightColor.copy(alpha = highlightColor.alpha * cyberIntensity),
                    0.5f to Color.Transparent,
                    start = Offset.Zero,
                    end = Offset.Infinite
                ),
                shape = shape
            )
            // Outer Main Border
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = if (isGodMode || cyberIntensity > 0.7f) {
                        listOf(primaryColor.copy(alpha = 0.8f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.8f))
                    } else {
                        listOf(borderColor, Color.Transparent, borderColor)
                    }
                ),
                shape = shape
            )
    ) {
        content()
    }
}

@Composable
fun NeuralMetadataHeader(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.width(4.dp))
        Box(modifier = Modifier.size(width = 8.dp, height = 1.dp).background(color.copy(alpha = 0.3f)))
        Spacer(Modifier.width(4.dp))
        Text(
            text = "[ $value ]",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        )
    }
}

@Composable
fun NeuralThinkingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neural_thinking")
    
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner Core
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .background(color, CircleShape)
        )

        // Orbital Ring 1
        Box(
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { rotationZ = rotation1 }
                .border(1.5.dp, color.copy(alpha = 0.4f), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .align(Alignment.TopCenter)
                    .background(color, CircleShape)
            )
        }

        // Orbital Ring 2
        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer { rotationZ = rotation2 }
                .border(1.dp, color.copy(alpha = 0.2f), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .align(Alignment.BottomCenter)
                    .background(color.copy(alpha = 0.6f), CircleShape)
            )
        }
    }
}

@Composable
fun NeuralPulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "neural_pulse")
    val primaryColor = MaterialTheme.colorScheme.primary
    val cyberIntensity = LocalCyberIntensity.current
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        repeat(4) { index ->
            val delay = index * 200
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f * cyberIntensity,
                targetValue = 0.8f * cyberIntensity,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(0.5.dp, primaryColor.copy(alpha = 0.3f * cyberIntensity), CircleShape)
            )
        }
    }
}
