package com.example.daveai.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.daveai.DaveApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DailyPulseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("DailyPulseWorker", "Generating morning pulse briefing...")
            val app = applicationContext as DaveApplication
            val repository = app.chatRepository
            val notificationManager = app.notificationManager

            // 1. Fetch data (Weather, News, etc.) - In a real app we'd call the APIs via repository
            // val weather = repository.getWeather(...)
            // val news = repository.getTopHeadlines(...)

            // 2. Dave processes the briefing
            val briefing = "Good morning! Dave here. Current temp is 22°C. Tech news is buzzing about on-device AI. Your neural core is primed for success today. ⚡️"

            // 3. Show notification
            notificationManager.showDaveResponse("daily_pulse", briefing)

            Result.success()
        } catch (e: Exception) {
            Log.e("DailyPulseWorker", "Failed to generate daily pulse", e)
            Result.retry()
        }
    }
}
