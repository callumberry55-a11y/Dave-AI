package com.example.daveai.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {
    @GET("search")
    suspend fun searchWeb(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("cx") contextId: String
    ): SearchResponse

    companion object {
        const val BASE_URL = "https://www.googleapis.com/customsearch/v1/"
    }
}

data class SearchResponse(
    val items: List<SearchItem>? = null
)

data class SearchItem(
    val title: String,
    val snippet: String,
    val link: String
)
