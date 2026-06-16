package com.example.daveai.service

import android.util.Log
import com.example.daveai.DaveApplication
import com.example.daveai.data.repository.UserStatsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles incoming FCM messages and token refreshes.
 * Ensures Dave can be reached remotely via the preferred network.
 */
class DaveMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userStatsRepository = UserStatsRepository()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("DaveMessaging", "Refreshed FCM Token: $token")
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            serviceScope.launch {
                userStatsRepository.saveFcmToken(currentUser.uid, token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("DaveMessaging", "From: ${remoteMessage.from}")

        val notificationManager = (application as DaveApplication).notificationManager

        // Check for both notification payload AND data payload (as fallback)
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]

        if (title != null || body != null) {
            Log.d("DaveMessaging", "Showing notification: $title - $body")
            notificationManager.showGenericNotification(
                title ?: "Dave AI Alert",
                body ?: ""
            )
        }

        // Handle raw data for agentic triggers
        if (remoteMessage.data.isNotEmpty() && !remoteMessage.data.containsKey("title")) {
            Log.d("DaveMessaging", "Message data payload (agentic): ${remoteMessage.data}")
            // Trigger background processing if specific keys exist
        }
    }
}
