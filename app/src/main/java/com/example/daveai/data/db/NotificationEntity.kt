package com.example.daveai.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 50")
    fun getRecentNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getNotificationsSince(since: Long): List<NotificationEntity>

    @Query("DELETE FROM notifications WHERE timestamp < :before")
    suspend fun pruneOldNotifications(before: Long)
}
