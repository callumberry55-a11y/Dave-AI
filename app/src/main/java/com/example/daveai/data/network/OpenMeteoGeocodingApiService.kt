package com.example.daveai.data.network

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoGeocodingApiService {
    @GET("v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
    ): GeocodingSearchResponse

    companion object {
        const val BASE_URL = "https://geocoding-api.open-meteo.com/"
    }
}

@JsonClass(generateAdapter = true)
data class GeocodingSearchResponse(
    val results: List<GeocodingSearchResult>?
)

@JsonClass(generateAdapter = true)
data class GeocodingSearchResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?
)
