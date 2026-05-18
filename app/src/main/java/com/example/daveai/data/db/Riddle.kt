package com.example.daveai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "riddle_table")
data class Riddle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,         // Dave reads this via TTS
    val answerKeyword: String,    // The exact word to scan for (lowercase)
    val hint: String,             // Triggered when Jack/user gets stuck
    val tier: Int,                // Progression level (1 = Casual, 2 = Enigma, etc.)
    val isSolved: Boolean = false // Keeps track of progress
)

fun verifyUserAnswer(userInput: String, currentRiddle: Riddle): AnswerResult {
    // 1. Clean the input: lowercase it and strip out punctuation
    val cleanedInput = userInput.lowercase().trim()
    val targetKeyword = currentRiddle.answerKeyword.lowercase()

    // 2. Scan for a match
    return if (cleanedInput.contains(targetKeyword)) {
        AnswerResult.CORRECT
    } else {
        AnswerResult.INCORRECT
    }
}

enum class AnswerResult {
    CORRECT, INCORRECT
}
