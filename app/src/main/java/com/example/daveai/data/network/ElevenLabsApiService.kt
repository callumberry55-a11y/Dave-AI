package com.example.daveai.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

data class ElevenLabsTtsRequest(
    val text: String,
    val model_id: String = "eleven_multilingual_v2",
    val voice_settings: VoiceSettings = VoiceSettings()
)

data class VoiceSettings(
    val stability: Double = 0.5,
    val similarity_boost: Double = 0.75
)

interface ElevenLabsApiService {
    @Streaming
    @POST("v1/text-to-speech/{voiceId}")
    suspend fun generateSpeech(
        @Header("xi-api-key") apiKey: String,
        @Path("voiceId") voiceId: String,
        @Body request: ElevenLabsTtsRequest
    ): Response<ResponseBody>

    companion object {
        const val BASE_URL = "https://api.elevenlabs.io/"
        const val DEFAULT_VOICE_ID = "21m00Tcm4TlvDq8ikWAM" // Rachel
    }
}
