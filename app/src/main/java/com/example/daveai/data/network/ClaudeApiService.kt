package com.example.daveai.data.network

import com.example.daveai.data.model.MessageRequest
import com.example.daveai.data.model.MessageResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Header("anthropic-beta") beta: String? = "pdfs-2024-09-25",
        @Header("content-type") contentType: String = "application/json",
        @Body request: MessageRequest
    ): MessageResponse

    companion object {
        const val BASE_URL = "https://api.anthropic.com/"
    }
}
