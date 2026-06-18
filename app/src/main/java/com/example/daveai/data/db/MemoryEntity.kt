package com.example.daveai.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "memories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["email"],
            childColumns = ["userEmail"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userEmail"])]
)
data class MemoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userEmail: String,
    val content: String,
    val vectorEmbedding: FloatArray?, // size 168 handled by app logic
    val sourceTitle: String?,
    val tags: List<String>?
)
