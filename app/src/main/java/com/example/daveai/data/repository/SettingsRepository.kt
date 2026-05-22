package com.example.daveai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val PRIMARY_COLOR = intPreferencesKey("primary_color")
        private val USE_SYSTEM_WALLPAPER = booleanPreferencesKey("use_system_wallpaper")
        private val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        private val DIGITAL_PERSONA = stringPreferencesKey("digital_persona")
        private val CYBER_INTENSITY = floatPreferencesKey("cyber_intensity")
        private val MESH_ANIMATION_SPEED = floatPreferencesKey("mesh_animation_speed")
        private val TYPOGRAPHY_STYLE = stringPreferencesKey("typography_style")
        private val IS_MOOD_REACTIVE = booleanPreferencesKey("is_mood_reactive")
        private val USE_IRISH_ACCENT = booleanPreferencesKey("use_irish_accent")
        
        const val DEFAULT_COLOR = 0xFF00E676.toInt() // DaveGreen
    }

    val primaryColor: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PRIMARY_COLOR] ?: DEFAULT_COLOR
    }

    val useSystemWallpaper: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_SYSTEM_WALLPAPER] ?: false
    }

    val customWallpaperUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_WALLPAPER_URI]
    }

    val digitalPersona: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DIGITAL_PERSONA] ?: "HACKER"
    }

    val cyberIntensity: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[CYBER_INTENSITY] ?: 0.8f
    }

    val meshAnimationSpeed: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[MESH_ANIMATION_SPEED] ?: 1.0f
    }

    val typographyStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TYPOGRAPHY_STYLE] ?: "MODERN"
    }

    val isMoodReactive: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_MOOD_REACTIVE] ?: false
    }

    val useIrishAccent: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_IRISH_ACCENT] ?: false
    }

    suspend fun setPrimaryColor(color: Int) {
        context.dataStore.edit { preferences ->
            preferences[PRIMARY_COLOR] = color
        }
    }

    suspend fun setUseSystemWallpaper(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_SYSTEM_WALLPAPER] = use
        }
    }

    suspend fun setCustomWallpaperUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) preferences.remove(CUSTOM_WALLPAPER_URI)
            else preferences[CUSTOM_WALLPAPER_URI] = uri
        }
    }

    suspend fun setDigitalPersona(persona: String) {
        context.dataStore.edit { preferences ->
            preferences[DIGITAL_PERSONA] = persona
        }
    }

    suspend fun setCyberIntensity(intensity: Float) {
        context.dataStore.edit { preferences ->
            preferences[CYBER_INTENSITY] = intensity
        }
    }

    suspend fun setMeshAnimationSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[MESH_ANIMATION_SPEED] = speed
        }
    }

    suspend fun setTypographyStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[TYPOGRAPHY_STYLE] = style
        }
    }

    suspend fun setIsMoodReactive(reactive: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_MOOD_REACTIVE] = reactive
        }
    }

    suspend fun setUseIrishAccent(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_IRISH_ACCENT] = use
        }
    }
}
