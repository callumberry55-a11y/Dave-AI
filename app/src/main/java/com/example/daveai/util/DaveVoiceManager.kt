package com.example.daveai.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.daveai.BuildConfig
import com.example.daveai.data.network.ElevenLabsApiService
import com.example.daveai.data.network.ElevenLabsTtsRequest
import com.example.daveai.data.network.OpenAiApiService
import com.example.daveai.data.network.TtsRequest
import com.example.daveai.data.network.VoiceSettings
import com.example.daveai.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream

/**
 * Dave's Vocal Engine (V3)
 * Completely rewritten for zero-overlap, manual-only execution.
 */
class DaveVoiceManager(
    private val context: Context,
    private val openAiService: OpenAiApiService,
    private val elevenLabsService: ElevenLabsApiService,
    private val settingsRepository: SettingsRepository
) : TextToSpeech.OnInitListener {
    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    var onStartSpeaking: (() -> Unit)? = null
    var onDoneSpeaking: (() -> Unit)? = null
    var onErrorSpeaking: (() -> Unit)? = null

    private var activeSpeechJob: Job? = null
    private val audioFileChannel = Channel<File>(Channel.UNLIMITED)

    init {
        tts = TextToSpeech(context, this)
        startQueueConsumer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            Log.d("DaveVoice", "Neural Vocal Core V3 Online.")
        }
    }

    /**
     * Entry point for manual speech. 
     * Stops everything current before starting.
     */
    fun speak(text: String, speed: Double = 1.05, mood: String = "NEUTRAL") {
        managerScope.launch {
            Log.d("DaveVoice", "Speech requested ($mood). Stopping current stream.")
            stopInternal()
            
            val useSystemTts = settingsRepository.useSystemTts.firstOrNull() ?: false
            if (useSystemTts) {
                speakSystem(text)
            } else {
                startCloudSpeech(text, speed, mood)
            }
        }
    }

    private fun startCloudSpeech(text: String, speed: Double, mood: String) {
        activeSpeechJob = managerScope.launch(Dispatchers.IO) {
            val userOpenAiKey = settingsRepository.userOpenAiApiKey.firstOrNull()
            val userElevenLabsKey = settingsRepository.userElevenLabsApiKey.firstOrNull()
            val useElevenLabs = !userElevenLabsKey.isNullOrBlank()
            
            val openAiKey = if (!userOpenAiKey.isNullOrBlank()) userOpenAiKey else BuildConfig.OPENAI_API_KEY
            val elevenLabsKey = if (useElevenLabs) userElevenLabsKey else BuildConfig.ELEVENLABS_API_KEY
            
            if (openAiKey.isBlank() && elevenLabsKey.isBlank()) return@launch

            _isSpeaking.value = true
            val chunks = text.split(Regex("(?<=[.!?\n])\\s+")).filter { it.isNotBlank() }
            
            for (chunk in chunks) {
                if (!isActive) break
                val file = if (useElevenLabs && !elevenLabsKey.isNullOrBlank()) {
                    fetchElevenLabsChunk(chunk, elevenLabsKey, mood)
                } else {
                    fetchOpenAiChunk(chunk, openAiKey, speed)
                }
                
                if (file != null) {
                    audioFileChannel.send(file)
                }
            }
        }
    }

    private fun startQueueConsumer() {
        managerScope.launch {
            for (file in audioFileChannel) {
                playAudioFile(file)
            }
        }
    }

    private suspend fun playAudioFile(file: File) = suspendCancellableCoroutine { continuation ->
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build())
                
                setOnPreparedListener { 
                    it.start() 
                    onStartSpeaking?.invoke()
                }
                setOnCompletionListener { 
                    cleanup(it, file)
                    if (audioFileChannel.isEmpty) {
                        _isSpeaking.value = false
                        onDoneSpeaking?.invoke()
                    }
                    continuation.resume(Unit) {}
                }
                setOnErrorListener { mp, _, _ ->
                    cleanup(mp, file)
                    onErrorSpeaking?.invoke()
                    continuation.resume(Unit) {}
                    true
                }
                prepareAsync()
            } catch (e: Exception) {
                cleanup(this, file)
                onErrorSpeaking?.invoke()
                continuation.resume(Unit) {}
            }
        }
        
        continuation.invokeOnCancellation {
            stopInternal()
        }
    }

    private fun cleanup(mp: MediaPlayer, file: File) {
        try {
            mp.release()
            if (mediaPlayer == mp) mediaPlayer = null
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
    }

    private fun speakSystem(text: String) {
        if (!isTtsInitialized) return
        _isSpeaking.value = true
        onStartSpeaking?.invoke()
        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "DaveV3")
        
        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(p0: String?) {}
            override fun onDone(p0: String?) { 
                _isSpeaking.value = false 
                onDoneSpeaking?.invoke()
            }
            override fun onError(p0: String?) { 
                _isSpeaking.value = false 
                onErrorSpeaking?.invoke()
            }
        })
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "DaveV3")
    }

    private suspend fun fetchOpenAiChunk(chunk: String, apiKey: String, speed: Double): File? {
        return try {
            val response = openAiService.generateSpeech(
                auth = "Bearer $apiKey",
                request = TtsRequest(input = chunk, speed = speed, voice = "alloy", responseFormat = "opus")
            )
            if (response.isSuccessful) {
                val tempFile = File.createTempFile("dave_v3_openai_", ".ogg", context.cacheDir)
                FileOutputStream(tempFile).use { out -> response.body()?.byteStream()?.copyTo(out) }
                tempFile
            } else null
        } catch (_: Exception) { null }
    }

    private suspend fun fetchElevenLabsChunk(chunk: String, apiKey: String, mood: String): File? {
        val (stability, similarity) = when (mood.uppercase()) {
            "CALM", "EMPATHETIC" -> 0.8 to 0.6
            "HYPED", "URGENT" -> 0.3 to 0.9
            "HACKER" -> 0.4 to 0.8
            else -> 0.5 to 0.75
        }
        
        return try {
            val response = elevenLabsService.generateSpeech(
                apiKey = apiKey,
                voiceId = ElevenLabsApiService.DEFAULT_VOICE_ID,
                request = ElevenLabsTtsRequest(
                    text = chunk,
                    model_id = "eleven_monolingual_v1",
                    voice_settings = VoiceSettings(stability = stability, similarity_boost = similarity)
                )
            )
            if (response.isSuccessful) {
                val tempFile = File.createTempFile("dave_v3_eleven_", ".mp3", context.cacheDir)
                FileOutputStream(tempFile).use { out -> response.body()?.byteStream()?.copyTo(out) }
                tempFile
            } else null
        } catch (_: Exception) { null }
    }

    fun stop() {
        managerScope.launch {
            stopInternal()
        }
    }

    private fun stopInternal() {
        activeSpeechJob?.cancel()
        activeSpeechJob = null
        
        // Drain channel
        while (!audioFileChannel.isEmpty) {
            audioFileChannel.tryReceive().getOrNull()?.delete()
        }
        
        mediaPlayer?.let {
            try { it.stop(); it.release() } catch (_: Exception) {}
        }
        mediaPlayer = null
        
        tts?.stop()
        _isSpeaking.value = false
    }

    fun destroy() {
        stopInternal()
        tts?.shutdown()
        managerScope.cancel()
    }
}
