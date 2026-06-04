package com.example.daveai.agent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.daveai.DaveApplication
import com.example.daveai.util.DaveNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles agentic background actions initiated by Google Assistant or Android 16/17 OS integrations.
 * This allows users to query Dave AI or trigger hardware scans entirely in the background.
 */
class DaveAgentReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("DaveAgent", "Agentic Action Received: $action")

        val app = context.applicationContext as DaveApplication
        val chatRepository = app.chatRepository
        val notificationManager = DaveNotificationManager(context)

        // Process intents based on BII or custom deep link actions
        val messageQuery = intent.getStringExtra("messageQuery")
        val scanType = intent.getStringExtra("scanType")

        receiverScope.launch {
            try {
                if (messageQuery != null) {
                    Log.d("DaveAgent", "Processing background query: $messageQuery")
                    val sessionId = chatRepository.createNewSession("Agentic Task", "GENERAL")
                    
                    // Send to Dave in Ghost Mode so it doesn't clutter the main UI history
                    val response = chatRepository.sendMessage(
                        sessionId = sessionId,
                        userContent = messageQuery,
                        isGhostMode = false, // We actually want to notify the user!
                        bypassIntercept = false
                    )
                    
                    // Display Dave's response as a rich Bubble Notification
                    notificationManager.showDaveResponse(sessionId, response)
                    
                } else if (scanType != null) {
                    val status = "Scanned $scanType: System state is OPTIMIZED. ⚡️"
                    notificationManager.showDaveResponse("sys_scan", status)
                }
            } catch (e: Exception) {
                Log.e("DaveAgent", "Agentic execution failed", e)
            }
        }
    }
}
