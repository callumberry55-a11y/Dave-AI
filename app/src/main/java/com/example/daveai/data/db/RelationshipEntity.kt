package com.example.daveai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relationship_ledger")
data class RelationshipEntity(
    @PrimaryKey val id: Int = 1, // Singleton
    val rapportLevel: Int = 50, // 0 to 100
    val insideJokes: String = "",
    val ongoingEmotionalArcs: String = "",
    val sharedExperiences: String = "",
    val monitoredKeywords: String = "" // Comma-separated list of items for Dave to track
)
