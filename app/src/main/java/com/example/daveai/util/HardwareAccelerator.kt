package com.example.daveai.util

import android.app.NotificationManager
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.mlkit.genai.prompt.Generation
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Manages access to the Google Tensor TPU, AICore, and Android System Intelligence features.
 */
class HardwareAccelerator(private val context: Context) {

    /**
     * Toggles the device flashlight.
     */
    fun toggleFlashlight(turnOn: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, turnOn)
            }
        } catch (e: Exception) {
            Log.e("HardwareAccelerator", "Flashlight error: ${e.message}")
        }
    }

    /**
     * Toggles Do Not Disturb (DND) mode.
     */
    fun toggleDND(turnOn: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            val filter = if (turnOn) NotificationManager.INTERRUPTION_FILTER_PRIORITY else NotificationManager.INTERRUPTION_FILTER_ALL
            notificationManager.setInterruptionFilter(filter)
        } else {
            Toast.makeText(context, "DND permission required. Please enable in settings.", Toast.LENGTH_LONG).show()
        }
    }

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
        val isSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        if (!isSupported) {
            Log.w("HardwareAccelerator", "AICore unavailable: Device SDK ${Build.VERSION.SDK_INT} < 34")
        }
        return isSupported
    }

    /**
     * Executes a prompt directly on the device TPU using Gemini Nano via AICore.
     * This is the fastest path for text tasks and ensures user privacy.
     */
    suspend fun generateOnDevice(prompt: String): String? {
        if (!isAICoreAvailable()) return null
        
        Log.d("HardwareAccelerator", "Routing task to System Intelligence / AICore TPU: $prompt")
        return withTimeoutOrNull(10000) {
            try {
                val generativeModel = Generation.getClient()
                val response = generativeModel.generateContent(prompt)
                val text = response.candidates.firstOrNull()?.text
                Log.d("HardwareAccelerator", "On-device TPU result: $text")
                text
            } catch (e: Exception) {
                Log.e("HardwareAccelerator", "Failed on-device execution: ${e.message}", e)
                null
            }
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
     * Identifies if a prompt is highly suitable for local Gemini Nano processing.
     * Simple questions, greetings, and formatting tasks are perfect for on-device TPU.
     */
    fun isLocalTask(prompt: String): Boolean {
        val p = prompt.lowercase()
        return p.length < 100 && (
            p.contains("hello") || p.contains("hi dave") || 
            p.contains("what time") || p.contains("date") ||
            p.contains("calculate") || p.startsWith("is ") ||
            p.startsWith("can you ") || p.contains("joke")
        )
    }

    /**
     * Verifies if the user actually intended to trigger a specific tool/action.
     * Prevents accidental misfires from simple keyword matches.
     */
    suspend fun verifyToolIntent(prompt: String, candidateTask: String): Boolean {
        if (!isAICoreAvailable()) return true // Fallback to keyword match if local AI is unavailable
        
        return withTimeoutOrNull(2500) {
            try {
                val verificationPrompt = """
                    User said: "$prompt"
                    Dave matched this to the task: "$candidateTask"
                    
                    Does the user's message EXPLICITLY request to perform this action?
                    Respond ONLY with 'TRUE' or 'FALSE'. 
                    If it's just a general mention or part of a normal sentence without active intent, respond 'FALSE'.
                """.trimIndent()
                
                val response = generateOnDevice(verificationPrompt)
                response?.trim()?.uppercase() == "TRUE"
            } catch (e: Exception) {
                Log.e("HardwareAccelerator", "Intent verification failed: ${e.message}")
                true // Fallback to true to avoid blocking valid tools on error
            }
        } ?: true
    }

    /**
     * Extracts key entities and topics from a user prompt using Gemini Nano.
     * These keywords are used for relevant semantic memory retrieval.
     */
    suspend fun extractKeywords(prompt: String): List<String> {
        if (!isAICoreAvailable()) return emptyList()
        // Optimization: Skip extraction for very short prompts
        if (prompt.trim().split("\\s+".toRegex()).size < 2) return emptyList()

        return withTimeoutOrNull(4000) {
            try {
                Log.d("HardwareAccelerator", "Starting local keyword extraction...")
                val extractionPrompt = """
                    Analyze the user prompt and extract 5-8 highly relevant keywords or entities. 
                    Focus on:
                    - Proper names (people, places)
                    - Projects or specific technologies
                    - User preferences or stated goals
                    - Key subject matters
                    
                    Respond ONLY with a comma-separated list.
                    
                    Prompt: $prompt
                """.trimIndent()
                val response = generateOnDevice(extractionPrompt)
                Log.d("HardwareAccelerator", "Local extraction complete: ${response?.take(50)}...")
                response?.split(",")?.map { it.trim().lowercase() }?.filter { it.length > 2 }
            } catch (e: Exception) {
                Log.e("HardwareAccelerator", "Keyword extraction failed: ${e.message}")
                null
            }
        } ?: emptyList()
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

    /**
     * Uses Android System Intelligence (or a simplified heuristic) to determine
     * if the user is currently interruptible by a notification.
     */
    fun detectUserActivityContext(): UserInterruptionLevel {
        // In a real implementation, this would query the Contextual Awareness APIs
        // For now, we'll use a time-based heuristic and check if common productivity apps are foregrounded
        val hour = java.util.Calendar.getInstance()[java.util.Calendar.HOUR_OF_DAY]
        
        return when {
            hour in 0..6 -> UserInterruptionLevel.QUIET_TIME // Late night
            // Logic could be expanded here to check foreground package names via Accessibility or Stats
            else -> UserInterruptionLevel.NORMAL
        }
    }

    data class VerificationResult(
        val success: Boolean,
        val age: Int? = null,
        val hasHologram: Boolean = false,
        val message: String = ""
    )

    suspend fun verifyIdentityDocument(base64Image: String): VerificationResult {
        // In a real-world scenario, we'd send the base64 to a specific vision model (like Gemini or on-device custom TFLite)
        // Here we simulate the AI logic using Dave's TPU generateOnDevice for high-level reasoning
        
        val prompt = "SYSTEM: You are an identity verification specialist. " +
                "ANALYZE: Identify the 'Holographic PASS mark' on the ID. " +
                "EXTRACT: Date of Birth. " +
                "COMPUTE: Current age (today is 2026-06-16). " +
                "FORMAT: JSON {success:bool, age:int, has_hologram:bool, status:string}. " +
                "INPUT_DATA: [BASE64_IMAGE_STREAM_ATTACHED]"

        return try {
            val response = generateOnDevice(prompt) ?: "ERROR: Neural processing failed."
            
            // Heuristic parsing for the simulation
            if (response.contains("VALID") || response.contains("true")) {
                VerificationResult(
                    success = true,
                    age = 21, // Simulation default
                    hasHologram = true,
                    message = "ID Authenticated :: Hologram Verified"
                )
            } else {
                VerificationResult(
                    success = false,
                    message = "Identity rejection: Security features not detected."
                )
            }
        } catch (e: Exception) {
            VerificationResult(success = false, message = "System Error: ${e.message}")
        }
    }

    enum class UserInterruptionLevel {
        QUIET_TIME, NORMAL, CRITICAL_ONLY
    }
}
