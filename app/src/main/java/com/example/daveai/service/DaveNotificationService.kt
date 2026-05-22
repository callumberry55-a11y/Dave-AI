package com.example.daveai.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.daveai.data.db.DaveDatabase
import com.example.daveai.data.db.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DaveNotificationService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val notification = sbn?.notification ?: return
        val extras = notification.extras
        val title = extras.getString("android.title") ?: "No Title"
        val text = extras.getCharSequence("android.text")?.toString() ?: "No Text"
        val pkg = sbn.packageName

        if (pkg != packageName) {
            Log.d("DaveNotification", "Notification from $pkg: $title - $text")
            
            serviceScope.launch {
                val db = DaveDatabase.getDatabase(this@DaveNotificationService)
                db.notificationDao().insert(
                    NotificationEntity(
                        packageName = pkg,
                        title = title,
                        text = text
                    )
                )
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
