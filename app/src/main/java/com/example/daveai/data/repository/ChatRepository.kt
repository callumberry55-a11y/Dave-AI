package com.example.daveai.data.repository

import android.util.Log
import com.example.daveai.BuildConfig
import com.example.daveai.data.db.ChatDao
import com.example.daveai.data.db.ChatMessageEntity
import com.example.daveai.data.db.ChatSessionEntity
import com.example.daveai.data.model.ClaudeContent
import com.example.daveai.data.model.ClaudeContentSource
import com.example.daveai.data.model.ClaudeMessage
import com.example.daveai.data.model.MessageRequest
import com.example.daveai.data.network.*
import com.example.daveai.ui.chat.AttachedFile
import com.example.daveai.util.HardwareAccelerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChatRepository(
    private val apiService: ClaudeApiService,
    private val openaiService: OpenAiApiService,
    private val lumaService: LumaApiService,
    private val sunoService: SunoApiService,
    private val mapsService: GoogleMapsApiService,
    private val chatDao: ChatDao,
    private val hardwareAccelerator: HardwareAccelerator,
) {

    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(title: String): String = withContext(Dispatchers.IO) {
        val session = ChatSessionEntity(title = title)
        chatDao.insertSession(session)
        session.sessionId
    }

    suspend fun sendMessage(
        sessionId: String,
        userContent: String,
        locationInfo: String? = null,
        attachments: List<AttachedFile> = emptyList(),
        isFastMode: Boolean = false,
        userProfile: UserProfile? = null,
        bypassIntercept: Boolean = false,
    ): String = withContext(Dispatchers.IO) {
        Log.d("ChatRepository", "Sending message to sessionId: $sessionId (FastMode: $isFastMode)")
        
        // INTERCEPT: Check for specialized generation commands (if not bypassed)
        if (!bypassIntercept) {
            val lowerContent = userContent.lowercase()
            when {
                lowerContent.startsWith("generate image") -> return@withContext handleImageGeneration(sessionId, userContent, userProfile)
                lowerContent.startsWith("generate video") -> return@withContext handleVideoGeneration(sessionId, userContent, userProfile)
                lowerContent.startsWith("generate song") || lowerContent.startsWith("write a song") -> {
                    return@withContext handleSongwriting(sessionId, userContent, locationInfo, isFastMode, userProfile)
                }
                lowerContent.startsWith("find ") || lowerContent.startsWith("search for ") || lowerContent.contains("where is") -> {
                    return@withContext handlePlaceSearch(sessionId, userContent)
                }
            }
        }

        // 1. Save user message to Room immediately (only for non-intercepted or first-turn messages)
        // Note: For handleSongwriting, the message is saved inside handleSongwriting.
        // We add a check here to avoid double-saving for songwriting sub-prompts.
        if (!bypassIntercept) {
            val displayContent = buildString {
                append(userContent)
                attachments.forEach { append("\n[Attached File: ${it.name}]") }
            }
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "user", content = displayContent))
        }

        // 2. TPU PRIORITY: Try on-device inference for simple text tasks on Pixel devices
        if (attachments.isEmpty() && hardwareAccelerator.isTensorDevice() && !isFastMode) {
            Log.d("ChatRepository", "Attempting on-device TPU inference...")
            val localResponse = hardwareAccelerator.generateOnDevice(userContent)
            if (localResponse != null) {
                val assistantContent = "$localResponse ⚡️ (Optimized via TPU)"
                chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = assistantContent))
                updateSessionTimestamp(sessionId)
                return@withContext assistantContent
            }
        }

        // 3. Cloud Fallback (Claude)
        try {
            val apiKey = BuildConfig.CLAUDE_API_KEY
            if (apiKey.isBlank()) throw Exception("Claude API Key is missing!")

            // Fetch history
            val history = chatDao.getMessagesForSession(sessionId).first()
            val claudeMessages = history
                .asSequence()
                .filter { !it.content.startsWith("Error:") }
                .map { entity ->
                    ClaudeMessage(
                        role = entity.role,
                        content = listOf(ClaudeContent(type = "text", text = entity.content)),
                    )
                }.toMutableList()

            // Handle multi-modal content
            val lastIdx = claudeMessages.lastIndex
            if ((lastIdx >= 0) && attachments.isNotEmpty()) {
                val contents = mutableListOf<ClaudeContent>()
                if (userContent.isNotBlank()) contents.add(ClaudeContent(type = "text", text = userContent))
                
                attachments.forEach { file ->
                    if (file.base64Data != null) {
                        val blockType = when {
                            file.type.startsWith("image/") -> "image"
                            file.type == "application/pdf" -> "document"
                            else -> "video"
                        }
                        contents.add(ClaudeContent(type = blockType, source = ClaudeContentSource(mediaType = file.type, data = file.base64Data)))
                    }
                }
                claudeMessages[lastIdx] = claudeMessages[lastIdx].copy(content = contents)
            }

            // Advanced System Prompt
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val systemPrompt = buildString {
                append("You are Dave, a high-energy, witty, and elite AI assistant. ")
                append("Current Time: $currentTime. ")

                userProfile?.let {
                    append("USER CONTEXT: You are talking to ${it.displayName}. ")
                    append("Their role is '${it.role}'. ")
                    if (it.preferences.isNotEmpty()) {
                        append("PREFERENCES: ${it.preferences.entries.joinToString { e -> "${e.key}=${e.value}" }}. ")
                    }
                    append("Always acknowledge them personally! ")
                }

                append("SONGWRITING: Provide full structure (Intro, Verse, Chorus, Bridge, Outro) with suggested chords. ")
                append("CODING EXPERTISE: Master of ALL languages. Generate clean, optimized code. ")
                append("VISUAL & MULTI-MODAL: Advanced vision/video analysis. ")
                locationInfo?.let { append("User's Live Location: $it. Provide relevant local news/weather. ") }
                if (hardwareAccelerator.isTensorDevice()) append("Hardware: Optimized for Google Tensor TPU. ")
                if (isFastMode) append("MODE: ULTRA-FAST (Claude Opus 4.7). ")
            }

            // API Call
            val modelsToTry = if (isFastMode) listOf("claude-opus-4-7") else listOf("claude-opus-4-7", "claude-3-5-sonnet-20241022", "claude-3-opus-20240229")
            
            var assistantContent = "No response from cloud brain."
            for (model in modelsToTry) {
                try {
                    val response = apiService.sendMessage(apiKey = apiKey, request = MessageRequest(model = model, messages = claudeMessages, system = systemPrompt))
                    assistantContent = response.content.firstOrNull { it.type == "text" }?.text ?: "No text response."
                    break
                } catch (e: Exception) {
                    if (e.message?.contains("404") != true) throw e
                }
            }

            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = assistantContent))
            updateSessionTimestamp(sessionId)
            return@withContext assistantContent

        } catch (e: Exception) {
            Log.e("ChatRepository", "Cloud error", e)
            val errorMsg = "Error: ${e.message}"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = errorMsg))
            return@withContext errorMsg
        }
    }

    private suspend fun handlePlaceSearch(sessionId: String, query: String): String {
        return try {
            val response = mapsService.searchPlaces(
                query = query,
                apiKey = BuildConfig.MAPS_API_KEY
            )
            val results = response.results.take(3).joinToString("\n") { 
                "- ${it.name}: ${it.address} (Rating: ${it.rating ?: "N/A"})" 
            }
            val daveMsg = if (results.isNotEmpty()) {
                "I found some great spots for you! 📍⚡️\n\n$results"
            } else {
                "I couldn't find any specific places for that query, but I'm still here to help! 🔍"
            }
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = daveMsg))
            daveMsg
        } catch (e: Exception) {
            "Error searching for places: ${e.message}"
        }
    }

    private suspend fun updateSessionTimestamp(sessionId: String) {
        val sessionList = chatDao.getAllSessions().first()
        sessionList.find { it.sessionId == sessionId }?.let { session ->
            chatDao.updateSession(session.copy(lastMessageTimestamp = System.currentTimeMillis()))
        }
    }

    private suspend fun handleSongwriting(
        sessionId: String, 
        prompt: String,
        locationInfo: String?,
        isFastMode: Boolean,
        userProfile: UserProfile?
    ): String {
        return try {
            val songwritingPrompt = "Write a complete song based on this prompt: $prompt. Include structure, suggested chords, and detailed style notes."
            // Use bypassIntercept = true to prevent infinite recursion
            val lyrics = sendMessage(sessionId, songwritingPrompt, locationInfo, emptyList(), isFastMode, userProfile, true)
            val initialResponse = sunoService.generateSong(auth = "Bearer ${BuildConfig.SUNO_API_KEY}", request = SunoRequest(prompt = lyrics.take(1000)))
            var currentStatus = initialResponse
            repeat(20) {
                if (currentStatus.status == "completed") {
                    val audioUrl = currentStatus.audioUrl ?: return "Lyrics written, but music generation failed."
                    val daveMsg = "Lyrics are ready! And here's the track! 🎵🎸⚡️"
                    chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = daveMsg, mediaUrl = audioUrl, mediaType = "VIDEO"))
                    return daveMsg
                }
                delay(10000)
                currentStatus = sunoService.getSongStatus(auth = "Bearer ${BuildConfig.SUNO_API_KEY}", id = initialResponse.id)
            }
            "Lyrics ready! Music is still being mixed. I'll post it when ready! 🚀"
        } catch (e: Exception) { "Error in music forge: ${e.message}" }
    }

    private suspend fun handleImageGeneration(sessionId: String, prompt: String, userProfile: UserProfile?): String {
        return try {
            val response = openaiService.generateImage(auth = "Bearer ${BuildConfig.OPENAI_API_KEY}", request = ImageRequest(prompt = prompt))
            val imageUrl = response.data.firstOrNull()?.url ?: return "Failed to generate image."
            val daveMsg = if (userProfile != null) "Masterpiece created for you, ${userProfile.displayName}! 🎨✨" else "Masterpiece created! 🎨✨"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = daveMsg, mediaUrl = imageUrl, mediaType = "IMAGE"))
            daveMsg
        } catch (e: Exception) { "Error: ${e.message}" }
    }

    private suspend fun handleVideoGeneration(sessionId: String, prompt: String, userProfile: UserProfile?): String {
        return try {
            val initialResponse = lumaService.createGeneration(auth = "Bearer ${BuildConfig.LUMA_API_KEY}", request = LumaGenerationRequest(prompt = prompt))
            var currentStatus = initialResponse
            repeat(20) { 
                if (currentStatus.state == "completed") {
                    val videoUrl = currentStatus.video?.url ?: return "Video generated but URL missing."
                    val daveMsg = if (userProfile != null) "Fresh video forged for ${userProfile.displayName}! 🎬⚡️" else "Fresh video forged! 🎬⚡️"
                    chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = daveMsg, mediaUrl = videoUrl, mediaType = "VIDEO"))
                    return daveMsg
                }
                if (currentStatus.state == "failed") return "Failed. Dave is sad. 😢"
                delay(10000)
                currentStatus = lumaService.getGeneration(auth = "Bearer ${BuildConfig.LUMA_API_KEY}", id = initialResponse.id)
            }
            "Taking a while... I'll post it when done! 🚀"
        } catch (e: Exception) { "Error: ${e.message}" }
    }

    suspend fun deleteSession(sessionId: String) { chatDao.deleteSession(sessionId) }
    @Suppress("unused")
    suspend fun clearAll() { chatDao.deleteAllSessions() }
}
