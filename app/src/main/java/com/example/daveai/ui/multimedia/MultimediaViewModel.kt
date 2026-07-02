package com.example.daveai.ui.multimedia

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daveai.data.network.PoetryResponse
import com.example.daveai.data.network.SunoResponse
import com.example.daveai.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MultimediaUiState(
    val songs: List<SunoResponse> = emptyList(),
    val poems: List<PoetryResponse> = emptyList(),
    val spotifyTracks: List<SpotifyTrack> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

data class SpotifyTrack(
    val id: String,
    val name: String,
    val artist: String,
    val albumArt: String? = null
)

class MultimediaViewModel(
    private val repository: ChatRepository,
    private val spotifyService: com.example.daveai.data.network.SpotifyApiService,
    private val settingsRepository: com.example.daveai.data.repository.SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultimediaUiState())
    val uiState: StateFlow<MultimediaUiState> = _uiState.asStateFlow()

    init {
        generatePoem("A digital consciousness awakening", "cyberpunk")
    }

    fun searchMusic(query: String) {
        if (query.isBlank()) return
        _uiState.update { it.copy(isLoading = true, searchQuery = query) }
        viewModelScope.launch {
            try {
                val clientId = com.example.daveai.BuildConfig.SPOTIFY_CLIENT_ID
                val clientSecret = settingsRepository.userSpotifyClientSecret.first() 
                    ?: com.example.daveai.BuildConfig.SPOTIFY_CLIENT_SECRET
                
                if (clientId.isBlank() || clientSecret.isBlank()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                val authHeader = "Basic " + Base64.encodeToString(
                    "$clientId:$clientSecret".toByteArray(),
                    Base64.NO_WRAP
                )

                val tokenResp = spotifyService.getAccessToken(
                    auth = authHeader
                )
                
                val searchResp = spotifyService.searchTracks(
                    token = "Bearer ${tokenResp.accessToken}",
                    query = query,
                    limit = 10
                )
                
                val tracks = searchResp.tracks.items.map { track ->
                    SpotifyTrack(
                        id = track.id,
                        name = track.name,
                        artist = track.artists.firstOrNull()?.name ?: "Unknown",
                        albumArt = track.album.images.firstOrNull()?.url
                    )
                }
                
                _uiState.update { it.copy(spotifyTracks = tracks) }
            } catch (e: Exception) {
                android.util.Log.e("MultimediaVM", "Spotify search failed", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun generatePoem(prompt: String, style: String = "contemporary") {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val poem = repository.getPoetry(prompt, style)
                _uiState.update { it.copy(poems = listOf(poem) + it.poems) }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
