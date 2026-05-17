package com.example.daveai.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daveai.data.db.ChatSessionEntity
import com.example.daveai.data.repository.ChatRepository
import com.example.daveai.data.repository.UserProfile
import com.example.daveai.data.repository.UserStatsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val sessions: List<ChatSessionEntity> = emptyList(),
    val userProfile: UserProfile? = null,
    val currentSessionId: String? = null,
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val isFastMode: Boolean = false,
    val userLocation: String? = null,
    val totalAppUsers: Long = 0,
    val attachedFiles: List<AttachedFile> = emptyList(),
)

data class AttachedFile(
    val uri: Uri,
    val name: String,
    val type: String,
    val base64Data: String? = null,
)

data class ChatMessage(
    val content: String,
    val isFromDave: Boolean,
    val mediaUrl: String? = null,
    val mediaType: MediaType = MediaType.NONE,
    val hasAttachment: Boolean = false,
)

enum class MediaType {
    NONE, IMAGE, VIDEO
}

class ChatViewModel(
    private val repository: ChatRepository,
) : ViewModel() {
    private val userStatsRepository = UserStatsRepository()
    private val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var messageCollectionJob: Job? = null

    init {
        observeSessions()
        fetchUserCount()
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val uid = auth?.currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = userStatsRepository.getUserProfile(uid)
            _uiState.update { it.copy(userProfile = profile) }
        }
    }

    private fun fetchUserCount() {
        viewModelScope.launch {
            userStatsRepository.observeTotalUserCount().collect { count ->
                _uiState.update { it.copy(totalAppUsers = count) }
            }
        }
    }

    private fun observeSessions() {
        viewModelScope.launch {
            repository.allSessions.collect { sessions ->
                _uiState.update { it.copy(sessions = sessions) }
                if ((_uiState.value.currentSessionId == null) && sessions.isNotEmpty()) {
                    selectSession(sessions.first().sessionId)
                } else if (sessions.isEmpty()) {
                    createNewChat()
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _uiState.update { it.copy(currentSessionId = sessionId) }
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { entities ->
                val uiMessages = entities.map {
                    ChatMessage(
                        content = it.content,
                        isFromDave = it.role == "assistant",
                        mediaUrl = it.mediaUrl,
                        mediaType = try { MediaType.valueOf(it.mediaType) } catch (_: Exception) { MediaType.NONE },
                        hasAttachment = it.content.contains("[Attached File:"),
                    )
                }
                _uiState.update { it.copy(messages = uiMessages) }
            }
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            val id = repository.createNewSession("New Chat ${System.currentTimeMillis() / 1000}")
            selectSession(id)
        }
    }

    fun updateLocation(location: String) {
        _uiState.update { it.copy(userLocation = location) }
    }

    fun setIsListening(isListening: Boolean) {
        _uiState.update { it.copy(isListening = isListening) }
    }

    fun toggleFastMode() {
        _uiState.update { it.copy(isFastMode = !it.isFastMode) }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun addAttachment(file: AttachedFile) {
        _uiState.update { it.copy(attachedFiles = it.attachedFiles + file) }
    }

    fun removeAttachment(file: AttachedFile) {
        _uiState.update { it.copy(attachedFiles = it.attachedFiles - file) }
    }

    fun sendMessage() {
        val currentInput = _uiState.value.inputText.trim()
        val sessionId = _uiState.value.currentSessionId ?: return
        val attachments = _uiState.value.attachedFiles
        
        if (currentInput.isEmpty() && attachments.isEmpty()) return
        if (_uiState.value.isLoading) return

        val location = _uiState.value.userLocation
        val isFastMode = _uiState.value.isFastMode
        val userProfile = _uiState.value.userProfile

        _uiState.update {
            it.copy(
                inputText = "",
                attachedFiles = emptyList(),
                isLoading = true,
            )
        }

        viewModelScope.launch {
            try {
                repository.sendMessage(
                    sessionId = sessionId,
                    userContent = currentInput,
                    locationInfo = location,
                    attachments = attachments,
                    isFastMode = isFastMode,
                    userProfile = userProfile,
                    bypassIntercept = false,
                )
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Send failed", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.currentSessionId == sessionId) {
                _uiState.update { it.copy(currentSessionId = null, messages = emptyList()) }
            }
        }
    }

    fun deleteCurrentSession() {
        val sessionId = _uiState.value.currentSessionId ?: return
        deleteSession(sessionId)
    }
}
