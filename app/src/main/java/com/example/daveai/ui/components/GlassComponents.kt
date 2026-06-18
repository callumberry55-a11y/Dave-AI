package com.example.daveai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.daveai.ui.theme.glassBlack
import com.example.daveai.ui.theme.glassBlackBorder

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.border(0.5.dp, glassBlackBorder, shape),
        shape = shape,
        color = containerColor ?: glassBlack,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    containerColor: Color? = null,
    contentColor: Color = Color.White,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.border(0.5.dp, glassBlackBorder, shape),
        shape = shape,
        color = (containerColor ?: glassBlack).copy(alpha = if (enabled) 1f else 0.5f),
        contentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}
