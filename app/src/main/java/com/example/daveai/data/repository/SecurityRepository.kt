package com.example.daveai.data.repository

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.daveai.data.db.SecurityEvent
import com.example.daveai.data.db.SecurityEventDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SecurityRepository(
    context: Context,
    private val securityEventDao: SecurityEventDao
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "dave_secure_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    fun logSecurityEvent(type: String, details: String? = null, severity: String = "INFO") {
        repositoryScope.launch {
            try {
                securityEventDao.insertEvent(
                    SecurityEvent(
                        eventType = type,
                        details = details,
                        severity = severity
                    )
                )
                Log.d("SecurityRepo", "Logged security event: $type")
            } catch (e: Exception) {
                Log.e("SecurityRepo", "Failed to log security event", e)
            }
        }
    }

    fun setEncryptedString(key: String, value: String?) {
        if (value.isNullOrBlank()) {
            sharedPrefs.edit().remove(key).apply()
        } else {
            sharedPrefs.edit().putString(key, value).apply()
        }
    }

    fun getEncryptedString(key: String): String? {
        return sharedPrefs.getString(key, null)
    }

    fun setVaultSecurityCode(code: String?) {
        setEncryptedString("vault_security_code", code)
        logSecurityEvent(
            type = if (code != null) "VAULT_CODE_UPDATED" else "VAULT_CODE_REMOVED",
            severity = if (code != null) "INFO" else "WARNING"
        )
    }

    fun getVaultSecurityCode(): String? = getEncryptedString("vault_security_code")

    companion object {
        const val KEY_CLAUDE_API = "user_claude_api_key"
        const val KEY_OPENAI_API = "user_openai_api_key"
        const val KEY_SPOTIFY_SECRET = "user_spotify_client_secret"
        const val KEY_NEWS_API = "user_news_api_key"
        const val KEY_MAPS_API = "user_maps_api_key"
        const val KEY_GROQ_API = "user_groq_api_key"
        const val KEY_PERPLEXITY_API = "user_perplexity_api_key"
        const val KEY_ELEVENLABS_API = "user_elevenlabs_api_key"
        const val KEY_WEATHER_API = "user_weather_api_key"
        const val KEY_FINANCE_API = "user_finance_api_key"
    }
}
