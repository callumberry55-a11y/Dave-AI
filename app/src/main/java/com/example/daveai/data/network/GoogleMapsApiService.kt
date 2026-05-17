package com.example.daveai.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapsApiService {
    @GET("maps/api/place/textsearch/json")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("key") apiKey: String
    ): PlacesResponse

    @GET("maps/api/geocode/json")
    suspend fun reverseGeocode(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String
    ): GeocodingResponse

    companion object {
        const val BASE_URL = "https://maps.googleapis.com/"
    }
}

@JsonClass(generateAdapter = true)
data class PlacesResponse(
    val results: List<PlaceResult>
)

@JsonClass(generateAdapter = true)
data class PlaceResult(
    val name: String,
    @param:Json(name = "formatted_address") val address: String,
    val rating: Double?
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResult>
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @param:Json(name = "formatted_address") val formattedAddress: String
)
