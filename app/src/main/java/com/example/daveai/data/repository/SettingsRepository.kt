package com.example.daveai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        private val IS_AUTO_REPLY_ENABLED = booleanPreferencesKey("is_auto_reply_enabled")
        private val USE_IRISH_ACCENT = booleanPreferencesKey("use_irish_accent")
        private val PARTNER_ID = stringPreferencesKey("partner_id")
        private val PARTNER_NAME = stringPreferencesKey("partner_name")
        private val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")

        // API Keys
        private val USER_CLAUDE_API_KEY = stringPreferencesKey("user_claude_api_key")
        private val USER_OPENAI_API_KEY = stringPreferencesKey("user_openai_api_key")
        private val USER_SPOTIFY_CLIENT_SECRET = stringPreferencesKey("user_spotify_client_secret")
        private val USER_NEWS_API_KEY = stringPreferencesKey("user_news_api_key")
        private val USER_MAPS_API_KEY = stringPreferencesKey("user_maps_api_key")
        private val USER_GROQ_API_KEY = stringPreferencesKey("user_groq_api_key")
        private val USER_PERPLEXITY_API_KEY = stringPreferencesKey("user_perplexity_api_key")
        private val USER_ELEVENLABS_API_KEY = stringPreferencesKey("user_elevenlabs_api_key")
        private val USER_WEATHER_API_KEY = stringPreferencesKey("user_weather_api_key")
        private val USER_FINANCE_API_KEY = stringPreferencesKey("user_finance_api_key")

        // Vault Security
        private val VAULT_SECURITY_CODE = stringPreferencesKey("vault_security_code")
        private val USE_BIOMETRICS_FOR_VAULT = booleanPreferencesKey("use_biometrics_for_vault")
        
        private val BLUR_INTENSITY = floatPreferencesKey("blur_intensity")
        private val GLOW_STRENGTH = floatPreferencesKey("glow_strength")
        
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

    val isAutoReplyEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_AUTO_REPLY_ENABLED] ?: false
    }

    val useIrishAccent: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_IRISH_ACCENT] ?: false
    }

    val partnerId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PARTNER_ID]
    }

    val partnerName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PARTNER_NAME]
    }

    val lastSyncTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIMESTAMP] ?: 0L
    }

    val userClaudeApiKey: Flow<String?> = context.dataStore.data.map { it[USER_CLAUDE_API_KEY] }
    val userOpenAiApiKey: Flow<String?> = context.dataStore.data.map { it[USER_OPENAI_API_KEY] }
    val userSpotifyClientSecret: Flow<String?> = context.dataStore.data.map { it[USER_SPOTIFY_CLIENT_SECRET] }
    val userNewsApiKey: Flow<String?> = context.dataStore.data.map { it[USER_NEWS_API_KEY] }
    val userMapsApiKey: Flow<String?> = context.dataStore.data.map { it[USER_MAPS_API_KEY] }
    val userGroqApiKey: Flow<String?> = context.dataStore.data.map { it[USER_GROQ_API_KEY] }
    val userPerplexityApiKey: Flow<String?> = context.dataStore.data.map { it[USER_PERPLEXITY_API_KEY] }
    val userElevenLabsApiKey: Flow<String?> = context.dataStore.data.map { it[USER_ELEVENLABS_API_KEY] }
    val userWeatherApiKey: Flow<String?> = context.dataStore.data.map { it[USER_WEATHER_API_KEY] }
    val userFinanceApiKey: Flow<String?> = context.dataStore.data.map { it[USER_FINANCE_API_KEY] }

    val vaultSecurityCode: Flow<String?> = context.dataStore.data.map { it[VAULT_SECURITY_CODE] }
    val useBiometricsForVault: Flow<Boolean> = context.dataStore.data.map { it[USE_BIOMETRICS_FOR_VAULT] ?: false }

    val blurIntensity: Flow<Float> = context.dataStore.data.map { it[BLUR_INTENSITY] ?: 0.5f }
    val glowStrength: Flow<Float> = context.dataStore.data.map { it[GLOW_STRENGTH] ?: 0.5f }

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

    suspend fun setIsAutoReplyEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_AUTO_REPLY_ENABLED] = enabled
        }
    }

    suspend fun setUseIrishAccent(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_IRISH_ACCENT] = use
        }
    }

    suspend fun setPartnerInfo(id: String?, name: String?) {
        context.dataStore.edit { preferences ->
            if (id == null) preferences.remove(PARTNER_ID) else preferences[PARTNER_ID] = id
            if (name == null) preferences.remove(PARTNER_NAME) else preferences[PARTNER_NAME] = name
        }
    }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    suspend fun setUserClaudeApiKey(key: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_CLAUDE_API_KEY, key) }
    }

    suspend fun setUserOpenAiApiKey(key: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_OPENAI_API_KEY, key) }
    }

    suspend fun setUserSpotifyClientSecret(secret: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_SPOTIFY_CLIENT_SECRET, secret) }
    }

    suspend fun setUserNewsApiKey(key: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_NEWS_API_KEY, key) }
    }

    suspend fun setUserMapsApiKey(key: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_MAPS_API_KEY, key) }
    }

    suspend fun setUserGroqApiKey(key: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_GROQ_API_KEY, key) }
    }

    suspend fun setUserPerplexityApiKey(key: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_PERPLEXITY_API_KEY, key) }
    }

    suspend fun setUserElevenLabsApiKey(key: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_ELEVENLABS_API_KEY, key) }
    }

    suspend fun setUserWeatherApiKey(key: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_WEATHER_API_KEY, key) }
    }

    suspend fun setUserFinanceApiKey(key: String?) {
        context.dataStore.edit { it.updateOrRemove(USER_FINANCE_API_KEY, key) }
    }

    suspend fun setVaultSecurityCode(code: String?) {
        context.dataStore.edit { it.updateOrRemove(VAULT_SECURITY_CODE, code) }
    }

    suspend fun setUseBiometricsForVault(use: Boolean) {
        context.dataStore.edit { it[USE_BIOMETRICS_FOR_VAULT] = use }
    }

    suspend fun setBlurIntensity(intensity: Float) {
        context.dataStore.edit { it[BLUR_INTENSITY] = intensity }
    }

    suspend fun setGlowStrength(strength: Float) {
        context.dataStore.edit { it[GLOW_STRENGTH] = strength }
    }

    private fun MutablePreferences.updateOrRemove(key: Preferences.Key<String>, value: String?) {
        if (value.isNullOrBlank()) remove(key) else this[key] = value
    }
}
