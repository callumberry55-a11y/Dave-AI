package com.example.daveai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val sessionId: String = UUID.randomUUID().toString(),
    val title: String,
    val lastMessageTimestamp: Long = System.currentTimeMillis()
)
