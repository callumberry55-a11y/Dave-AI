package com.example.daveai.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.daveai.MainActivity
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
                .cornerRadius(24.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ActionItem("🎤", "LISTEN", actionStartActivity<AssistantActivity>())
            Spacer(GlanceModifier.width(8.dp))
            ActionItem("💬", "CHAT", actionStartActivity<MainActivity>())
            Spacer(GlanceModifier.width(8.dp))
            ActionItem("⚡", "SYSTEM", actionStartActivity<MainActivity>())
        }
    }

    @Composable
    private fun ActionItem(icon: String, label: String, action: androidx.glance.action.Action) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier
                .background(DaveWidgetTheme.CardBg)
                .cornerRadius(12.dp)
                .clickable(action)
                .padding(8.dp),
        ) {
            Text(text = icon, style = TextStyle(fontSize = 20.sp))
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = label, 
                style = TextStyle(
                    fontSize = 8.sp, 
                    fontWeight = FontWeight.Bold,
                    color = DaveWidgetTheme.Gold
                )
            )
        }
    }
}
