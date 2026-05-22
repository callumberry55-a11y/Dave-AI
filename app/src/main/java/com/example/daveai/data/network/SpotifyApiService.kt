package com.example.daveai.data.network

import com.example.daveai.data.model.SpotifyAudioFeatures
import com.example.daveai.data.model.SpotifySearchResponse
import com.example.daveai.data.model.SpotifyTokenResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyApiService {
    @FormUrlEncoded
    @POST("https://accounts.spotify.com/api/token")
    suspend fun getAccessToken(
        @Header("Authorization") auth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): SpotifyTokenResponse

    @GET("v1/search")
    suspend fun searchTracks(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 1
    ): SpotifySearchResponse

    @GET("v1/audio-features/{id}")
    suspend fun getAudioFeatures(
        @Header("Authorization") token: String,
        @Path("id") trackId: String
    ): SpotifyAudioFeatures

    companion object {
        const val BASE_URL = "https://api.spotify.com/"
    }
}
