package com.example.daveai.data.network

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

interface PoetryDbApiService {
    @GET("author/{author}")
    suspend fun getPoemsByAuthor(
        @Path("author") author: String
    ): List<PoetryDbEntry>

    @GET("author,title/{author};{title}")
    suspend fun getPoemsByAuthorAndTitle(
        @Path("author") author: String,
        @Path("title") title: String
    ): List<PoetryDbEntry>

    @GET("author")
    suspend fun getAuthors(): PoetryDbAuthorsResponse

    companion object {
        const val BASE_URL = "https://poetrydb.org/"
    }
}

@JsonClass(generateAdapter = true)
data class PoetryDbEntry(
    val title: String,
    val author: String,
    val lines: List<String>,
    val linecount: String
)

@JsonClass(generateAdapter = true)
data class PoetryDbAuthorsResponse(
    val authors: List<String>
)
