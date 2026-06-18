package com.example.daveai.util

import kotlin.math.sqrt

object VectorUtils {

    /**
     * Calculates the cosine similarity between two vectors.
     * Higher value means more similar (max 1.0).
     */
    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.size != v2.size) return 0f
        
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            normA += v1[i] * v1[i]
            normB += v2[i] * v2[i]
        }
        
        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 0) dotProduct / denominator.toFloat() else 0f
    }

    /**
     * Finds the top N items from a list that are most similar to the target vector.
     */
    fun <T> findMostSimilar(
        target: FloatArray,
        items: List<T>,
        vectorExtractor: (T) -> FloatArray?,
        limit: Int = 5
    ): List<T> {
        return items
            .asSequence()
            .map { item ->
                val vector = vectorExtractor(item)
                val score = if (vector != null) cosineSimilarity(target, vector) else -1f
                item to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
            .toList()
    }
}
