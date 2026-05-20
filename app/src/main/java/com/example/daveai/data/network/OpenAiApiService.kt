package com.example.daveai.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OpenAiApiService {
    @POST("v1/images/generations")
    suspend fun generateImage(
        @Header("Authorization") auth: String,
        @Body request: ImageRequest,
    ): ImageResponse

    @Streaming
    @POST("v1/audio/speech")
    suspend fun generateSpeech(
        @Header("Authorization") auth: String,
        @Body request: TtsRequest,
    ): Response<ResponseBody>

    companion object {
        const val BASE_URL = "https://api.openai.com/"
    }
}

@JsonClass(generateAdapter = true)
data class TtsRequest(
    @param:Json(name = "model") val model: String = "tts-1",
    @param:Json(name = "input") val input: String,
    @param:Json(name = "voice") val voice: String = "echo", // echo is a balanced, warm male voice
    @param:Json(name = "response_format") val responseFormat: String = "opus", // Opus streams much faster than MP3
    @param:Json(name = "speed") val speed: Double = 1.05, // Slightly faster, but relaxed for warmth
)

@JsonClass(generateAdapter = true)
data class ImageRequest(
    @param:Json(name = "model") val model: String = "dall-e-3",
    @param:Json(name = "prompt") val prompt: String,
    @param:Json(name = "n") val n: Int = 1,
    @param:Json(name = "size") val size: String = "1024x1024",
)

@JsonClass(generateAdapter = true)
data class ImageResponse(
    @Json(name = "data") val data: List<ImageData>
)

@JsonClass(generateAdapter = true)
data class ImageData(
    @Json(name = "url") val url: String
)
