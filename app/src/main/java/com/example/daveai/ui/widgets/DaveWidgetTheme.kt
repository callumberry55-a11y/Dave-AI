package com.example.daveai.ui.widgets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.unit.ColorProvider

object DaveWidgetTheme {
    val Gold = ColorProvider(color = Color(0xFFFFD700))
    val DarkBg = ColorProvider(color = Color(0xFF0A0214))
    val CardBg = ColorProvider(color = Color(0xFF1E0B36))
    val AccentPurple = ColorProvider(color = Color(0xFF4A148C))
    val TextPrimary = ColorProvider(color = Color(0xFFFFFFFF))
    val TextSecondary = ColorProvider(color = Color(0xFFAAAAAA))

    val EliteModifier = GlanceModifier
        .cornerRadius(24.dp)
}
