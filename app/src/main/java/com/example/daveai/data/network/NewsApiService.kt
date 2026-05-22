package com.example.daveai.data.network

import com.example.daveai.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("apiKey") apiKey: String,
        @Query("country") country: String = "us",
        @Query("pageSize") pageSize: Int = 5
    ): NewsResponse

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("apiKey") apiKey: String,
        @Query("q") query: String,
        @Query("pageSize") pageSize: Int = 5,
        @Query("sortBy") sortBy: String = "relevancy"
    ): NewsResponse

    companion object {
        const val BASE_URL = "https://newsapi.org/"
    }
}
