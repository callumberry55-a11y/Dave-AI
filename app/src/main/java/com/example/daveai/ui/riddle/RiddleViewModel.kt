package com.example.daveai.ui.riddle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daveai.data.db.AnswerResult
import com.example.daveai.data.db.Riddle
import com.example.daveai.data.db.RiddleDao
import com.example.daveai.data.db.verifyUserAnswer
import com.example.daveai.data.repository.ChatRepository
import com.example.daveai.data.repository.SettingsRepository
import com.example.daveai.data.repository.UserProfile
import com.example.daveai.data.repository.UserStatsRepository
import com.example.daveai.util.DaveVoiceManager
import com.example.daveai.util.RiddleSoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RiddleUiState(
    val currentRiddle: Riddle? = null,
    val inputText: String = "",
    val isSolved: Boolean = false,
    val showHint: Boolean = false,
    val isLoading: Boolean = true,
    val solvedCount: Int = 0,
    val totalCount: Int = 0,
    val streak: Int = 0,
    val tierName: String = "CASUAL",
    val errorTrigger: Int = 0,
    val userProfile: UserProfile? = null,
    val sessions: List<com.example.daveai.data.db.ConversationEntity> = emptyList(),
    val glowStrength: Float = 0.5f,
    val blurIntensity: Float = 0.5f,
)

class RiddleViewModel(
    private val riddleDao: RiddleDao,
    private val voiceManager: DaveVoiceManager,
    private val soundManager: RiddleSoundManager,
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val userStatsRepository = UserStatsRepository()

    private val _uiState = MutableStateFlow(RiddleUiState())
    val uiState: StateFlow<RiddleUiState> = _uiState.asStateFlow()
    
    private val skippedIds = mutableListOf<Int>()

    init {
        loadProgress()
        viewModelScope.launch {
            chatRepository.allConversations.collect { conversations ->
                _uiState.update { it.copy(sessions = conversations) }
            }
        }
        viewModelScope.launch {
            settingsRepository.glowStrength.collect { strength ->
                _uiState.update { it.copy(glowStrength = strength) }
            }
        }
        viewModelScope.launch {
            settingsRepository.blurIntensity.collect { intensity ->
                _uiState.update { it.copy(blurIntensity = intensity) }
            }
        }
        viewModelScope.launch {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            auth.currentUser?.uid?.let { uid ->
                val profile = userStatsRepository.getUserProfile(uid)
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            riddleDao.getAllRiddles().collect { riddles ->
                _uiState.update { it.copy(
                    solvedCount = riddles.count { r -> r.isSolved },
                    totalCount = riddles.size
                ) }
            }
        }
    }

    fun loadNextRiddle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSolved = false, showHint = false, inputText = "") }
            var riddle = riddleDao.getNextUnsolvedRiddle(skippedIds)
            
            if (riddle == null) {
                // Procedurally generate new riddles!
                _uiState.update { it.copy(isLoading = true) }
                voiceManager.speak("Forging new challenges in the depths of the vault...")
                chatRepository.generateProceduralRiddles(5)
                riddle = riddleDao.getNextUnsolvedRiddle(skippedIds) // Try fetching again
            }

            _uiState.update { state -> 
                state.copy(
                    currentRiddle = riddle, 
                    isLoading = false,
                    tierName = when(riddle?.tier) {
                        1 -> "CASUAL"
                        2 -> "EXPLORER"
                        3 -> "MASTER"
                        4 -> "ELITE"
                        5 -> "LEGENDARY"
                        else -> "UNKNOWN"
                    }
                ) 
            }
            
            riddle?.let {
                voiceManager.speak("Next challenge: ${it.question}")
            } ?: run {
                voiceManager.speak("Incredible! You've conquered every riddle in the vault. You are truly elite!")
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val currentRiddle = state.currentRiddle ?: return
        
        val result = verifyUserAnswer(state.inputText, currentRiddle)
        
        if (result == AnswerResult.CORRECT) {
            soundManager.playCorrect()
            _uiState.update { it.copy(isSolved = true, streak = it.streak + 1) }
            viewModelScope.launch {
                riddleDao.markAsSolved(currentRiddle.id)
                voiceManager.speak("Correct! Spot on, boss.")
            }
        } else {
            soundManager.playWrong()
            _uiState.update { it.copy(streak = 0, errorTrigger = it.errorTrigger + 1) }
            viewModelScope.launch {
                voiceManager.speak("Not quite. Want a hint, or should I read it again?")
            }
        }
    }

    fun toggleHint() {
        _uiState.update { it.copy(showHint = !it.showHint) }
        if (_uiState.value.showHint) {
            viewModelScope.launch {
                _uiState.value.currentRiddle?.hint?.let { voiceManager.speak(it) }
            }
        }
    }

    fun skipRiddle() {
        val state = _uiState.value
        val currentRiddle = state.currentRiddle ?: return
        skippedIds.add(currentRiddle.id)
        
        // Reset streak and move on
        soundManager.playWrong() // Maybe play a "skip" sound, but wrong works for now
        _uiState.update { it.copy(streak = 0) }
        viewModelScope.launch {
            voiceManager.speak("Skipping that one. The answer was ${currentRiddle.answerKeyword}. Moving on!")
            loadNextRiddle()
        }
    }

    fun updateGlowStrength(strength: Float) {
        viewModelScope.launch {
            settingsRepository.setGlowStrength(strength)
        }
    }

    fun updateBlurIntensity(intensity: Float) {
        viewModelScope.launch {
            settingsRepository.setBlurIntensity(intensity)
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "ANONYMOUS"
            chatRepository.createNewConversation("New Neural Thread", email)
        }
    }

    fun logout() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
    }
}
