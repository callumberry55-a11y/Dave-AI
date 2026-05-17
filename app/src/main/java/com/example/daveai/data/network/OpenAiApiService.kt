package com.example.daveai.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApiService {
    @POST("v1/images/generations")
    suspend fun generateImage(
        @Header("Authorization") auth: String,
        @Body request: ImageRequest
    ): ImageResponse

    companion object {
        const val BASE_URL = "https://api.openai.com/"
    }
}

@JsonClass(generateAdapter = true)
data class ImageRequest(
    @Json(name = "model") val model: String = "dall-e-3",
    @Json(name = "prompt") val prompt: String,
    @Json(name = "n") val n: Int = 1,
    @Json(name = "size") val size: String = "1024x1024"
)

@JsonClass(generateAdapter = true)
data class ImageResponse(
    @Json(name = "data") val data: List<ImageData>
)

@JsonClass(generateAdapter = true)
data class ImageData(
    @Json(name = "url") val url: String
)
