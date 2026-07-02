package com.example.daveai.util

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions

class SemanticMemoryManager(private val context: Context) {
    private var textEmbedder: TextEmbedder? = null

    init {
        try {
            val options = TextEmbedderOptions.builder()
                .setBaseOptions(
                    BaseOptions.builder()
                        .setModelAssetPath("universal_sentence_encoder.tflite")
                        .build()
                )
                .build()
            
            textEmbedder = TextEmbedder.createFromOptions(context, options)
            Log.d("SemanticMemory", "TextEmbedder initialized successfully.")
        } catch (e: Exception) {
            Log.e("SemanticMemory", "Failed to initialize TextEmbedder: ${e.message}")
        }
    }

    fun getEmbedding(text: String): FloatArray? {
        return try {
            val result = textEmbedder?.embed(text)
            result?.embeddingResult()?.embeddings()?.firstOrNull()?.floatEmbedding()
        } catch (e: Exception) {
            Log.e("SemanticMemory", "Embedding generation failed: ${e.message}")
            null
        }
    }

    companion object {
        fun cosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
            var dotProduct = 0.0f
            var normA = 0.0f
            var normB = 0.0f
            for (i in vectorA.indices) {
                dotProduct += vectorA[i] * vectorB[i]
                normA += vectorA[i] * vectorA[i]
                normB += vectorB[i] * vectorB[i]
            }
            return if (normA == 0.0f || normB == 0.0f) 0.0f 
                   else dotProduct / (Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble())).toFloat()
        }
    }
}
