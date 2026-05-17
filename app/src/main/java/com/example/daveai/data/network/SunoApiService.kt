package com.example.daveai.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.*

interface SunoApiService {
    @POST("v1/suno/generate")
    suspend fun generateSong(
        @Header("Authorization") auth: String,
        @Body request: SunoRequest
    ): SunoResponse

    @GET("v1/suno/status/{id}")
    suspend fun getSongStatus(
        @Header("Authorization") auth: String,
        @Path("id") id: String
    ): SunoResponse

    companion object {
        const val BASE_URL = "https://api.suno.ai/" // Placeholder for 2026 standard API
    }
}

@JsonClass(generateAdapter = true)
data class SunoRequest(
    val prompt: String,
    val model: String = "v5.5",
    @Json(name = "make_instrumental") val makeInstrumental: Boolean = false,
    @Json(name = "wait_audio") val waitAudio: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SunoResponse(
    val id: String,
    val status: String, // pending, dreaming, completed, failed
    @Json(name = "audio_url") val audioUrl: String? = null
)
