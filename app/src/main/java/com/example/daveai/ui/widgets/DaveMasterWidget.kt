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
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.daveai.DaveApplication
import com.example.daveai.MainActivity
import com.example.daveai.ui.assistant.AssistantActivity
import kotlinx.coroutines.flow.first

class DaveMasterWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = (context.applicationContext as DaveApplication).chatRepository
        val sessions = repo.allSessions.first()
        val latestSession = sessions.firstOrNull()
        val latestMessage = latestSession?.let { repo.getMessagesForSession(it.sessionId).first().lastOrNull() }
        
        provideContent {
            MasterContent(latestMessage?.content)
        }
    }

    @Composable
    private fun MasterContent(latestDaveMessage: String?) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(DaveWidgetTheme.DarkBg)
                .padding(12.dp)
                .cornerRadius(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(8.dp)
                        .background(DaveWidgetTheme.Gold)
                        .cornerRadius(4.dp)
                ) {}
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "DAVE ELITE COMMAND",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold, 
                        fontSize = 11.sp,
                        color = DaveWidgetTheme.Gold,
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "V4.7",
                    style = TextStyle(fontSize = 10.sp, color = DaveWidgetTheme.TextSecondary)
                )
            }
            
            Spacer(GlanceModifier.height(12.dp))

            // Latest Message Card
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(DaveWidgetTheme.CardBg)
                    .cornerRadius(16.dp)
                    .clickable(actionStartActivity<AssistantActivity>())
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "SYSTEM STATUS:",
                        style = TextStyle(
                            fontSize = 8.sp, 
                            fontWeight = FontWeight.Bold,
                            color = DaveWidgetTheme.Gold
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = latestDaveMessage ?: "Awaiting your command, boss. All systems nominal.",
                        maxLines = 2,
                        style = TextStyle(fontSize = 11.sp, color = DaveWidgetTheme.TextPrimary),
                    )
                }
            }

            Spacer(GlanceModifier.height(12.dp))

            // Action Grid
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    MasterItem("🎤", "VOICE", actionStartActivity<AssistantActivity>())
                    MasterItem("💬", "CHAT", actionStartActivity<MainActivity>())
                    MasterItem("🔦", "BEAM", actionStartActivity<MainActivity>())
                }
                Spacer(GlanceModifier.height(8.dp))
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    MasterItem("🎸", "FORGE", actionStartActivity<MainActivity>())
                    MasterItem("🧠", "RIDDLE", actionStartActivity<MainActivity>()) // Changed to Riddle
                    MasterItem("⚡", "STATUS", actionStartActivity<MainActivity>())
                }
            }
        }
    }

    @Composable
    private fun RowScope.MasterItem(icon: String, label: String, action: androidx.glance.action.Action) {
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(DaveWidgetTheme.CardBg)
                    .cornerRadius(12.dp)
                    .clickable(action),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = icon, style = TextStyle(fontSize = 20.sp))
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = label, 
                        style = TextStyle(
                            fontSize = 8.sp, 
                            fontWeight = FontWeight.Bold,
                            color = DaveWidgetTheme.TextPrimary
                        )
                    )
                }
            }
        }
    }
}
