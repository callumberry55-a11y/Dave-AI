package com.example.daveai.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface DaveRoute {
    @Serializable data object Auth : DaveRoute, NavKey
    @Serializable data object Landing : DaveRoute, NavKey
    @Serializable data object Chat : DaveRoute, NavKey
    @Serializable data object Riddle : DaveRoute, NavKey
    @Serializable data object LiveVoice : DaveRoute, NavKey
    @Serializable data object DeveloperDashboard : DaveRoute, NavKey
    @Serializable data object Sanctum : DaveRoute, NavKey
    @Serializable data object Vault : DaveRoute, NavKey
    @Serializable data object SecuritySetup : DaveRoute, NavKey
    @Serializable data object VaultAuth : DaveRoute, NavKey
    @Serializable data object AuraMarketplace : DaveRoute, NavKey
    @Serializable data object PersonalityEditor : DaveRoute, NavKey
    @Serializable data object IdentityVerification : DaveRoute, NavKey
}
