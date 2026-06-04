package com.example.daveai.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface PoetryApiService {
    companion object {
        const val BASE_URL = "https://www.poetrysuite.net/api/"
    }

    @GET("generate")
    suspend fun getPoetry(
        @Query("prompt") prompt: String,
        @Query("style") style: String = "contemporary"
    ): PoetryResponse
}

data class PoetryResponse(
    val content: String,
    val author: String? = null
)
