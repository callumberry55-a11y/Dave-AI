package com.example.daveai.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface DaveRoute : NavKey {
    @Serializable
    data object Auth : DaveRoute

    @Serializable
    data object Landing : DaveRoute

    @Serializable
    data object Chat : DaveRoute

    @Serializable
    data object Riddle : DaveRoute

    @Serializable
    data object Lessons : DaveRoute
}
