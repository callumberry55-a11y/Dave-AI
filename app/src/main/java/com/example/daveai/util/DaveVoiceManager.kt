package com.example.daveai.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.daveai.BuildConfig
import com.example.daveai.data.network.OpenAiApiService
import com.example.daveai.data.network.TtsRequest
import com.example.daveai.data.network.ElevenLabsApiService
import com.example.daveai.data.repository.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Manages Dave's vocal engine, optimized for consistent, fluid reading using OpenAI TTS.
 */
class DaveVoiceManager(
    private val context: Context,
    private val openAiService: OpenAiApiService,
    private val elevenLabsService: ElevenLabsApiService,
    private val settingsRepository: SettingsRepository
) {
    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaPlayer: MediaPlayer? = null
    private val audioQueue = ConcurrentLinkedQueue<File>()
    
    @Volatile
    private var isPlayingQueue: Boolean = false
    
    @Volatile
    private var isFetching: Boolean = false
    
    private var speakJob: Job? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    var onStartSpeaking: (() -> Unit)? = null
    var onDoneSpeaking: (() -> Unit)? = null
    var onErrorSpeaking: (() -> Unit)? = null

    suspend fun speak(text: String, speed: Double = 1.05) = withContext(Dispatchers.IO) {
        val userOpenAiKey = settingsRepository.userOpenAiApiKey.firstOrNull()
        val openAiKey = if (!userOpenAiKey.isNullOrBlank()) userOpenAiKey else BuildConfig.OPENAI_API_KEY
        
        if (openAiKey.isBlank()) {
            Log.w("DaveVoice", "OpenAI API Key is missing. Dave is silent.")
            return@withContext
        }

        Log.d("DaveVoice", "Initiating vocal sequence: \"${text.take(20)}...\"")
        
        // Stop current sequence
        stop()

        val chunks = splitIntoChunks(text)
        if (chunks.isEmpty()) return@withContext

        speakJob = managerScope.launch(Dispatchers.IO) {
            isFetching = true
            try {
                // Map chunks to Deferred to preserve order while allowing parallel fetch
                val deferredChunks = chunks.map { chunk ->
                    async {
                        fetchWithRetry(chunk, openAiKey, speed)
                    }
                }

                // Process them in order
                deferredChunks.forEachIndexed { index, deferred ->
                    val tempFile = deferred.await()
                    if (tempFile != null) {
                        Log.d("DaveVoice", "Chunk $index ready, adding to queue.")
                        audioQueue.offer(tempFile)
                        
                        // Kickstart playback if it's not currently active
                        synchronized(this@DaveVoiceManager) {
                            if (mediaPlayer == null) {
                                Log.d("DaveVoice", "Player idle, kickstarting for chunk $index")
                                managerScope.launch(Dispatchers.Main) {
                                    playNextInQueue()
                                }
                            }
                        }
                    } else {
                        Log.e("DaveVoice", "Chunk $index failed to fetch.")
                    }
                }
            } finally {
                isFetching = false
                Log.d("DaveVoice", "All chunks fetched. Remaining in queue: ${audioQueue.size}")
                // Final check to ensure we don't end early
                synchronized(this@DaveVoiceManager) {
                    if (mediaPlayer == null && audioQueue.isNotEmpty()) {
                        managerScope.launch(Dispatchers.Main) {
                            playNextInQueue()
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchWithRetry(chunk: String, apiKey: String, speed: Double, retries: Int = 2): File? {
        var attempt = 0
        while (attempt <= retries) {
            try {
                val response = openAiService.generateSpeech(
                    auth = "Bearer $apiKey",
                    request = TtsRequest(
                        input = chunk,
                        speed = speed,
                        voice = "alloy",
                        responseFormat = "opus"
                    )
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val tempFile = File.createTempFile("dave_vocal_${System.currentTimeMillis()}", ".ogg", context.cacheDir)
                        FileOutputStream(tempFile).use { output ->
                            body.byteStream().use { input ->
                                input.copyTo(output)
                            }
                        }
                        return tempFile
                    }
                } else {
                    Log.e("DaveVoice", "OpenAI TTS error (${response.code()}): ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("DaveVoice", "Fetch error (Attempt ${attempt + 1})", e)
            }
            attempt++
            if (attempt <= retries) delay(400) 
        }
        return null
    }

    private fun splitIntoChunks(text: String): List<String> {
        val baseChunks = text.split(Regex("(?<=[.!?\n])\\s+")).filter { it.isNotBlank() }
        val result = mutableListOf<String>()
        
        for (chunk in baseChunks) {
            if (chunk.length > 250) { 
                val subChunks = chunk.split(Regex("(?<=[,;:])\\s+")).filter { it.isNotBlank() }
                for (sub in subChunks) {
                    if (sub.length > 250) {
                        val words = sub.split(" ")
                        val sb = StringBuilder()
                        for (word in words) {
                            if ((sb.length + word.length) > 250) {
                                result.add(sb.toString().trim())
                                sb.clear()
                            }
                            sb.append(word).append(" ")
                        }
                        if (sb.isNotBlank()) result.add(sb.toString().trim())
                    } else {
                        result.add(sub)
                    }
                }
            } else {
                result.add(chunk)
            }
        }
        return result
    }

    private fun playNextInQueue() {
        synchronized(this) {
            val nextFile = audioQueue.poll()
            if (nextFile != null) {
                Log.d("DaveVoice", "Playing next chunk. Queue size: ${audioQueue.size}")
                val wasActive = isPlayingQueue
                isPlayingQueue = true
                _isSpeaking.value = true
                
                if (!wasActive) {
                    onStartSpeaking?.invoke()
                }
                
                mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(nextFile.absolutePath)
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                        setAudioAttributes(audioAttributes)
                        
                        setOnPreparedListener { 
                            it.start()
                        }
                        
                        setOnCompletionListener { 
                            cleanupAndNext(it, nextFile)
                        }
                        
                        setOnErrorListener { mp, what, extra -> 
                            Log.e("DaveVoice", "MediaPlayer error: $what, $extra")
                            cleanupAndNext(mp, nextFile)
                            true
                        }
                        
                        prepareAsync()
                    } catch (e: Exception) {
                        Log.e("DaveVoice", "MediaPlayer setup failed", e)
                        cleanupAndNext(this, nextFile)
                    }
                }
            } else {
                // Queue is empty. Are we still fetching?
                if (isFetching) {
                    Log.d("DaveVoice", "Queue empty but still fetching. Waiting...")
                    mediaPlayer = null
                    // We don't set isPlayingQueue to false here because we expect more
                    return
                }
                
                Log.d("DaveVoice", "Sequence complete.")
                isPlayingQueue = false
                _isSpeaking.value = false
                mediaPlayer = null
                onDoneSpeaking?.invoke()
            }
        }
    }

    private fun cleanupAndNext(mp: MediaPlayer, file: File) {
        synchronized(this) {
            try {
                mp.release()
                if (mediaPlayer == mp) mediaPlayer = null
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                Log.e("DaveVoice", "Cleanup error", e)
            }
            playNextInQueue()
        }
    }

    fun stop() {
        synchronized(this) {
            Log.d("DaveVoice", "Stopping all vocal activity.")
            speakJob?.cancel()
            speakJob = null
            isFetching = false
            isPlayingQueue = false
            _isSpeaking.value = false
            
            mediaPlayer?.let {
                try {
                    it.release()
                } catch (_: Exception) {}
            }
            mediaPlayer = null
            
            while (audioQueue.isNotEmpty()) {
                audioQueue.poll()?.delete()
            }
        }
    }

    fun destroy() {
        stop()
        managerScope.cancel()
    }
}
