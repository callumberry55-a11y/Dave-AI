package com.example.daveai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_events")
data class SecurityEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // e.g., "VAULT_AUTH_SUCCESS", "VAULT_AUTH_FAILURE", "DEV_HANDSHAKE_SUCCESS", "DEV_HANDSHAKE_FAILURE"
    val details: String? = null,
    val severity: String = "INFO" // INFO, WARNING, CRITICAL
)
