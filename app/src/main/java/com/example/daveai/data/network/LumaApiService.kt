package com.example.daveai.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.*

interface LumaApiService {
    @POST("dream-machine/v1/generations")
    suspend fun createGeneration(
        @Header("Authorization") auth: String,
        @Body request: LumaGenerationRequest
    ): LumaGenerationResponse

    @GET("dream-machine/v1/generations/{id}")
    suspend fun getGeneration(
        @Header("Authorization") auth: String,
        @Path("id") id: String
    ): LumaGenerationResponse

    companion object {
        const val BASE_URL = "https://api.lumalabs.ai/"
    }
}

@JsonClass(generateAdapter = true)
data class LumaGenerationRequest(
    val prompt: String,
    val model: String = "ray-2",
    @Json(name = "aspect_ratio") val aspectRatio: String = "16:9"
)

@JsonClass(generateAdapter = true)
data class LumaGenerationResponse(
    val id: String,
    val state: String, // pending, dreaming, completed, failed
    val video: LumaVideoData? = null
)

@JsonClass(generateAdapter = true)
data class LumaVideoData(
    val url: String
)
