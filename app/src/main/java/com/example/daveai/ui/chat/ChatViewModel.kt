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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val dbMessages: List<ChatMessage> = emptyList(),
    val ghostMessages: List<ChatMessage> = emptyList(),
    val sessions: List<ChatSessionEntity> = emptyList(),
    val userProfile: UserProfile? = null,
    val currentSessionId: String? = null,
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val isFastMode: Boolean = false,
    val isGodMode: Boolean = false,
    val isGhostMode: Boolean = false,
    val isContinuousVoiceMode: Boolean = false,
    val isLiveMode: Boolean = false,
    val userLocation: String? = null,
    val totalAppUsers: Long = 0,
    val attachedFiles: List<AttachedFile> = emptyList(),
    val dynamicSuggestions: List<String> = emptyList(),
    val isVaultOpen: Boolean = false,
    val currentMode: DaveMode = DaveMode.EXPLORER,
) {
    val messages: List<ChatMessage>
        get() = dbMessages + ghostMessages
}

enum class DaveMode {
    EXPLORER, RESEARCHER, CREATIVE, HACKER, ANALYST, GAMER
}

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
    val isLocal: Boolean = false,
    val actions: List<String> = emptyList(),
    val widgetType: WidgetType = WidgetType.NONE,
    val widgetData: String? = null,
)

enum class MediaType {
    NONE, IMAGE, VIDEO
}

enum class WidgetType {
    NONE, MAP, HARDWARE
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
        refreshDynamicSuggestions()
    }

    private fun refreshDynamicSuggestions() {
        viewModelScope.launch {
            val battery = repository.getDeviceAssistant().getBatteryLevel()
            val connectivity = repository.getDeviceAssistant().getConnectivityStatus()
            val hour = java.util.Calendar.getInstance()[java.util.Calendar.HOUR_OF_DAY]
            
            val suggestions = mutableListOf<String>()
            
            // Context-based suggestions
            if (battery < 30) {
                suggestions.add("🔋 Check battery health")
            }
            
            if (connectivity.contains("offline")) {
                suggestions.add("🌐 Troubleshoot connection")
            }

            if ((hour !in 6..20)) {
                suggestions.add("🔦 Turn on flashlight")
            }

            // Always add some "Elite" features
            suggestions.add("📝 Write a deep poem about AI")
            suggestions.add("🎸 Write a rock song about coding")
            suggestions.add("🎨 Create a futuristic AI portrait")
            suggestions.add("🛠️ Scan hardware performance")
            
            _uiState.update { it.copy(dynamicSuggestions = suggestions.take(5)) }
        }
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
        _uiState.update { it.copy(currentSessionId = sessionId, ghostMessages = emptyList()) }
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { entities ->
                val uiMessages = entities.map {
                    val content = it.content
                    ChatMessage(
                        content = content,
                        isFromDave = it.role == "assistant",
                        mediaUrl = it.mediaUrl,
                        mediaType = try { MediaType.valueOf(it.mediaType) } catch (_: Exception) { MediaType.NONE },
                        hasAttachment = content.contains("[Attached File:"),
                        isLocal = content.contains("⚡️ ("),
                        actions = buildList {
                            // Legacy auto-actions
                            if (content.contains("battery", ignoreCase = true)) add("Check Battery")
                            if (content.contains("flashlight", ignoreCase = true)) add("Toggle Light")
                            if (content.contains("hardware", ignoreCase = true)) add("Scan Specs")
                            if (content.contains("location", ignoreCase = true) || content.contains("near me", ignoreCase = true)) add("Find Nearby")
                            
                            // Explicit [BUTTON: ...] actions
                            val regex = "\\[BUTTON: (.*?)]".toRegex()
                            regex.findAll(content).forEach { match ->
                                add(match.groupValues[1])
                            }
                        },
                        widgetType = try { WidgetType.valueOf(it.widgetType) } catch (_: Exception) { WidgetType.NONE },
                        widgetData = it.widgetData,
                    )
                }
                _uiState.update { it.copy(dbMessages = uiMessages) }
            }
        }
    }

    fun createNewChat(projectType: String = "GENERAL") {
        viewModelScope.launch {
            val titlePrefix = when(projectType) {
                "CODE" -> "💻 Code Project"
                "ART" -> "🎨 Art Project"
                "LANGUAGE" -> "🌐 Language Project"
                "MUSIC" -> "🎵 Music Project"
                "FITNESS" -> "🏋️ Fitness Room"
                "FINANCE" -> "💰 Finance Room"
                "TRAVEL" -> "✈️ Travel Room"
                "GAMING" -> "🎮 Gaming Room"
                "LESSONS" -> "🎓 Lesson Room"
                else -> "New Chat"
            }
            val id = repository.createNewSession("$titlePrefix ${System.currentTimeMillis() / 1000}", projectType)
            selectSession(id)
        }
    }

    fun setIsListening(isListening: Boolean) {
        _uiState.update { it.copy(isListening = isListening) }
    }

    fun setLiveMode(isActive: Boolean) {
        _uiState.update { it.copy(isLiveMode = isActive) }
    }

    fun toggleFastMode() {
        _uiState.update { it.copy(isFastMode = !it.isFastMode) }
    }

    fun toggleGodMode() {
        _uiState.update { it.copy(isGodMode = !it.isGodMode) }
    }

    fun toggleGhostMode() {
        _uiState.update { it.copy(isGhostMode = !it.isGhostMode) }
    }

    fun toggleContinuousVoiceMode() {
        _uiState.update { it.copy(isContinuousVoiceMode = !it.isContinuousVoiceMode) }
    }

    fun setMode(mode: DaveMode) {
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun updateLocation(location: String?) {
        _uiState.update { it.copy(userLocation = location) }
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
        val isGodMode = _uiState.value.isGodMode
        val isGhostMode = _uiState.value.isGhostMode
        val userProfile = _uiState.value.userProfile
        val currentMode = _uiState.value.currentMode
        val uid = auth?.currentUser?.uid
        val isLiveMode = _uiState.value.isLiveMode

        if (isGhostMode) {
            val userMsg = ChatMessage(
                content = currentInput,
                isFromDave = false,
                hasAttachment = attachments.isNotEmpty()
            )
            _uiState.update { it.copy(ghostMessages = it.ghostMessages + userMsg) }
        }

        _uiState.update {
            it.copy(
                inputText = "",
                attachedFiles = emptyList(),
                isLoading = true,
            )
        }

        viewModelScope.launch {
            try {
                val responseText = repository.sendMessage(
                    sessionId = sessionId,
                    userContent = currentInput,
                    locationInfo = location,
                    attachments = attachments,
                    isFastMode = isFastMode,
                    isGodMode = isGodMode || currentMode == DaveMode.HACKER,
                    isGhostMode = isGhostMode,
                    userProfile = userProfile,
                    uid = uid,
                    bypassIntercept = false,
                    mode = currentMode,
                    isLiveMode = isLiveMode,
                )
                
                // Fetch user profile again in case the background memory extractor found something
                fetchUserProfile()
                
                if (isGhostMode && !responseText.startsWith("Error:")) {
                    val daveMsg = ChatMessage(
                        content = responseText,
                        isFromDave = true,
                        isLocal = responseText.contains("⚡️ (")
                    )
                    _uiState.update { it.copy(ghostMessages = it.ghostMessages + daveMsg) }
                }

                refreshDynamicSuggestions() // Refresh after Dave responds
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
                _uiState.update { it.copy(currentSessionId = null, dbMessages = emptyList(), ghostMessages = emptyList()) }
            }
        }
    }

    fun deleteCurrentSession() {
        val sessionId = _uiState.value.currentSessionId ?: return
        deleteSession(sessionId)
    }

    fun toggleVault(open: Boolean) {
        _uiState.update { it.copy(isVaultOpen = open) }
    }

    fun updateVaultEntry(key: String, value: String) {
        val uid = auth?.currentUser?.uid ?: return
        viewModelScope.launch {
            userStatsRepository.updatePreference(uid, key, value)
            fetchUserProfile()
        }
    }
}
