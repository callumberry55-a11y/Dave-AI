package com.example.daveai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityEventDao {
    @Insert
    suspend fun insertEvent(event: SecurityEvent)

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC LIMIT 100")
    fun observeRecentEvents(): Flow<List<SecurityEvent>>

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecentEvents(): List<SecurityEvent>

    @Query("DELETE FROM security_events WHERE timestamp < :expiry")
    suspend fun clearOldEvents(expiry: Long)
}
