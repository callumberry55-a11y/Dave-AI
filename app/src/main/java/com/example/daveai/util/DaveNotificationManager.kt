package com.example.daveai.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import com.example.daveai.ChatRoomActivity
import com.example.daveai.MainActivity
import com.example.daveai.R
import com.example.daveai.receiver.NotificationReplyReceiver

class DaveNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "dave_chat_channel"
    private val defaultChannelId = "default_notification_channel"

    init {
        createNotificationChannel()
        createDefaultAlertChannel()
    }

    private fun createNotificationChannel() {
        val name = "Dave AI Chat"
        val descriptionText = "Notifications for Dave's elite responses"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
            setAllowBubbles(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createDefaultAlertChannel() {
        val name = "Default Alerts"
        val descriptionText = "System and network level alerts for Dave AI"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(defaultChannelId, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showDaveResponse(sessionId: String, message: String) {
        val shortcutId = "dave_chat_$sessionId"
        pushConversationShortcut(sessionId, message, shortcutId)

        val replyLabel = "Reply to Dave..."
        val remoteInput = RemoteInput.Builder(NotificationReplyReceiver.KEY_TEXT_REPLY)
            .setLabel(replyLabel)
            .build()

        val replyIntent = Intent(context, NotificationReplyReceiver::class.java).apply {
            putExtra("sessionId", sessionId)
        }
        
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId.hashCode(),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

        val action = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Reply",
            replyPendingIntent,
        ).addRemoteInput(remoteInput).build()

        // Content Intent
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("sessionId", sessionId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            sessionId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Bubble Intent
        val bubbleIntent = Intent(context, ChatRoomActivity::class.java).apply {
            putExtra("sessionId", sessionId)
        }
        val bubblePendingIntent = PendingIntent.getActivity(
            context,
            sessionId.hashCode(),
            bubbleIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val bubbleData = NotificationCompat.BubbleMetadata.Builder(
            bubblePendingIntent,
            IconCompat.createWithResource(context, R.mipmap.ic_launcher_round)
        )
            .setDesiredHeight(600)
            .setAutoExpandBubble(false)
            .setSuppressNotification(true)
            .build()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Dave AI")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(contentPendingIntent)
            .addAction(action)
            .setBubbleMetadata(bubbleData)
            .setShortcutId(shortcutId)
            .setLocusId(androidx.core.content.LocusIdCompat(shortcutId))
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(sessionId.hashCode(), notification)
    }

    fun showScanningProgress(sessionId: String, title: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText("Dave is processing...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .build()

        notificationManager.notify(sessionId.hashCode(), notification)
    }

    fun dismissNotification(sessionId: String) {
        notificationManager.cancel(sessionId.hashCode())
    }

    fun showGenericNotification(title: String, body: String) {
        val notification = android.app.Notification.Builder(context, defaultChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun pushConversationShortcut(sessionId: String, lastMessage: String, shortcutId: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("sessionId", sessionId)
        }

        val shortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel("Dave Chat")
            .setLongLabel(lastMessage.take(20) + "...")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher_round))
            .setIntent(intent)
            .setLongLived(true)
            .build()

        androidx.core.content.pm.ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }
}
