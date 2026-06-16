package com.example.daveai.service

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.daveai.DaveApplication
import com.example.daveai.data.db.DaveDatabase
import com.example.daveai.data.db.NotificationEntity
import com.example.daveai.data.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DaveNotificationService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val lastReplyTimestamps = mutableMapOf<String, Long>()
    private val cooldownMs = 60_000L // 1 minute

    override fun onCreate() {
        super.onCreate()
        Log.d("DaveNotification", "Service Created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val notification = sbn?.notification ?: return
        val extras = notification.extras
        val title = extras.getString("android.title") ?: "No Title"
        val text = extras.getCharSequence("android.text")?.toString() ?: "No Text"
        val pkg = sbn.packageName

        if (pkg != packageName) {
            Log.d("DaveNotification", "Notification from $pkg: $title - $text")
            
            val app = applicationContext as DaveApplication
            val chatRepository = app.chatRepository
            val settingsRepository = app.settingsRepository

            serviceScope.launch {
                // Save to DB
                val db = DaveDatabase.getDatabase(this@DaveNotificationService)
                db.notificationDao().insert(
                    NotificationEntity(
                        packageName = pkg,
                        title = title,
                        text = text
                    )
                )

                // Check if auto-reply is enabled
                if (settingsRepository.isAutoReplyEnabled.first()) {
                    attemptAutoReply(sbn, chatRepository)
                }
            }
        }
    }

    private fun attemptAutoReply(sbn: StatusBarNotification, chat: ChatRepository) {
        val notification = sbn.notification
        val actions = notification.actions ?: return
        val extras = notification.extras
        val title = extras.getString("android.title") ?: "Someone"
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        // Skip if text is empty
        if (text.isBlank()) return

        // Rate limiting: 1 reply per 60 seconds per package/sender
        val replyKey = "${sbn.packageName}_$title"
        val now = System.currentTimeMillis()
        val lastTime = lastReplyTimestamps[replyKey] ?: 0L
        if (now - lastTime < cooldownMs) {
            Log.d("DaveNotification", "Rate limiting auto-reply for $replyKey. Time remaining: ${(cooldownMs - (now - lastTime)) / 1000}s")
            return
        }

        var replyAction: Notification.Action? = null
        var remoteInput: RemoteInput? = null

        for (action in actions) {
            val ris = action.remoteInputs ?: continue
            for (ri in ris) {
                if (ri.allowFreeFormInput) {
                    replyAction = action
                    remoteInput = ri
                    break
                }
            }
            if (replyAction != null) break
        }

        if (replyAction != null && remoteInput != null) {
            serviceScope.launch {
                Log.d("DaveNotification", "Generating auto-reply for message from $title...")
                val prompt = "The user received a message from '$title': '$text'. Generate a very brief, helpful auto-reply for them as their AI assistant Dave. Keep it under 15 words. Respond ONLY with the reply text."
                
                try {
                    val replyText = chat.sendMessage(
                        sessionId = "auto_reply_${sbn.packageName}",
                        userContent = prompt,
                        isGhostMode = true,
                        bypassIntercept = true,
                        muteVoice = true
                    )

                    if (replyText.isNotBlank() && !replyText.startsWith("Error:")) {
                        // Update timestamp BEFORE sending to prevent race conditions during long API calls
                        lastReplyTimestamps[replyKey] = System.currentTimeMillis()

                        val intent = Intent()
                        val bundle = Bundle()
                        bundle.putCharSequence(remoteInput.resultKey, replyText)
                        RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)
                        
                        replyAction.actionIntent.send(this@DaveNotificationService, 0, intent)
                        Log.d("DaveNotification", "Auto-replied to ${sbn.packageName} with: $replyText")
                    }
                } catch (e: Exception) {
                    Log.e("DaveNotification", "Auto-reply generation failed", e)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
