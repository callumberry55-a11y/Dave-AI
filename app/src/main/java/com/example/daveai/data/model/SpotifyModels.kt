package com.example.daveai.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int
)

@JsonClass(generateAdapter = true)
data class SpotifySearchResponse(
    @Json(name = "tracks") val tracks: SpotifyTracks
)

@JsonClass(generateAdapter = true)
data class SpotifyTracks(
    @Json(name = "items") val items: List<SpotifyTrack>
)

@JsonClass(generateAdapter = true)
data class SpotifyTrack(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "artists") val artists: List<SpotifyArtist>,
    @Json(name = "album") val album: SpotifyAlbum,
    @Json(name = "external_urls") val externalUrls: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class SpotifyArtist(
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class SpotifyAlbum(
    @Json(name = "name") val name: String,
    @Json(name = "images") val images: List<SpotifyImage>
)

@JsonClass(generateAdapter = true)
data class SpotifyImage(
    @Json(name = "url") val url: String,
    @Json(name = "height") val height: Int,
    @Json(name = "width") val width: Int
)

@JsonClass(generateAdapter = true)
data class SpotifyAudioFeatures(
    @Json(name = "danceability") val danceability: Float,
    @Json(name = "energy") val energy: Float,
    @Json(name = "key") val key: Int,
    @Json(name = "loudness") val loudness: Float,
    @Json(name = "mode") val mode: Int,
    @Json(name = "speechiness") val speechiness: Float,
    @Json(name = "acousticness") val acousticness: Float,
    @Json(name = "instrumentalness") val instrumentalness: Float,
    @Json(name = "liveness") val liveness: Float,
    @Json(name = "valence") val valence: Float,
    @Json(name = "tempo") val tempo: Float,
    @Json(name = "duration_ms") val durationMs: Int
)
