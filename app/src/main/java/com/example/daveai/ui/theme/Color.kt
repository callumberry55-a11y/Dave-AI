package com.example.daveai.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Liquid Obsidian Palette (Android 17 "Neural Fluidity")
val ObsidianDeep = Color(0xFF020405)
val ObsidianSurface = Color(0xFF0A0F12)
val NeonEmerald = Color(0xFF00FF9D)
val PulseCyan = Color(0xFF00D1FF)
val ElectricViolet = Color(0xFF9D00FF)
val GhostWhite = Color(0xFFF0F5F8)
val MutedSlate = Color(0xFF637B85)

// Light Palette (Refined Fluidity)
val primaryLight = Color(0xFF00895D)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFC0FFDF)
val onPrimaryContainerLight = Color(0xFF002114)
val secondaryLight = Color(0xFF4C6359)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFCFE9DB)
val onSecondaryContainerLight = Color(0xFF092017)
val tertiaryLight = Color(0xFF3E636D)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFC1E8F4)
val onTertiaryContainerLight = Color(0xFF001F26)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val backgroundLight = Color(0xFFF5FAF6)
val onBackgroundLight = Color(0xFF171D1A)
val surfaceLight = Color(0xFFF5FAF6)
val onSurfaceLight = Color(0xFF171D1A)

// Dark Palette (Liquid Obsidian)
val primaryDark = NeonEmerald
val onPrimaryDark = Color(0xFF003823)
val primaryContainerDark = Color(0xFF005237)
val onPrimaryContainerDark = Color(0xFF7CFFB9)
val secondaryDark = PulseCyan
val onSecondaryDark = Color(0xFF003642)
val secondaryContainerDark = Color(0xFF004E5F)
val onSecondaryContainerDark = Color(0xFFA6EEFF)
val tertiaryDark = ElectricViolet
val onTertiaryDark = Color(0xFF53008E)
val tertiaryContainerDark = Color(0xFF7600C9)
val onTertiaryContainerDark = Color(0xFFF1DBFF)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val backgroundDark = ObsidianDeep
val onBackgroundDark = GhostWhite
val surfaceDark = ObsidianSurface
val onSurfaceDark = GhostWhite

// Glass & Liquid Effects
val glassWhite = Color(0x1AFFFFFF)
val glassWhiteBorder = Color(0x33FFFFFF)
val glassBlack = Color(0x4D000000)
val glassBlackBorder = Color(0x66000000)

// Brushes
val LiquidNeuralGradient = Brush.linearGradient(
    colors = listOf(NeonEmerald, PulseCyan)
)

val ObsidianGradient = Brush.verticalGradient(
    colors = listOf(ObsidianSurface, ObsidianDeep)
)

val DavePurple = ElectricViolet
val DaveGreen = NeonEmerald
val DaveBlue = PulseCyan
