package com.example.daveai.ui.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daveai.data.db.SecurityEvent
import com.example.daveai.data.db.SecurityEventDao
import com.example.daveai.data.repository.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalUsers: Long = 0,
    val users: List<Map<String, Any>> = emptyList(),
    val recentEvents: List<SecurityEvent> = emptyList(),
    val isLoading: Boolean = false
)

class DeveloperDashboardViewModel(
    private val chatRepository: com.example.daveai.data.repository.ChatRepository,
    private val userStatsRepository: UserStatsRepository,
    private val securityEventDao: SecurityEventDao
) : ViewModel() {

    private val chatDao = chatRepository.getChatDao()

    val uiState: StateFlow<DashboardUiState> = MutableStateFlow(DashboardUiState()).asStateFlow()

    val totalUsersCount = userStatsRepository.observeTotalUserCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val allUsers = userStatsRepository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentEvents = securityEventDao.observeRecentEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serverLogs = chatRepository.serverLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalInputTokens = chatDao.observeTotalInputTokens()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalOutputTokens = chatDao.observeTotalOutputTokens()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val recentUsage = chatDao.observeRecentUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalStats = userStatsRepository.observeGlobalStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun clearLogs() {
        viewModelScope.launch {
            securityEventDao.clearOldEvents(System.currentTimeMillis() + 1000) // Clear all
        }
    }
    
    fun elevateUser(uid: String) {
        viewModelScope.launch {
            userStatsRepository.elevateToMasterDeveloper(uid)
        }
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            userStatsRepository.deleteUserData(uid)
        }
    }
}
