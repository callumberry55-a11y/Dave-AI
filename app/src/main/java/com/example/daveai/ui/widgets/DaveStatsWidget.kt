package com.example.daveai.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class DaveStatsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            StatsContent()
        }
    }

    @Composable
    private fun StatsContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(DaveWidgetTheme.DarkBg)
                .cornerRadius(24.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ELITE TELEMETRY",
                style = TextStyle(
                    fontWeight = FontWeight.Bold, 
                    fontSize = 11.sp,
                    color = DaveWidgetTheme.Gold,
                )
            )
            Spacer(GlanceModifier.height(12.dp))
            Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                StatItem("🔋", "JUICE", "85%")
                Spacer(GlanceModifier.width(12.dp))
                StatItem("⚡", "TPU", "LIVE")
            }
        }
    }

    @Composable
    private fun RowScope.StatItem(icon: String, label: String, value: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier
                .defaultWeight()
                .background(DaveWidgetTheme.CardBg)
                .cornerRadius(12.dp)
                .padding(8.dp)
        ) {
            Text(text = icon, style = TextStyle(fontSize = 18.sp))
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = value, 
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DaveWidgetTheme.TextPrimary
                )
            )
            Text(
                text = label, 
                style = TextStyle(
                    fontSize = 8.sp, 
                    fontWeight = FontWeight.Bold,
                    color = DaveWidgetTheme.TextSecondary
                )
            )
        }
    }
}
