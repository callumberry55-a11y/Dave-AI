package com.example.daveai.data.network

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PerplexityApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") auth: String,
        @Body request: PerplexityChatRequest
    ): PerplexityChatResponse

    companion object {
        const val BASE_URL = "https://api.perplexity.ai/"
    }
}

@JsonClass(generateAdapter = true)
data class PerplexityChatRequest(
    val model: String = "llama-3-sonar-small-32k-online",
    val messages: List<PerplexityMessage>,
    val temperature: Float = 0.2f,
    val max_tokens: Int = 1000
)

@JsonClass(generateAdapter = true)
data class PerplexityMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class PerplexityChatResponse(
    val choices: List<PerplexityChoice>,
    val usage: PerplexityUsage? = null
)

@JsonClass(generateAdapter = true)
data class PerplexityChoice(
    val message: PerplexityMessage
)

@JsonClass(generateAdapter = true)
data class PerplexityUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)
