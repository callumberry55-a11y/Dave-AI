package com.example.daveai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val email: String,
    val createdAt: Date,
    val displayName: String?,
    val avatarUrl: String?
)
