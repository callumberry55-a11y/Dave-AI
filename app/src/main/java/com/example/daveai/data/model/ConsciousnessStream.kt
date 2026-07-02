package com.example.daveai.data.model

data class Thought(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: ThoughtType,
    val content: String,
    val urgency: Float = 0.1f // 0.0 to 1.0
)

enum class ThoughtType {
    REFLECTION, OBSERVATION, PLANNING, EMOTION, CURIOSITY
}
