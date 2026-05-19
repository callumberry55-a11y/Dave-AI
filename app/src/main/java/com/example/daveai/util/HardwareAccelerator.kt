package com.example.daveai.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.mlkit.genai.prompt.Generation

/**
 * Manages access to the Google Tensor TPU, AICore, and Android System Intelligence features.
 */
class HardwareAccelerator(@Suppress("unused") private val context: Context) {

    /**
     * Checks if the device has a Google Tensor chip (Pixel 6+)
     */
    fun isTensorDevice(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MANUFACTURER.equals("Google", ignoreCase = true)
        } else {
            Build.HARDWARE.contains("tensor", ignoreCase = true)
        }
    }

    /**
     * Checks if Android AICore is available.
     * AICore manages Gemini Nano on supported Pixel and Samsung devices.
     */
    fun isAICoreAvailable(): Boolean {
        // AICore requires Android 14+ and specific hardware support
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    /**
     * Executes a prompt directly on the device TPU using Gemini Nano via AICore.
     * This is the fastest path for text tasks and ensures user privacy.
     */
    suspend fun generateOnDevice(prompt: String): String? {
        if (!isAICoreAvailable()) return null
        
        Log.d("HardwareAccelerator", "Routing task to System Intelligence / AICore TPU: $prompt")
        return try {
            val generativeModel = Generation.getClient()
            val response = generativeModel.generateContent(prompt)
            response.candidates.firstOrNull()?.text
        } catch (e: Exception) {
            Log.e("HardwareAccelerator", "Failed on-device execution: ${e.message}", e)
            null
        }
    }

    suspend fun summarizeLocally(text: String): String? {
        if (!isAICoreAvailable()) return null
        return generateOnDevice("Summarize the following text concisely:\n\n$text")
    }

    suspend fun proofreadLocally(text: String): String? {
        if (!isAICoreAvailable()) return null
        return generateOnDevice("Proofread and correct the grammar of the following text. Only output the corrected text:\n\n$text")
    }

    suspend fun rewriteLocally(text: String): String? {
        if (!isAICoreAvailable()) return null
        return generateOnDevice("Rewrite the following text to be more professional and clear. Only output the rewritten text:\n\n$text")
    }



    /**
     * Instructions for Dave to utilize Android System Intelligence for 
     * features like smart replies and rephrasing.
     */
    fun getSystemIntelligenceIntegrationPrompt(): String {
        return """
            ANDROID SYSTEM INTELLIGENCE & AICORE:
            - You have direct access to Android System Intelligence for local context.
            - Gemini Nano (via AICore) is active on this device's TPU.
            - Use the TPU for rapid text processing and private user data handling.
            - Leverage Android 15+ System Intelligence APIs for real-time proactive assistance.
        """.trimIndent()
    }
}
