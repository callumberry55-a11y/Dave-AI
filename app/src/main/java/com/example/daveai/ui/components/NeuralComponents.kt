package com.example.daveai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.theme.glassBlack
import com.example.daveai.ui.theme.glassBlackBorder
import com.example.daveai.ui.theme.glassWhite
import com.example.daveai.ui.theme.glassWhiteBorder

val LocalCyberIntensity = compositionLocalOf { 0.8f }
val LocalGlowStrength = compositionLocalOf { 0.5f }
val LocalBlurIntensity = compositionLocalOf { 0.5f }

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
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            readOnly = readOnly,
            enabled = enabled,
            singleLine = singleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
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
    val bgColor = if (isDark) glassBlack else glassWhite
    val borderColor = if (isDark) glassBlackBorder else glassWhiteBorder

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
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = borderColor
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
    val glowStrength = LocalGlowStrength.current
    
    val baseBgColor = containerColor ?: if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.03f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    val highlightColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.5f)
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val infiniteTransition = rememberInfiniteTransition(label = "neural_glow")
    
    // Entrance Animation
    var entranceTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entranceTrigger = true }
    val entranceAlpha by animateFloatAsState(
        targetValue = if (entranceTrigger) 1f else 0f,
        animationSpec = tween(600, easing = LinearOutSlowInEasing),
        label = "entranceAlpha"
    )
    val entranceScale by animateFloatAsState(
        targetValue = if (entranceTrigger) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "entranceScale"
    )

    val glowOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowOffset"
    )

    // Scanline Animation
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlineY"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = entranceAlpha
                scaleX = entranceScale
                scaleY = entranceScale
                shadowElevation = if (isGodMode) 20f else 8f
                spotShadowColor = primaryColor.copy(alpha = 0.2f)
                ambientShadowColor = Color.Black.copy(alpha = 0.1f)
            }
            .clip(shape)
            .background(baseBgColor)
            .drawWithContent {
                drawContent()
                
                // 1. Subtle Frosted Noise Texture
                val noiseAlpha = (if (isDark) 0.02f else 0.04f) * cyberIntensity
                repeat(15) {
                    drawCircle(
                        color = Color.White.copy(alpha = noiseAlpha),
                        radius = (1..3).random().toFloat(),
                        center = Offset(
                            (0..size.width.toInt()).random().toFloat(),
                            (0..size.height.toInt()).random().toFloat()
                        )
                    )
                }

                // 2. Layered Inner Glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.05f * cyberIntensity * glowStrength), Color.Transparent),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.maxDimension * 0.7f
                    )
                )

                // 3. Dynamic Neural Glow
                val glowAlpha = (if (isGodMode) 0.25f else 0.1f) * cyberIntensity * glowStrength * 1.5f
                if (glowAlpha > 0.001f) {
                    val brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, primaryColor.copy(alpha = glowAlpha), Color.Transparent),
                        start = Offset(glowOffset, 0f),
                        end = Offset(glowOffset + 400f, 400f)
                    )
                    drawRect(brush = brush)
                }

                // 4. Moving Scanline
                val scanLineAlpha = 0.05f * cyberIntensity
                val currentScanY = size.height * scanlineY
                drawLine(
                    color = primaryColor.copy(alpha = scanLineAlpha),
                    start = Offset(0f, currentScanY),
                    end = Offset(size.width, currentScanY),
                    strokeWidth = 2f
                )
            }
            // Inner Highlight Border (Rim Light)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    0.0f to highlightColor.copy(alpha = highlightColor.alpha * cyberIntensity),
                    0.5f to Color.Transparent,
                    1.0f to borderColor.copy(alpha = borderColor.alpha * cyberIntensity),
                    start = Offset.Zero,
                    end = Offset.Infinite
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
    val infiniteTransition = rememberInfiniteTransition(label = "neural_pulse")
    val cyberIntensity = LocalCyberIntensity.current
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        // Orbiting particles
        repeat(3) { index ->
            val orbitalRotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000 + (index * 500), easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "orbitalRotation$index"
            )
            val orbitalScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "orbitalScale$index"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = orbitalRotation + (index * 120f)
                        scaleX = orbitalScale
                        scaleY = orbitalScale
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color.copy(alpha = 0.8f * cyberIntensity), CircleShape)
                        .border(0.5.dp, color.copy(alpha = 0.3f), CircleShape)
                )
            }
        }

        // Core Pulse
        val coreScale by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "coreScale"
        )
        
        Box(
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = coreScale
                    scaleY = coreScale
                    rotationZ = -rotation
                }
                .background(
                    brush = Brush.sweepGradient(
                        listOf(color.copy(alpha = 0.1f), color, color.copy(alpha = 0.1f))
                    ),
                    shape = CircleShape
                )
                .border(1.dp, color.copy(alpha = 0.5f * cyberIntensity), CircleShape)
        )
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

@Composable
fun StructuredContent(text: String, contentColor: Color) {
    val lines = text.split("\n")
    var inTable = false
    val tableBuffer = mutableListOf<String>()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.contains("|") && !trimmedLine.startsWith("```")) {
                inTable = true
                tableBuffer.add(trimmedLine)
            } else {
                if (inTable) {
                    if (tableBuffer.size >= 2) {
                        Spacer(Modifier.height(8.dp))
                        EliteDataGrid(tableBuffer.toList())
                        Spacer(Modifier.height(8.dp))
                    } else if (tableBuffer.size == 1) {
                        Text(text = tableBuffer.first(), color = contentColor, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.25.sp))
                    }
                    tableBuffer.clear()
                    inTable = false
                }
                if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")) {
                    EliteBulletPoint(trimmedLine.substring(2), contentColor)
                } else if (trimmedLine.isNotEmpty() && !trimmedLine.contains("---")) {
                    Text(text = trimmedLine, color = contentColor, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.25.sp))
                }
            }
        }
        if (inTable) {
            if (tableBuffer.size >= 2) {
                Spacer(Modifier.height(8.dp))
                EliteDataGrid(tableBuffer.toList())
            } else if (tableBuffer.size == 1) {
                Text(text = tableBuffer.first(), color = contentColor, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.25.sp))
            }
        }
    }
}

@Composable
fun EliteBulletPoint(text: String, color: Color) {
    Row(modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp).padding(top = 4.dp), tint = color.copy(alpha = 0.7f))
        Spacer(Modifier.width(8.dp))
        Text(text = text, color = color, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp))
    }
}

@Composable
fun EliteDataGrid(lines: List<String>) {
    val data = lines.asSequence().map { line ->
        line.split("|").asSequence().filter { it.isNotBlank() }.map { it.trim() }.toList()
    }.filter { it.isNotEmpty() && !it.all { cell -> cell.contains("-") } }.toList()
    if (data.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))) {
        data.forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth().background(if (rowIndex == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { cell ->
                    Text(text = cell, style = if (rowIndex == 0) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall, color = if (rowIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                }
            }
            if (rowIndex < data.lastIndex) {
                HorizontalDivider(modifier = Modifier.graphicsLayer { alpha = 0.3f })
            }
        }
    }
}
