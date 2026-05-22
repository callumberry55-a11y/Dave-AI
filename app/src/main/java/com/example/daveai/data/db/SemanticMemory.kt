package com.example.daveai.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "semantic_memory")
data class SemanticMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "memory_type")
    val memoryType: String,
    val content: String,
    val timestamp: Long,
    val importance: Int = 0, // 0 to 10
    @ColumnInfo(name = "access_count")
    val accessCount: Int = 0,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false
)
