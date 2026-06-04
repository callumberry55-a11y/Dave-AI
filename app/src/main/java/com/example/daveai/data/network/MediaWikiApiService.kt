package com.example.daveai.data.network

import com.example.daveai.data.model.WikiExtractResponse
import com.example.daveai.data.model.WikiSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MediaWikiApiService {
    @GET("api.php")
    suspend fun search(
        @Query("action") action: String = "query",
        @Query("list") list: String = "search",
        @Query("srsearch") query: String,
        @Query("format") format: String = "json"
    ): WikiSearchResponse

    @GET("api.php")
    suspend fun getExtract(
        @Query("action") action: String = "query",
        @Query("prop") prop: String = "extracts",
        @Query("exintro") exintro: Boolean = true,
        @Query("explaintext") explaintext: Boolean = true,
        @Query("titles") titles: String,
        @Query("format") format: String = "json"
    ): WikiExtractResponse

    companion object {
        const val BASE_URL = "https://en.wikipedia.org/w/"
    }
}
