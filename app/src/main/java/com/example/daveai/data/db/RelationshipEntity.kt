package com.example.daveai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relationship_ledger")
data class RelationshipEntity(
    @PrimaryKey val id: Int = 1,
    val rapportLevel: Int = 10,
    val insideJokes: String = "",
    val ongoingEmotionalArcs: String = "",
    val sharedExperiences: String = "",
    val monitoredKeywords: String = "",
    val partnerId: String? = null,
    val partnerName: String? = null,
    val lastInteractionSentiment: String = "NEUTRAL",
    val totalInteractions: Int = 0,
    val lastInteractionTimestamp: Long = System.currentTimeMillis(),
    val currentMood: String = "NEUTRAL"
)
