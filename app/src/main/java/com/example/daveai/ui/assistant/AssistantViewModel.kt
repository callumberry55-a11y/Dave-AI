package com.example.daveai.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daveai.data.repository.ChatRepository
import com.example.daveai.data.repository.UserStatsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantUiState(
    val daveResponse: String? = null,
    val isThinking: Boolean = false,
    val error: String? = null,
)

class AssistantViewModel(
    private val repository: ChatRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val userStatsRepository = UserStatsRepository()
    private val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }

    fun onUserSpeechFinished(text: String) {
        if (text.isBlank()) return
        
        _uiState.update { it.copy(isThinking = true, daveResponse = null, error = null) }
        
        viewModelScope.launch {
            try {
                // Find or create a session for Assistant interactions
                val email = auth?.currentUser?.email ?: "ANONYMOUS"
                val conversations = repository.allConversations.first()
                val assistantSession = conversations.find { it.title == "Quick Assistant" } 
                    ?: repository.createNewConversation("Quick Assistant", email).let { id ->
                        repository.allConversations.first().find { it.id == id }!!
                    }
                
                val profile = auth?.currentUser?.uid?.let { userStatsRepository.getUserProfile(it) }

                val response = repository.sendMessage(
                    sessionId = assistantSession.id,
                    userContent = text,
                    userProfile = profile
                )
                
                _uiState.update { it.copy(daveResponse = response, isThinking = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isThinking = false) }
            }
        }
    }

    fun reset() {
        _uiState.update { AssistantUiState() }
    }
}
