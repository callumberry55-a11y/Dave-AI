package com.example.daveai.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.daveai.DaveApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Periodically triggers Dave's proactive reasoning process in the background.
 * This allows Dave to monitor market data, user interests, and relationship arcs autonomously.
 */
class DaveAutonomousWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("DaveAutonomousWorker", "Initiating background agentic cycle...")
            val app = applicationContext as DaveApplication
            val repository = app.chatRepository
            
            // Execute Dave's proactive "thought" process
            repository.executeAutonomousThought()
            
            Result.success()
        } catch (e: Exception) {
            Log.e("DaveAutonomousWorker", "Background agentic cycle failed: ${e.message}", e)
            Result.retry()
        }
    }
}
