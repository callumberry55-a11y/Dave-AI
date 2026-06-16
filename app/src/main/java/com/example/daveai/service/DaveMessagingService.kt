package com.example.daveai.service

import android.util.Log
import com.example.daveai.data.repository.UserStatsRepository
import com.example.daveai.util.DaveNotificationManager
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

        val notificationManager = DaveNotificationManager(applicationContext)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("DaveMessaging", "Message data payload: ${remoteMessage.data}")
            // Handle specific agentic triggers from the cloud brain here
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("DaveMessaging", "Message Notification Body: ${it.body}")
            notificationManager.showGenericNotification(
                it.title ?: "Dave AI Alert",
                it.body ?: ""
            )
        }
    }
}
