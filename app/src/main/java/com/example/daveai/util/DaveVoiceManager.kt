package com.example.daveai.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.daveai.BuildConfig
import com.example.daveai.data.network.ElevenLabsApiService
import com.example.daveai.data.network.ElevenLabsTtsRequest
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

class DaveVoiceManager(
    private val context: Context,
    private val openAiService: OpenAiApiService,
    private val elevenLabsService: ElevenLabsApiService,
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
        // Initialization if needed
    }

    suspend fun speak(text: String, speed: Double = 1.05) = withContext(Dispatchers.IO) {
        val userOpenAiKey = settingsRepository.userOpenAiApiKey.firstOrNull()
        val userElevenKey = settingsRepository.userElevenLabsApiKey.firstOrNull()
        
        val openAiKey = if (!userOpenAiKey.isNullOrBlank()) userOpenAiKey else BuildConfig.OPENAI_API_KEY
        
        // Cancel any previous job if we are interrupted
        stop()

        // Granular chunker - split by punctuation or long pauses
        val chunks = splitIntoChunks(text)

        speakJob = managerScope.launch {
            isFetching = true
            try {
                // Use parallel fetching for chunks to eliminate gaps
                chunks.map { chunk ->
                    async {
                        try {
                            val response = if (!userElevenKey.isNullOrBlank()) {
                                elevenLabsService.generateSpeech(
                                    apiKey = userElevenKey,
                                    voiceId = ElevenLabsApiService.DEFAULT_VOICE_ID,
                                    request = ElevenLabsTtsRequest(text = chunk)
                                )
                            } else {
                                if (openAiKey.isBlank()) return@async null
                                openAiService.generateSpeech(
                                    auth = "Bearer $openAiKey",
                                    request = TtsRequest(input = chunk, speed = speed, voice = "alloy")
                                )
                            }

                            if (response.isSuccessful) {
                                val body = response.body()
                                if (body != null) {
                                    val tempFile = File.createTempFile("dave_voice_chunk", ".opus", context.cacheDir)
                                    FileOutputStream(tempFile).use { output ->
                                        body.byteStream().use { input ->
                                            input.copyTo(output)
                                        }
                                    }
                                    return@async tempFile
                                }
                            } else {
                                Log.e("DaveVoice", "TTS failed: ${response.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            Log.e("DaveVoice", "TTS error for chunk: $chunk", e)
                        }
                        null
                    }
                }.forEach { deferred ->
                    val tempFile = deferred.await()
                    if (tempFile != null) {
                        audioQueue.offer(tempFile)
                        
                        // If not already playing, start the queue immediately
                        if (!isPlayingQueue) {
                            withContext(Dispatchers.Main) {
                                playNextInQueue()
                            }
                        }
                    }
                }
            } finally {
                isFetching = false
                // Final check to see if we need to kickstart the queue
                if (!isPlayingQueue && audioQueue.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        playNextInQueue()
                    }
                }
            }
        }
    }

    private fun splitIntoChunks(text: String): List<String> {
        // Split by sentences first, avoid splitting on commas initially to reduce network requests
        val baseChunks = text.split(Regex("(?<=[.!?\n])\\s+")).filter { it.isNotBlank() }
        val result = mutableListOf<String>()
        for (chunk in baseChunks) {
            if (chunk.length > 300) {
                // Split very long chunks by commas, semicolons or just max length
                val subChunks = chunk.split(Regex("(?<=[,;:])\\s+")).filter { it.isNotBlank() }
                for (sub in subChunks) {
                    if (sub.length > 300) {
                        // Hard split by words if still too long
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
                    Log.e("DaveVoice", "Error setting up MediaPlayer", e)
                    playNextInQueue()
                }
            }
        } else {
            // Queue is empty. If we are still fetching chunks, don't signal end of speaking yet.
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