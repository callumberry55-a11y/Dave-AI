package com.example.daveai.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daveai.data.repository.ChatRepository
import com.example.daveai.data.repository.UserProfile
import com.example.daveai.data.repository.UserStatsRepository
import com.example.daveai.ui.navigation.DaveRoute
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class ChatUiState(
    val dbMessages: List<ChatMessage> = emptyList(),
    val ghostMessages: List<ChatMessage> = emptyList(),
    val sessions: List<com.example.daveai.data.db.ConversationEntity> = emptyList(),
    val userProfile: UserProfile? = null,
    val currentSessionId: String? = null,
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val isFastMode: Boolean = false,
    val isGodMode: Boolean = false,
    val isGhostMode: Boolean = false,
    val isLiveMode: Boolean = false,
    val userLocation: String? = null,
    val totalAppUsers: Long = 0L,
    val attachedFiles: List<AttachedFile> = emptyList(),
    val dynamicSuggestions: List<String> = emptyList(),
    val isVaultOpen: Boolean = false,
    val currentMode: DaveMode = DaveMode.EXPLORER,
    val semanticMemories: List<SemanticMemory> = emptyList(),
    val primaryColor: Int = 0,
    val useSystemWallpaper: Boolean = false,
    val customWallpaperUri: String? = null,
    val digitalPersona: String = "HACKER",
    val cyberIntensity: Float = 0.8f,
    val typographyStyle: String = "MODERN",
    val isMoodReactive: Boolean = true,
    val meshAnimationSpeed: Float = 1.0f,
    val useIrishAccent: Boolean = false,
    val isBuildingApp: Boolean = false,
    val buildProgress: Float = 0f,
    val buildLogs: List<String> = emptyList(),
    val appBlueprint: List<com.example.daveai.util.BlueprintItem> = emptyList(),
    val isShowingPreview: Boolean = false,
    val isAutoReplyEnabled: Boolean = false,
    val isSpeaking: Boolean = false,
    val partnerId: String? = null,
    val partnerName: String? = null,
    val pairingCode: String? = null,
    val userClaudeApiKey: String? = null,
    val userOpenAiApiKey: String? = null,
    val userSpotifyClientId: String? = null,
    val userSpotifyClientSecret: String? = null,
    val userNewsApiKey: String? = null,
    val userMapsApiKey: String? = null,
    val userGroqApiKey: String? = null,
    val userPerplexityApiKey: String? = null,
    val userElevenLabsApiKey: String? = null,
    val userWeatherApiKey: String? = null,
    val userFinanceApiKey: String? = null,
    val userGeminiApiKey: String? = null,
    val userSunoApiKey: String? = null,
    val userCryptoApiKey: String? = null,
    val userWikiApiKey: String? = null,
    val userFirestoreApiKey: String? = null,
    val blurIntensity: Float = 0.5f,
    val glowStrength: Float = 0.5f,
    val pendingRoute: DaveRoute? = null,
    val isFlashlightOn: Boolean = false,
    val isDndOn: Boolean = false,
) {
    val messages: List<ChatMessage>
        get() = dbMessages + ghostMessages
}

enum class DaveMode {
    EXPLORER, RESEARCHER, CREATIVE, HACKER, ANALYST, GAMER, VISIONARY, SOCIOLOGIST, APP_FACTORY
}

data class AttachedFile(
    val uri: Uri,
    val name: String,
    val type: String,
    val base64Data: String? = null,
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromDave: Boolean,
    val mediaUrl: String? = null,
    val mediaType: MediaType = MediaType.NONE,
    val hasAttachment: Boolean = false,
    val isLocal: Boolean = false,
    val actions: List<String> = emptyList(),
    val widgetType: WidgetType = WidgetType.NONE,
    val widgetData: String? = null,
    val mood: String = "NEUTRAL",
)

enum class MediaType {
    NONE, IMAGE, VIDEO
}

enum class WidgetType {
    NONE, MAP, HARDWARE, FINANCE, FITNESS, SPOTIFY, NEWS, CALENDAR, USAGE
}

typealias ChatMessageEntity = com.example.daveai.data.db.ChatMessageEntity
typealias SemanticMemory = com.example.daveai.data.db.SemanticMemory

class ChatViewModel(
    private val repository: ChatRepository,
    private val settingsRepository: com.example.daveai.data.repository.SettingsRepository
) : ViewModel() {

    private val userStatsRepository = UserStatsRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val thinkingStatus = repository.thinkingStatus

    private var messageCollectionJob: Job? = null

    init {
        fetchUserProfile()
        fetchUserCount()
        observeSessions()
        observeMemories()

        // Sync local UI state with SettingsRepository flows
        viewModelScope.launch {
            settingsRepository.primaryColor.collect { color ->
                _uiState.update { it.copy(primaryColor = color) }
            }
        }
        viewModelScope.launch {
            settingsRepository.useSystemWallpaper.collect { use ->
                _uiState.update { it.copy(useSystemWallpaper = use) }
            }
        }
        viewModelScope.launch {
            settingsRepository.customWallpaperUri.collect { uri ->
                _uiState.update { it.copy(customWallpaperUri = uri) }
            }
        }
        viewModelScope.launch {
            settingsRepository.digitalPersona.collect { persona ->
                _uiState.update { it.copy(digitalPersona = persona) }
            }
        }
        viewModelScope.launch {
            settingsRepository.cyberIntensity.collect { intensity ->
                _uiState.update { it.copy(cyberIntensity = intensity) }
            }
        }
        viewModelScope.launch {
            settingsRepository.typographyStyle.collect { style ->
                _uiState.update { it.copy(typographyStyle = style) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isMoodReactive.collect { reactive ->
                _uiState.update { it.copy(isMoodReactive = reactive) }
            }
        }
        viewModelScope.launch {
            settingsRepository.meshAnimationSpeed.collect { speed ->
                _uiState.update { it.copy(meshAnimationSpeed = speed) }
            }
        }
        viewModelScope.launch {
            settingsRepository.useIrishAccent.collect { use ->
                _uiState.update { it.copy(useIrishAccent = use) }
            }
        }
        viewModelScope.launch {
            settingsRepository.partnerId.collect { id ->
                _uiState.update { it.copy(partnerId = id) }
            }
        }
        viewModelScope.launch {
            settingsRepository.partnerName.collect { name ->
                _uiState.update { it.copy(partnerName = name) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isAutoReplyEnabled.collect { enabled ->
                _uiState.update { it.copy(isAutoReplyEnabled = enabled) }
            }
        }

        // API Keys Observation
        viewModelScope.launch {
            settingsRepository.userClaudeApiKey.collect { key ->
                _uiState.update { it.copy(userClaudeApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userOpenAiApiKey.collect { key ->
                _uiState.update { it.copy(userOpenAiApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userSpotifyClientSecret.collect { secret ->
                _uiState.update { it.copy(userSpotifyClientSecret = secret) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userNewsApiKey.collect { key ->
                _uiState.update { it.copy(userNewsApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userMapsApiKey.collect { key ->
                _uiState.update { it.copy(userMapsApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userGroqApiKey.collect { key ->
                _uiState.update { it.copy(userGroqApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userPerplexityApiKey.collect { key ->
                _uiState.update { it.copy(userPerplexityApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userElevenLabsApiKey.collect { key ->
                _uiState.update { it.copy(userElevenLabsApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userWeatherApiKey.collect { key ->
                _uiState.update { it.copy(userWeatherApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userFinanceApiKey.collect { key ->
                _uiState.update { it.copy(userFinanceApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userGeminiApiKey.collect { key ->
                _uiState.update { it.copy(userGeminiApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userSunoApiKey.collect { key ->
                _uiState.update { it.copy(userSunoApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userCryptoApiKey.collect { key ->
                _uiState.update { it.copy(userCryptoApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userWikiApiKey.collect { key ->
                _uiState.update { it.copy(userWikiApiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userFirestoreApiKey.collect { key ->
                _uiState.update { it.copy(userFirestoreApiKey = key) }
            }
        }

        viewModelScope.launch {
            settingsRepository.blurIntensity.collect { intensity ->
                _uiState.update { it.copy(blurIntensity = intensity) }
            }
        }
        viewModelScope.launch {
            settingsRepository.glowStrength.collect { strength ->
                _uiState.update { it.copy(glowStrength = strength) }
            }
        }

        viewModelScope.launch {
            repository.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
    }

    private fun observeMemories() {
        viewModelScope.launch {
            repository.getSemanticMemoryDao().getAllMemories().collect { memories ->
                _uiState.update { it.copy(semanticMemories = memories) }
            }
        }
    }

    fun refreshDynamicSuggestions() {
        viewModelScope.launch {
            val uiMessages = _uiState.value.messages
            uiMessages.lastOrNull { it.isFromDave }?.let { lastMsg ->
                val keywords = repository.getHardwareAccelerator().extractKeywords(lastMsg.content)
                val suggestions = mutableListOf<String>()
                if (keywords.isNotEmpty()) {
                    suggestions.add("Tell me more about ${keywords.random()}")
                }
                suggestions.add("Analyze this further")
                
                // Add context-aware hardware suggestions
                val lower = lastMsg.content.lowercase()
                if (lower.contains("weather")) suggestions.add("Check 7-day forecast")
                if (lower.contains("music") || lower.contains("spotify")) suggestions.add("What's playing now?")
                
                _uiState.update { it.copy(dynamicSuggestions = suggestions.take(3)) }
            }
        }
    }

    private fun fetchUserProfile() {
        auth.currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                val profile = userStatsRepository.getUserProfile(uid)
                _uiState.update { it.copy(userProfile = profile) }
                // Trigger migration to Firestore
                repository.syncAllToFirestore(uid)
            }
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
            repository.allConversations.collect { conversations ->
                _uiState.update { it.copy(sessions = conversations) }
            }
        }
    }

    fun selectSession(sessionId: String) {
        messageCollectionJob?.cancel()
        _uiState.update { it.copy(currentSessionId = sessionId, dbMessages = emptyList(), ghostMessages = emptyList()) }
        
        messageCollectionJob = viewModelScope.launch {
            repository.getMessagesForConversation(sessionId).collect { entities ->
                val mapped = entities.map { entity ->
                    ChatMessage(
                        content = entity.content,
                        isFromDave = entity.role == "assistant",
                        mediaUrl = entity.mediaUrl,
                        mediaType = when (entity.mediaType) {
                            "IMAGE" -> MediaType.IMAGE
                            "VIDEO" -> MediaType.VIDEO
                            else -> MediaType.NONE
                        },
                        widgetType = when (entity.widgetType) {
                            "MAP" -> WidgetType.MAP
                            "HARDWARE" -> WidgetType.HARDWARE
                            "FINANCE" -> WidgetType.FINANCE
                            "FITNESS" -> WidgetType.FITNESS
                            "SPOTIFY" -> WidgetType.SPOTIFY
                            "NEWS" -> WidgetType.NEWS
                            "CALENDAR" -> WidgetType.CALENDAR
                            "USAGE" -> WidgetType.USAGE
                            else -> WidgetType.NONE
                        },
                        widgetData = entity.widgetData,
                        mood = entity.mood,
                        hasAttachment = entity.hasAttachment
                    )
                }
                _uiState.update { it.copy(dbMessages = mapped) }
            }
        }
    }

    fun createNewChat(title: String = "Neural Link") {
        viewModelScope.launch {
            val email = auth.currentUser?.email ?: "ANONYMOUS"
            val sessionId = repository.createNewConversation(title, email)
            selectSession(sessionId)
        }
    }

    fun reset() {
        messageCollectionJob?.cancel()
        _uiState.update { ChatUiState() }
    }

    fun setIsListening(isListening: Boolean) {
        _uiState.update { it.copy(isListening = isListening) }
    }

    fun setLiveMode(isActive: Boolean) {
        _uiState.update { it.copy(isLiveMode = isActive) }
    }

    fun setMode(mode: DaveMode) {
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun updateLocation(location: String?) {
        _uiState.update { it.copy(userLocation = location) }
    }

    fun addAttachment(file: AttachedFile) {
        _uiState.update { it.copy(attachedFiles = it.attachedFiles + file) }
    }

    fun speak(text: String) {
        viewModelScope.launch {
            repository.speak(text)
        }
    }

    fun stopSpeaking() {
        repository.stopSpeaking()
    }

    fun sendMessage(muteVoice: Boolean = false) {
        val currentInput = _uiState.value.inputText.trim()
        val attachments = _uiState.value.attachedFiles
        
        if (currentInput.isEmpty() && attachments.isEmpty()) return
        if (_uiState.value.isLoading) return

        val location = _uiState.value.userLocation
        val isFastMode = _uiState.value.isFastMode
        val isGodMode = _uiState.value.isGodMode
        val isGhostMode = _uiState.value.isGhostMode
        val userProfile = _uiState.value.userProfile
        val currentMode = _uiState.value.currentMode
        val uid = auth.currentUser?.uid
        val isLiveMode = _uiState.value.isLiveMode
        val persona = _uiState.value.digitalPersona
        val useIrishAccent = _uiState.value.useIrishAccent

        _uiState.update {
            it.copy(
                inputText = "",
                attachedFiles = emptyList(),
                isLoading = true,
            )
        }

        viewModelScope.launch {
            try {
                var sessionId = _uiState.value.currentSessionId
                if (sessionId == null) {
                    sessionId = repository.createNewConversation("New Intelligence Link", auth.currentUser?.email ?: "ANONYMOUS")
                    selectSession(sessionId)
                }

                if (isGhostMode) {
                    val userMsg = ChatMessage(
                        content = currentInput,
                        isFromDave = false,
                        hasAttachment = attachments.isNotEmpty()
                    )
                    _uiState.update { it.copy(ghostMessages = it.ghostMessages + userMsg) }
                }

                // Internal Command: Aura Status
                if (currentInput.lowercase() == "aura status") {
                    val role = userProfile?.role ?: "Standard"
                    val auraMsg = "AURA NETWORK DIAGNOSTICS :: Link: STABLE. Tier: $role. Signal: OPTIMAL. Vanguard Sync: ENABLED. ⚡️"
                    val daveMsg = ChatMessage(content = auraMsg, isFromDave = true, isLocal = true)
                    _uiState.update { it.copy(ghostMessages = it.ghostMessages + daveMsg, isLoading = false) }
                    return@launch
                }

                val responseText = withTimeoutOrNull(35000) {
                    repository.sendMessage(
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
                        persona = persona,
                        useIrishAccent = useIrishAccent,
                        muteVoice = muteVoice
                    )
                } ?: "Error: Dave is deep in thought and taking too long. Try again! ⏳⚡️"
                
                fetchUserProfile()

                if (responseText.contains("[ACTION: ID_VERIFY]")) {
                    _uiState.update { it.copy(pendingRoute = DaveRoute.IdentityVerification) }
                }
                
                if (isGhostMode && !responseText.startsWith("Error:")) {
                    val daveMsg = ChatMessage(
                        content = responseText,
                        isFromDave = true,
                        isLocal = responseText.contains("⚡️ ("),
                        hasAttachment = attachments.isNotEmpty()
                    )
                    _uiState.update { it.copy(ghostMessages = it.ghostMessages + daveMsg) }
                }

                refreshDynamicSuggestions()
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Send failed", e)
                val errorMsg = ChatMessage(
                    content = "Error: Send failed. Check your connection! 📡💥",
                    isFromDave = true,
                    mood = "URGENT"
                )
                _uiState.update { it.copy(ghostMessages = it.ghostMessages + errorMsg) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteConversation(sessionId)
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

    fun addSemanticMemory(key: String, value: String) {
        viewModelScope.launch {
            val memory = com.example.daveai.data.db.SemanticMemory(
                memoryType = key,
                content = value,
                timestamp = System.currentTimeMillis()
            )
            repository.getSemanticMemoryDao().insertMemory(memory)
        }
    }

    fun deleteSemanticMemory(id: Long) {
        viewModelScope.launch {
            repository.getSemanticMemoryDao().deleteMemory(id)
        }
    }

    fun strengthenSemanticMemory(id: Long) {
        viewModelScope.launch {
            val memories = _uiState.value.semanticMemories
            memories.find { it.id == id }?.let { memory ->
                val updated = memory.copy(importance = (memory.importance + 1).coerceAtMost(10))
                repository.getSemanticMemoryDao().updateMemory(updated)
            }
        }
    }

    fun archiveSemanticMemory(id: Long, archive: Boolean) {
        viewModelScope.launch {
            val memories = _uiState.value.semanticMemories
            memories.find { it.id == id }?.let { memory ->
                val updated = memory.copy(isArchived = archive)
                repository.getSemanticMemoryDao().updateMemory(updated)
            }
        }
    }

    fun clearPendingRoute() {
        _uiState.update { it.copy(pendingRoute = null) }
    }

    fun editSemanticMemory(id: Long, newContent: String) {
        viewModelScope.launch {
            val memories = _uiState.value.semanticMemories
            memories.find { it.id == id }?.let { memory ->
                val updated = memory.copy(content = newContent)
                repository.getSemanticMemoryDao().updateMemory(updated)
            }
        }
    }

    fun toggleMemoryLock(id: Long) {
        viewModelScope.launch {
            val memories = _uiState.value.semanticMemories
            memories.find { it.id == id }?.let { memory ->
                val updated = memory.copy(isLocked = !memory.isLocked)
                repository.getSemanticMemoryDao().updateMemory(updated)
            }
        }
    }

    fun searchMemories(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                observeMemories()
            } else {
                val results = repository.getSemanticMemoryDao().searchBySemanticMeaning(query)
                _uiState.update { it.copy(semanticMemories = results) }
            }
        }
    }

    fun updatePrimaryColor(color: Int) {
        viewModelScope.launch { settingsRepository.setPrimaryColor(color) }
    }

    fun updateDigitalPersona(persona: String) {
        viewModelScope.launch { settingsRepository.setDigitalPersona(persona) }
    }

    fun updateAnimationSpeed(speed: Float) {
        viewModelScope.launch { settingsRepository.setMeshAnimationSpeed(speed) }
    }

    fun updateBlurIntensity(intensity: Float) {
        viewModelScope.launch { settingsRepository.setBlurIntensity(intensity) }
    }

    fun updateGlowStrength(strength: Float) {
        viewModelScope.launch { settingsRepository.setGlowStrength(strength) }
    }

    fun toggleAutoReply(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setIsAutoReplyEnabled(enabled) }
    }

    fun updateClaudeApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserClaudeApiKey(key) }
    }

    fun updateOpenAiApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserOpenAiApiKey(key) }
    }

    fun updateSpotifyClientSecret(secret: String?) {
        viewModelScope.launch { settingsRepository.setUserSpotifyClientSecret(secret) }
    }

    fun updateNewsApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserNewsApiKey(key) }
    }

    fun updateMapsApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserMapsApiKey(key) }
    }

    fun updateGroqApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserGroqApiKey(key) }
    }

    fun updatePerplexityApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserPerplexityApiKey(key) }
    }

    fun updateElevenLabsApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserElevenLabsApiKey(key) }
    }

    fun updateWeatherApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserWeatherApiKey(key) }
    }

    fun updateFinanceApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserFinanceApiKey(key) }
    }

    fun updateGeminiApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserGeminiApiKey(key) }
    }

    fun updateSunoApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserSunoApiKey(key) }
    }

    fun updateCryptoApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserCryptoApiKey(key) }
    }

    fun updateWikiApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserWikiApiKey(key) }
    }

    fun updateFirestoreApiKey(key: String?) {
        viewModelScope.launch { settingsRepository.setUserFirestoreApiKey(key) }
    }

    fun syncIntelligence() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val partnerId = _uiState.value.partnerId
            if (partnerId != null) {
                repository.speak("Establishing neural link with partner shadow... Standby.")
            }
            val importedCount = repository.syncIntelligence()
            _uiState.update { it.copy(isLoading = false) }
            
            if (importedCount > 0) {
                _uiState.update { state ->
                    state.copy(ghostMessages = state.ghostMessages + ChatMessage(
                        content = "Neural link established. Synchronized $importedCount intelligence signals from the cloud mainframe. 🧠⚡️",
                        isFromDave = true
                    ))
                }
                repository.speak("Synchronized $importedCount signals.")
            } else if (partnerId != null) {
                repository.speak("Reality is already synchronized, boss.")
            }
        }
    }

    fun generatePairingCode() {
        viewModelScope.launch {
            val code = repository.requestPairingCode()
            _uiState.update { it.copy(pairingCode = code) }
        }
    }

    fun linkPartner(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val partnerName = repository.linkPartner(code)
            _uiState.update { it.copy(isLoading = false) }
            if (partnerName != null) {
                repository.speak("Handshake successful. Dave is now linked with $partnerName.")
            } else {
                repository.speak("Handshake failed. Verify the code and try again.")
            }
        }
    }

    fun unlinkPartner() {
        viewModelScope.launch {
            settingsRepository.setPartnerInfo(null, null)
            repository.speak("Neural link severed.")
        }
    }

    fun toggleFlashlight() {
        val newState = !_uiState.value.isFlashlightOn
        _uiState.update { it.copy(isFlashlightOn = newState) }
        repository.getHardwareAccelerator().toggleFlashlight(newState)
    }

    fun toggleDnd() {
        val newState = !_uiState.value.isDndOn
        _uiState.update { it.copy(isDndOn = newState) }
        repository.getHardwareAccelerator().toggleDND(newState)
    }

    fun closeAppFactory() {
        _uiState.update { it.copy(isBuildingApp = false, isShowingPreview = false) }
    }

    fun generateSessionMarkdown(): String {
        val messages = _uiState.value.messages
        val session = _uiState.value.sessions.find { it.id == _uiState.value.currentSessionId }
        val title = session?.title ?: "Neural Thread"
        
        return buildString {
            append("# DAVE OS :: NEURAL THREAD\n")
            append("## Title: $title\n")
            append("## Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())}\n\n")
            append("---\n\n")
            
            messages.forEach { msg ->
                val role = if (msg.isFromDave) "DAVE" else "USER"
                append("**$role**:\n${msg.content}\n\n")
            }
            
            append("---\n")
            append("*End of Transmission*\n")
        }
    }
}
