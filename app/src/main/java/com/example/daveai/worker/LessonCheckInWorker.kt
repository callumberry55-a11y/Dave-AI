package com.example.daveai.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.daveai.DaveApplication

class LessonCheckInWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString("sessionId") ?: return Result.failure()
        val app = applicationContext as DaveApplication
        
        // Push a "ping" notification
        app.notificationManager.showDaveResponse(
            sessionId = sessionId,
            message = "Hey boss, just checking in on our lesson! Still there? 🎓"
        )
        
        return Result.success()
    }
}
