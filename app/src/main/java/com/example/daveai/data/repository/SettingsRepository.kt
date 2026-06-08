package com.example.daveai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context,
    val securityRepository: SecurityRepository
) {

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

        // Legacy API Keys in DataStore (for migration)
        private val LEGACY_CLAUDE_KEY = stringPreferencesKey("user_claude_api_key")
        private val LEGACY_OPENAI_KEY = stringPreferencesKey("user_openai_api_key")
        private val LEGACY_VAULT_CODE = stringPreferencesKey("vault_security_code")
        
        private val USE_BIOMETRICS_FOR_VAULT = booleanPreferencesKey("use_biometrics_for_vault")
        private val BLUR_INTENSITY = floatPreferencesKey("blur_intensity")
        private val GLOW_STRENGTH = floatPreferencesKey("glow_strength")
        
        const val DEFAULT_COLOR = 0xFF00E676.toInt() // DaveGreen
    }

    private val _claudeKeyFlow = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_CLAUDE_API))
    val userClaudeApiKey = _claudeKeyFlow.asStateFlow()

    private val _openaiKeyFlow = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_OPENAI_API))
    val userOpenAiApiKey = _openaiKeyFlow.asStateFlow()

    private val _vaultCodeFlow = MutableStateFlow(securityRepository.getVaultSecurityCode())
    val vaultSecurityCode = _vaultCodeFlow.asStateFlow()

    init {
        migrateSensitiveData()
    }

    private fun migrateSensitiveData() {
        runBlocking {
            val prefs = context.dataStore.data.first()
            
            prefs[LEGACY_CLAUDE_KEY]?.let {
                securityRepository.setEncryptedString(SecurityRepository.KEY_CLAUDE_API, it)
                _claudeKeyFlow.value = it
                context.dataStore.edit { it.remove(LEGACY_CLAUDE_KEY) }
            }
            
            prefs[LEGACY_OPENAI_KEY]?.let {
                securityRepository.setEncryptedString(SecurityRepository.KEY_OPENAI_API, it)
                _openaiKeyFlow.value = it
                context.dataStore.edit { it.remove(LEGACY_OPENAI_KEY) }
            }

            prefs[LEGACY_VAULT_CODE]?.let {
                securityRepository.setVaultSecurityCode(it)
                _vaultCodeFlow.value = it
                context.dataStore.edit { it.remove(LEGACY_VAULT_CODE) }
            }
        }
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

    // New flows for encrypted data
    val userSpotifyClientSecret: Flow<String?> = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_SPOTIFY_SECRET))
    val userNewsApiKey: Flow<String?> = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_NEWS_API))
    val userMapsApiKey: Flow<String?> = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_MAPS_API))
    val userGroqApiKey: Flow<String?> = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_GROQ_API))
    val userPerplexityApiKey: Flow<String?> = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_PERPLEXITY_API))
    val userElevenLabsApiKey: Flow<String?> = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_ELEVENLABS_API))
    val userWeatherApiKey: Flow<String?> = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_WEATHER_API))
    val userFinanceApiKey: Flow<String?> = MutableStateFlow(securityRepository.getEncryptedString(SecurityRepository.KEY_FINANCE_API))

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
        securityRepository.setEncryptedString(SecurityRepository.KEY_CLAUDE_API, key)
        _claudeKeyFlow.value = key
    }

    suspend fun setUserOpenAiApiKey(key: String?) {
        securityRepository.setEncryptedString(SecurityRepository.KEY_OPENAI_API, key)
        _openaiKeyFlow.value = key
    }

    suspend fun setUserSpotifyClientSecret(secret: String?) {
        securityRepository.setEncryptedString(SecurityRepository.KEY_SPOTIFY_SECRET, secret)
    }

    suspend fun setUserNewsApiKey(key: String?) {
        securityRepository.setEncryptedString(SecurityRepository.KEY_NEWS_API, key)
    }

    suspend fun setUserMapsApiKey(key: String?) {
        securityRepository.setEncryptedString(SecurityRepository.KEY_MAPS_API, key)
    }

    suspend fun setUserGroqApiKey(key: String?) {
        securityRepository.setEncryptedString(SecurityRepository.KEY_GROQ_API, key)
    }

    suspend fun setUserPerplexityApiKey(key: String?) {
        securityRepository.setEncryptedString(SecurityRepository.KEY_PERPLEXITY_API, key)
    }

    suspend fun setUserElevenLabsApiKey(key: String?) {
        securityRepository.setEncryptedString(SecurityRepository.KEY_ELEVENLABS_API, key)
    }

    suspend fun setUserWeatherApiKey(key: String?) {
        securityRepository.setEncryptedString(SecurityRepository.KEY_WEATHER_API, key)
    }

    suspend fun setUserFinanceApiKey(key: String?) {
        securityRepository.setEncryptedString(SecurityRepository.KEY_FINANCE_API, key)
    }

    suspend fun setVaultSecurityCode(code: String?) {
        securityRepository.setVaultSecurityCode(code)
        _vaultCodeFlow.value = code
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
}
