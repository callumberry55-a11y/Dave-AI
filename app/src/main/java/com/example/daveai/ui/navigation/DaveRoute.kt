package com.example.daveai.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface DaveRoute {
    @Serializable data object Auth : DaveRoute, NavKey
    @Serializable data object Landing : DaveRoute, NavKey
    @Serializable data object Chat : DaveRoute, NavKey
    @Serializable data object Riddle : DaveRoute, NavKey
    @Serializable data object Lessons : DaveRoute, NavKey
    @Serializable data object LiveVoice : DaveRoute, NavKey
    @Serializable data object Terminal : DaveRoute, NavKey
    @Serializable data object Sanctum : DaveRoute, NavKey
}
