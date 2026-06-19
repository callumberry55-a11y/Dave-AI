package com.example.daveai.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.daveai.R
import com.example.daveai.ui.assistant.AssistantActivity
import kotlinx.coroutines.flow.first

class DaveMasterWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = (context.applicationContext as DaveApplication).chatRepository
        val assistant = repo.getDeviceAssistant()
        val battery = assistant.getBatteryLevel()
        val connection = assistant.getConnectivityStatus()
        
        val conversations = repo.allConversations.first()
        val latestConv = conversations.firstOrNull()
        val latestMessage = latestConv?.let { repo.getMessagesForConversation(it.id).first().lastOrNull() }
        
        provideContent {
            MasterContent(latestMessage?.content, battery, connection)
        }
    }

    @Composable
    private fun MasterContent(latestDaveMessage: String?, battery: Int, connection: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .then(DaveWidgetTheme.EliteModifier)
                .background(DaveWidgetTheme.DarkBg)
                .padding(12.dp),
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
                    text = "🔋 $battery% | $connection",
                    style = TextStyle(fontSize = 10.sp, color = DaveWidgetTheme.TextSecondary)
                )
            }
            
            Spacer(GlanceModifier.height(12.dp))

            // Latest Message Card
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(DaveWidgetTheme.CardBg)
                    .cornerRadius(12.dp)
                    .clickable(actionStartActivity<AssistantActivity>())
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "system@dave_ai: ~$",
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
                        style = TextStyle(
                            fontSize = 11.sp, 
                            color = DaveWidgetTheme.TextPrimary
                        ),
                    )
                }
            }

            Spacer(GlanceModifier.height(12.dp))

            // Asymmetrical Action Grid
            Row(modifier = GlanceModifier.fillMaxSize()) {
                // Left Column: Massive Voice Bounding Box
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .defaultWeight()
                        .background(DaveWidgetTheme.CardBg)
                        .cornerRadius(16.dp)
                        .clickable(actionStartActivity<AssistantActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.glance.Image(
                            provider = androidx.glance.ImageProvider(R.drawable.ic_widget_mic),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(DaveWidgetTheme.Gold),
                            modifier = GlanceModifier.size(36.dp)
                        )
                        Spacer(GlanceModifier.height(8.dp))
                        Text(
                            text = "VOICE", 
                            style = TextStyle(
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                color = DaveWidgetTheme.TextSecondary
                            )
                        )
                    }
                }

                Spacer(GlanceModifier.width(8.dp))

                // Right Column: Compact 2x2 Grid
                Column(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .defaultWeight()
                ) {
                    Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                        MasterItem(R.drawable.ic_widget_chat, "CHAT", actionStartActivity<MainActivity>())
                        Spacer(GlanceModifier.width(8.dp))
                        MasterItem(R.drawable.ic_widget_music, "FORGE", actionStartActivity<MainActivity>())
                    }
                    Spacer(GlanceModifier.height(8.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                        MasterItem(R.drawable.ic_widget_brain, "RIDDLE", actionStartActivity<MainActivity>())
                        Spacer(GlanceModifier.width(8.dp))
                        MasterItem(R.drawable.ic_widget_settings, "SYSTEM", actionStartActivity<MainActivity>())
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.MasterItem(iconRes: Int, label: String, action: androidx.glance.action.Action) {
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .background(DaveWidgetTheme.CardBg)
                .cornerRadius(12.dp)
                .clickable(action),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.glance.Image(
                    provider = androidx.glance.ImageProvider(iconRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(DaveWidgetTheme.TextPrimary),
                    modifier = GlanceModifier.size(20.dp)
                )
                Spacer(GlanceModifier.height(4.dp))
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
}
