package com.example.daveai.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.daveai.BuildConfig
import com.example.daveai.data.network.OpenAiApiService
import com.example.daveai.data.network.TtsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DaveVoiceManager(
    private val context: Context,
    private val openAiService: OpenAiApiService,
) {
    private var mediaPlayer: MediaPlayer? = null

    suspend fun speak(text: String) = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        if (apiKey.isBlank()) return@withContext

        try {
            val response = openAiService.generateSpeech(
                auth = "Bearer $apiKey",
                request = TtsRequest(input = text)
            )

            if (response.isSuccessful) {
                val body = response.body() ?: return@withContext
                val tempFile = File.createTempFile("dave_voice", ".mp3", context.cacheDir)
                FileOutputStream(tempFile).use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    playAudio(tempFile)
                }
            } else {
                Log.e("DaveVoice", "TTS failed: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("DaveVoice", "TTS error", e)
        }
    }

    private fun playAudio(file: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            setOnCompletionListener { 
                it.release()
                if (mediaPlayer == it) mediaPlayer = null
            }
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
