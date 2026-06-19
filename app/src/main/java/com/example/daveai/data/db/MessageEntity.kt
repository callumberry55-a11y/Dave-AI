package com.example.daveai.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: String,
    val content: String,
    val mediaUrl: String? = null,
    val mediaType: String = "NONE", // NONE, IMAGE, VIDEO
    val widgetType: String = "NONE", // NONE, MAP, HARDWARE, etc.
    val widgetData: String? = null, // JSON string
    val timestamp: Date? = Date(),
    val mood: String = "NEUTRAL",
    val hasAttachment: Boolean = false,
    val inputTokens: Int = 0,
    val outputTokens: Int = 0
)
