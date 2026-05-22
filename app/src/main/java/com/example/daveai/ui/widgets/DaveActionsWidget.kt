package com.example.daveai.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import com.example.daveai.MainActivity
import com.example.daveai.R
import com.example.daveai.ui.assistant.AssistantActivity

class DaveActionsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ActionsContent()
        }
    }

    @Composable
    private fun ActionsContent() {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(DaveWidgetTheme.DarkBg)
                .cornerRadius(50.dp) // Sleek Pill Shape
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ActionItem(R.drawable.ic_widget_mic, actionStartActivity<AssistantActivity>())
            Spacer(GlanceModifier.width(16.dp))
            ActionItem(R.drawable.ic_widget_chat, actionStartActivity<MainActivity>())
            Spacer(GlanceModifier.width(16.dp))
            ActionItem(R.drawable.ic_widget_settings, actionStartActivity<MainActivity>())
        }
    }

    @Composable
    private fun ActionItem(iconRes: Int, action: androidx.glance.action.Action) {
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .background(DaveWidgetTheme.CardBg)
                .cornerRadius(24.dp)
                .clickable(action),
            contentAlignment = Alignment.Center
        ) {
            androidx.glance.Image(
                provider = androidx.glance.ImageProvider(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(DaveWidgetTheme.Gold),
                modifier = GlanceModifier.size(24.dp)
            )
        }
    }
}
