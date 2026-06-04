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
    private var mediaPlayer: MediaPlayer? = null
    private val audioQueue = ConcurrentLinkedQueue<File>()
    private var isPlayingQueue = false
    private var scopeJob: Job? = null
    
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

        // Granular chunker
        val sentences = text.split(Regex("(?<=[.!?,\n-])\\s+")).filter { it.isNotBlank() }

        scopeJob = CoroutineScope(Dispatchers.IO).launch {
            for (sentence in sentences) {
                try {
                    val response = if (!userElevenKey.isNullOrBlank()) {
                        elevenLabsService.generateSpeech(
                            apiKey = userElevenKey,
                            voiceId = ElevenLabsApiService.DEFAULT_VOICE_ID,
                            request = ElevenLabsTtsRequest(text = sentence)
                        )
                    } else {
                        if (openAiKey.isBlank()) continue
                        openAiService.generateSpeech(
                            auth = "Bearer $openAiKey",
                            request = TtsRequest(input = sentence, speed = speed, voice = "alloy")
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
            val wasPlaying = isPlayingQueue
            isPlayingQueue = true
            _isSpeaking.value = true
            
            if (!wasPlaying) {
                onStartSpeaking?.invoke()
            }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(nextFile.absolutePath)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                setAudioAttributes(audioAttributes)
                
                prepare()
                start()
                setOnCompletionListener { 
                    it.release()
                    if (mediaPlayer == it) mediaPlayer = null
                    // Delete temp file after playing
                    try { nextFile.delete() } catch (_: Exception) {}
                    
                    playNextInQueue()
                }
                setOnErrorListener { mp, _, _ -> 
                    mp.release()
                    if (mediaPlayer == mp) mediaPlayer = null
                    try { nextFile.delete() } catch (_: Exception) {}
                    playNextInQueue()
                    true
                }
            }
        } else {
            // Queue is empty
            val wasPlaying = isPlayingQueue
            isPlayingQueue = false
            _isSpeaking.value = false
            
            if (wasPlaying) {
                onDoneSpeaking?.invoke()
            }
        }
    }

    fun stop() {
        val wasPlaying = isPlayingQueue
        scopeJob?.cancel()
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