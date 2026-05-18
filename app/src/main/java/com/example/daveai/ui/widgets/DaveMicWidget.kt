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
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.daveai.ui.assistant.AssistantActivity

class DaveMicWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MicWidgetContent()
        }
    }

    @Composable
    private fun MicWidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(DaveWidgetTheme.DarkBg)
                .cornerRadius(24.dp)
                .padding(8.dp)
                .clickable(actionStartActivity<AssistantActivity>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = GlanceModifier
                    .size(56.dp)
                    .background(DaveWidgetTheme.CardBg)
                    .cornerRadius(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎤",
                    style = TextStyle(fontSize = 28.sp)
                )
            }
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = "LISTEN",
                style = TextStyle(
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold,
                    color = DaveWidgetTheme.Gold
                )
            )
        }
    }
}
