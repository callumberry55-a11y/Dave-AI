package com.example.daveai.ui.widgets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.unit.ColorProvider

object DaveWidgetTheme {
    val Gold = ColorProvider(Color(0xFFFFB300))
    val DarkBg = ColorProvider(Color(0xFF121212))
    val CardBg = ColorProvider(Color(0xFF1E1E1E))
    val TextPrimary = ColorProvider(Color.White)
    val TextSecondary = ColorProvider(Color(0xFF888888))

    val EliteModifier = GlanceModifier
        .background(DarkBg)
        .cornerRadius(16.dp)
}
