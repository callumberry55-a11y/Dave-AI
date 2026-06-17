package com.example.daveai.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val role: String,
    val content: String,
    val mediaUrl: String? = null,
    val mediaType: String = "NONE", // NONE, IMAGE, VIDEO
    val widgetType: String = "NONE", // NONE, MAP, HARDWARE
    val widgetData: String? = null, // JSON string for widget specifics
    val timestamp: Long = System.currentTimeMillis(),
    val mood: String = "NEUTRAL",
    val hasAttachment: Boolean = false,
    val attachmentNames: String? = null, // Comma-separated list
    val inputTokens: Int = 0,
    val outputTokens: Int = 0
)
