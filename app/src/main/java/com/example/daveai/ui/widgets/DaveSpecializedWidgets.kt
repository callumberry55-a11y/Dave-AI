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
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.daveai.MainActivity

class DaveFlashlightWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            EliteButtonWidget("🔦", "BEAM")
        }
    }
}

class DaveHardwareWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            EliteButtonWidget("⚡", "SYSTEM")
        }
    }
}

class DaveSongWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            EliteButtonWidget("🎸", "FORGE")
        }
    }
}

class DaveHistoryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            EliteButtonWidget("📜", "LOGS")
        }
    }
}

@Composable
private fun EliteButtonWidget(icon: String, label: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(DaveWidgetTheme.DarkBg)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(44.dp)
                .background(DaveWidgetTheme.CardBg)
                .cornerRadius(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = icon, style = TextStyle(fontSize = 20.sp))
        }
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = label, 
            style = TextStyle(
                fontSize = 9.sp, 
                fontWeight = FontWeight.Bold,
                color = DaveWidgetTheme.Gold
            )
        )
    }
}
