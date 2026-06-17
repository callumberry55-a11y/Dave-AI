package com.example.daveai.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.daveai.BuildConfig
import com.example.daveai.data.network.ElevenLabsApiService
import com.example.daveai.data.network.OpenAiApiService
import com.example.daveai.data.network.TtsRequest
import com.example.daveai.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Manages Dave's vocal output using OpenAI TTS.
 * Handles chunking, parallel pre-fetching, and sequential playback.
 */
class DaveVoiceManager(
    private val context: Context,
    private val openAiService: OpenAiApiService,
    private val elevenLabsService: ElevenLabsApiService, // Kept for DI compatibility but unused
    private val settingsRepository: SettingsRepository
) {
    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mediaPlayer: MediaPlayer? = null
    private val audioQueue = ConcurrentLinkedQueue<File>()
    private var isPlayingQueue = false
    private var isFetching = false
    private var speakJob: Job? = null
    
    private val _isSpeaking = MutableStateFlow(value = false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    var onStartSpeaking: (() -> Unit)? = null
    var onDoneSpeaking: (() -> Unit)? = null
    var onErrorSpeaking: (() -> Unit)? = null

    init {
        Log.d("DaveVoice", "Neural Vocal Core Initialized :: OpenAI Preferred")
    }

    suspend fun speak(text: String, speed: Double = 1.05) = withContext(Dispatchers.IO) {
        val userOpenAiKey = settingsRepository.userOpenAiApiKey.firstOrNull()
        val openAiKey = if (!userOpenAiKey.isNullOrBlank()) userOpenAiKey else BuildConfig.OPENAI_API_KEY
        
        if (openAiKey.isBlank()) {
            Log.w("DaveVoice", "OpenAI API Key is missing. Dave is silent.")
            return@withContext
        }

        // Cancel any previous vocal sequence
        stop()

        // Granular chunker - split by punctuation
        val chunks = splitIntoChunks(text)

        speakJob = managerScope.launch {
            isFetching = true
            try {
                // Parallel fetching for performance
                chunks.map { chunk ->
                    async {
                        try {
                            val response = openAiService.generateSpeech(
                                auth = "Bearer $openAiKey",
                                request = TtsRequest(
                                    input = chunk, 
                                    speed = speed, 
                                    voice = "alloy",
                                    responseFormat = "opus" // Opus is fast and high quality
                                )
                            )

                            if (response.isSuccessful) {
                                val body = response.body()
                                if (body != null) {
                                    // Use .ogg extension for Opus format (standard container)
                                    val tempFile = File.createTempFile("dave_vocal", ".ogg", context.cacheDir)
                                    FileOutputStream(tempFile).use { output ->
                                        body.byteStream().use { input ->
                                            input.copyTo(output)
                                        }
                                    }
                                    return@async tempFile
                                }
                            } else {
                                Log.e("DaveVoice", "OpenAI TTS failed: ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            Log.e("DaveVoice", "Vocal synth error for chunk: $chunk", e)
                        }
                        null
                    }
                }.forEach { deferred ->
                    val tempFile = deferred.await()
                    if (tempFile != null) {
                        audioQueue.offer(tempFile)
                        
                        // Kickstart playback if idle
                        if (!isPlayingQueue) {
                            withContext(Dispatchers.Main) {
                                playNextInQueue()
                            }
                        }
                    }
                }
            } finally {
                isFetching = false
                // Final check to ensure the queue completes
                if (!isPlayingQueue && audioQueue.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        playNextInQueue()
                    }
                }
            }
        }
    }

    private fun splitIntoChunks(text: String): List<String> {
        val baseChunks = text.split(Regex("(?<=[.!?\n])\\s+")).filter { it.isNotBlank() }
        val result = mutableListOf<String>()
        for (chunk in baseChunks) {
            if (chunk.length > 300) {
                val subChunks = chunk.split(Regex("(?<=[,;:])\\s+")).filter { it.isNotBlank() }
                for (sub in subChunks) {
                    if (sub.length > 300) {
                        val words = sub.split(" ")
                        val sb = StringBuilder()
                        for (word in words) {
                            if (sb.length + word.length > 300) {
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
        val nextFile = audioQueue.poll()
        if (nextFile != null) {
            val wasPlaying = isPlayingQueue
            isPlayingQueue = true
            _isSpeaking.value = true
            
            if (!wasPlaying) {
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
                        it.release()
                        if (mediaPlayer == it) mediaPlayer = null
                        try { nextFile.delete() } catch (_: Exception) {}
                        playNextInQueue()
                    }
                    
                    setOnErrorListener { mp, what, extra -> 
                        Log.e("DaveVoice", "MediaPlayer error: $what, $extra")
                        mp.release()
                        if (mediaPlayer == mp) mediaPlayer = null
                        try { nextFile.delete() } catch (_: Exception) {}
                        playNextInQueue()
                        true
                    }
                    
                    prepareAsync()
                } catch (e: Exception) {
                    Log.e("DaveVoice", "MediaPlayer config failed", e)
                    playNextInQueue()
                }
            }
        } else {
            if (isFetching) {
                isPlayingQueue = false
                return
            }

            val wasPlaying = isPlayingQueue
            isPlayingQueue = false
            _isSpeaking.value = false
            
            if (wasPlaying) {
                onDoneSpeaking?.invoke()
            }
        }
    }

    fun stop() {
        val wasPlaying = isPlayingQueue || _isSpeaking.value
        speakJob?.cancel()
        isFetching = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        audioQueue.clear()
        isPlayingQueue = false
        _isSpeaking.value = false
        
        if (wasPlaying) {
            onDoneSpeaking?.invoke()
        }
    }

    fun destroy() {
        stop()
    }
}
