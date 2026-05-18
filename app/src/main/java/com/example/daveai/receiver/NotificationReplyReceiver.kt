package com.example.daveai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.daveai.DaveApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReplyReceiver : BroadcastReceiver() {
    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val replyText = remoteInput?.getCharSequence(KEY_TEXT_REPLY)?.toString()
        val sessionId = intent.getStringExtra("sessionId")

        if ((replyText != null) && (sessionId != null)) {
            val repository = (context.applicationContext as DaveApplication).chatRepository
            CoroutineScope(Dispatchers.IO).launch {
                repository.sendMessage(
                    sessionId = sessionId,
                    userContent = replyText,
                    bypassIntercept = false,
                )
            }
        }
    }
}
