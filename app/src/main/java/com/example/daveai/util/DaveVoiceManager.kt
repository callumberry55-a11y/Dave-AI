package com.example.daveai.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.daveai.BuildConfig
import com.example.daveai.data.network.OpenAiApiService
import com.example.daveai.data.network.TtsRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentLinkedQueue

class DaveVoiceManager(
    private val context: Context,
    private val openAiService: OpenAiApiService,
) {
    private var mediaPlayer: MediaPlayer? = null
    private val audioQueue = ConcurrentLinkedQueue<File>()
    private var isPlayingQueue = false
    private var scopeJob: Job? = null
    
    private val _isSpeaking = MutableStateFlow(value = false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    suspend fun speak(text: String) = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        if (apiKey.isBlank()) return@withContext

        // Cancel any previous job if we are interrupted
        stop()

        // Simple sentence chunker: split on punctuation followed by space
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }

        scopeJob = CoroutineScope(Dispatchers.IO).launch {
            for (sentence in sentences) {
                try {
                    val response = openAiService.generateSpeech(
                        auth = "Bearer $apiKey",
                        request = TtsRequest(input = sentence)
                    )

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val tempFile = File.createTempFile("dave_voice_chunk", ".mp3", context.cacheDir)
                            FileOutputStream(tempFile).use { output ->
                                body.byteStream().use { input ->
                                    input.copyTo(output)
                                }
                            }
                            
                            audioQueue.offer(tempFile)
                            
                            // If not already playing, start the queue immediately
                            if (!isPlayingQueue) {
                                withContext(Dispatchers.Main) {
                                    playNextInQueue()
                                }
                            }
                        }
                    } else {
                        Log.e("DaveVoice", "TTS failed: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("DaveVoice", "TTS error", e)
                }
            }
        }
    }

    private fun playNextInQueue() {
        val nextFile = audioQueue.poll()
        if (nextFile != null) {
            isPlayingQueue = true
            _isSpeaking.value = true
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(nextFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener { 
                    it.release()
                    if (mediaPlayer == it) mediaPlayer = null
                    // Delete temp file after playing
                    try { nextFile.delete() } catch (_: Exception) {}
                    
                    playNextInQueue()
                }
            }
        } else {
            // Queue is empty
            isPlayingQueue = false
            _isSpeaking.value = false
        }
    }

    fun stop() {
        scopeJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        audioQueue.clear()
        isPlayingQueue = false
        _isSpeaking.value = false
    }
}
