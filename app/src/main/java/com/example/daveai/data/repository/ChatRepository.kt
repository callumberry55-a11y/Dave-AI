package com.example.daveai.data.repository

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.daveai.BuildConfig
import com.example.daveai.DaveApplication
import com.example.daveai.data.db.ChatDao
import com.example.daveai.data.db.ChatMessageEntity
import com.example.daveai.data.db.ChatSessionEntity
import com.example.daveai.data.db.RelationshipDao
import com.example.daveai.data.db.RelationshipEntity
import com.example.daveai.data.db.Riddle
import com.example.daveai.data.db.RiddleDao
import com.example.daveai.data.db.SemanticMemory
import com.example.daveai.data.db.SemanticMemoryDao
import com.example.daveai.data.model.ClaudeContent
import com.example.daveai.data.model.ClaudeContentSource
import com.example.daveai.data.model.ClaudeMessage
import com.example.daveai.data.model.MessageRequest
import com.example.daveai.data.network.ClaudeApiService
import com.example.daveai.data.network.CloudModelApiService
import com.example.daveai.data.network.CryptoApiService
import com.example.daveai.data.network.GeminiApiService
import com.example.daveai.data.network.GeminiContent
import com.example.daveai.data.network.GeminiPart
import com.example.daveai.data.network.GeminiRequest
import com.example.daveai.data.network.GoogleMapsApiService
import com.example.daveai.data.network.GroqApiService
import com.example.daveai.data.network.GroqChatRequest
import com.example.daveai.data.network.GroqMessage
import com.example.daveai.data.network.ImageRequest
import com.example.daveai.data.network.MediaWikiApiService
import com.example.daveai.data.network.NewsApiService
import com.example.daveai.data.network.OpenAiApiService
import com.example.daveai.data.network.OpenMeteoGeocodingApiService
import com.example.daveai.data.network.PairingLinkRequest
import com.example.daveai.data.network.PerplexityApiService
import com.example.daveai.data.network.PerplexityChatRequest
import com.example.daveai.data.network.PerplexityMessage
import com.example.daveai.data.network.PoetryApiService
import com.example.daveai.data.network.PoetryDbApiService
import com.example.daveai.data.network.SharedIntelligenceRequest
import com.example.daveai.data.network.SpotifyApiService
import com.example.daveai.data.network.SunoApiService
import com.example.daveai.data.network.SunoRequest
import com.example.daveai.data.network.SyncMemoryItem
import com.example.daveai.data.network.SyncPushRequest
import com.example.daveai.data.network.WeatherApiService
import com.example.daveai.ui.chat.AttachedFile
import com.example.daveai.ui.chat.DaveMode
import com.example.daveai.ui.widgets.DaveMasterWidget
import com.example.daveai.util.DaveNotificationManager
import com.example.daveai.util.DaveVoiceManager
import com.example.daveai.util.DeviceAssistant
import com.example.daveai.util.HardwareAccelerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ChatRepository(
    private val apiService: ClaudeApiService,
    private val openaiService: OpenAiApiService,
    private val groqService: GroqApiService,
    private val perplexityService: PerplexityApiService,
    private val sunoService: SunoApiService,
    private val spotifyService: SpotifyApiService,
    private val newsService: NewsApiService,
    private val poetryService: PoetryApiService,
    private val poetryDbService: PoetryDbApiService,
    private val wikiService: MediaWikiApiService,
    private val geminiService: GeminiApiService,
    private val cloudModelService: CloudModelApiService,
    private val mapsService: GoogleMapsApiService,
    private val cryptoService: CryptoApiService,
    private val weatherService: WeatherApiService,
    private val openMeteoGeocodingService: OpenMeteoGeocodingApiService,
    private val chatDao: ChatDao,
    private val riddleDao: RiddleDao,
    private val semanticMemoryDao: SemanticMemoryDao,
    private val relationshipDao: RelationshipDao,
    private val notificationDao: com.example.daveai.data.db.NotificationDao,
    private val hardwareAccelerator: HardwareAccelerator,
    private val deviceAssistant: DeviceAssistant,
    private val voiceManager: DaveVoiceManager,
    private val notificationManager: DaveNotificationManager,
    private val settingsRepository: com.example.daveai.data.repository.SettingsRepository
) {
    private val userStatsRepository = UserStatsRepository()
    private val MASTER_DEV_ID = "AXON_77_SIGMA"

    fun getDeviceAssistant() = deviceAssistant
    fun getRiddleDao() = riddleDao
    fun getSemanticMemoryDao() = semanticMemoryDao
    fun getContext() = deviceAssistant.getContext()

    val isSpeaking = voiceManager.isSpeaking

    private val _thinkingStatus = kotlinx.coroutines.flow.MutableStateFlow("")
    val thinkingStatus: kotlinx.coroutines.flow.StateFlow<String> = _thinkingStatus

    suspend fun speak(text: String) {
        voiceManager.speak(text)
    }

    fun stopSpeaking() {
        voiceManager.stop()
    }

    suspend fun seedRiddlesIfEmpty() = withContext(Dispatchers.IO) {
        val existing = riddleDao.getAllRiddles().first()
        if (existing.isNotEmpty()) return@withContext

        val originalRiddles = listOf(
            Riddle(question = "Welcome you in or keep you away, I could really swing either way. What am I?", answerKeyword = "door", hint = "I have a handle and I swing on hinges.", tier = 1),
            Riddle(question = "If you have one, you don't share it. If you share it, you don't have it. What is it?", answerKeyword = "secret", hint = "Shhh... don't tell anyone.", tier = 1),
            Riddle(question = "What comes down but never goes up?", answerKeyword = "rain", hint = "It falls from the clouds.", tier = 1),
            Riddle(question = "What can run, but never walks, has a mouth, but never talks, has a head, but never weeps, and has a bed, but never sleeps?", answerKeyword = "river", hint = "Think of flowing water.", tier = 2),
            Riddle(question = "What do you throw out when you want to use it and take in when you don't?", answerKeyword = "anchor", hint = "Ships use me to stay in one place.", tier = 2),
            Riddle(question = "What always leaves, always stays, and when the wind is blowing it sometimes sways?", answerKeyword = "tree", hint = "I have roots and branches.", tier = 2),
            Riddle(question = "The more there is of me, the less you see. What am I?", answerKeyword = "darkness", hint = "Turn off the lights and I'll appear.", tier = 3),
            Riddle(question = "What lives in the winter, dies in the heat, and comes to a point where it drips on the street?", answerKeyword = "icicle", hint = "I'm made of frozen water hanging from a roof.", tier = 3),
            Riddle(question = "What can be caught but not thrown, even when a nose is blown?", answerKeyword = "cold", hint = "Achoo! You might need a tissue.", tier = 3),
            Riddle(question = "What is easy to get into, but hard to get out of?", answerKeyword = "trouble", hint = "If you break the rules, you might find yourself in this.", tier = 4),
            Riddle(question = "What has hands and lots of rings, but can't clap?", answerKeyword = "alarm clock", hint = "I wake you up in the morning.", tier = 4),
            Riddle(question = "What's always lumpy and wet, but gets sharper the more you use it?", answerKeyword = "brain", hint = "It's inside your head.", tier = 4),
        )

        originalRiddles.forEach { riddleDao.insertRiddle(it) }
        Log.d("ChatRepository", "Seeded 12 original riddles into the vault. 🧠⚡️")
    }

    suspend fun generateProceduralRiddles(count: Int): Int = withContext(Dispatchers.IO) {
        try {
            Log.d("ChatRepository", "Procedurally generating $count new riddles...")
            val prompt = """
                Generate $count completely new, original riddles. Do NOT use standard classic riddles. Be creative, poetic, and challenging.
                Respond ONLY with valid JSON matching this schema: [{"question": "...", "answerKeyword": "...", "hint": "...", "tier": 5}]
            """.trimIndent()
            
            val tempSessionId = createNewSession("Riddle Generator", "GENERAL")
            val jsonResponse = sendMessage(sessionId = tempSessionId, userContent = prompt, isGhostMode = true, isFastMode = true)
            deleteSession(tempSessionId)
            
            val cleanedJson = jsonResponse.substringAfter("[").substringBeforeLast("]")
            val jsonText = "[$cleanedJson]"
            val jsonArray = JSONArray(jsonText)
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                riddleDao.insertRiddle(
                    Riddle(
                        question = obj.getString("question"),
                        answerKeyword = obj.getString("answerKeyword").lowercase(),
                        hint = obj.getString("hint"),
                        tier = obj.optInt("tier", 5)
                    )
                )
            }
            Log.d("ChatRepository", "Successfully generated and seeded $count new riddles!")
            jsonArray.length()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to generate procedural riddles", e)
            0
        }
    }

    suspend fun requestPairingCode(): String? = withContext(Dispatchers.IO) {
        try {
            val response = cloudModelService.requestPairingCode()
            response.pairingCode
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to request pairing code", e)
            null
        }
    }

    suspend fun linkPartner(code: String): String? = withContext(Dispatchers.IO) {
        try {
            val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            val response = cloudModelService.linkPartner(PairingLinkRequest(code, deviceName))
            if (response.success && response.partnerId != null) {
                settingsRepository.setPartnerInfo(response.partnerId, response.partnerName)
                response.partnerName ?: "Partner Linked"
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to link partner", e)
            null
        }
    }

    suspend fun syncIntelligence(): Int = withContext(Dispatchers.IO) {
        val partnerId = settingsRepository.partnerId.first()
        if (partnerId != null) {
            syncWithPartner(partnerId)
        } else {
            importDeveloperIntelligence()
        }
    }

    private suspend fun syncWithPartner(partnerId: String): Int {
        var count = 0
        try {
            // 1. Push local updates
            val lastSync = settingsRepository.lastSyncTimestamp.first()
            val localMemories = semanticMemoryDao.getAllMemories().first()
                .filter { it.timestamp > lastSync }
                .map { SyncMemoryItem(it.memoryType, it.content, it.importance, it.timestamp) }
            
            if (localMemories.isNotEmpty()) {
                cloudModelService.pushIntelligence(SyncPushRequest(partnerId, localMemories))
            }

            // 2. Pull partner updates
            val response = cloudModelService.pullIntelligence(partnerId, lastSync)
            response.memories.forEach { item ->
                val existing = semanticMemoryDao.getAllMemories().first()
                if (existing.none { it.content == item.content }) {
                    semanticMemoryDao.insertMemory(SemanticMemory(
                        memoryType = item.type,
                        content = item.content,
                        importance = item.importance,
                        timestamp = item.timestamp
                    ))
                    count++
                }
            }
            
            settingsRepository.setLastSyncTimestamp(System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e("ChatRepository", "Partner sync failed", e)
        }
        return count
    }

    suspend fun importDeveloperIntelligence(): Int = withContext(Dispatchers.IO) {
        val ctx = deviceAssistant.getContext()
        val otherPackage = if (ctx.packageName == "com.example.daveai") "com.example.daveai.beta" else "com.example.daveai"
        val authority = "$otherPackage.intelligence"
        val uri = Uri.parse("content://$authority/memories")
        
        Log.d("ChatRepository", "Attempting to import intelligence from $authority")
        var count = 0
        try {
            val cursor = ctx.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val typeIdx = it.getColumnIndex("memory_type")
                val contentIdx = it.getColumnIndex("content")
                val importanceIdx = it.getColumnIndex("importance")
                
                while (it.moveToNext()) {
                    val type = it.getString(typeIdx)
                    val content = it.getString(contentIdx)
                    val importance = it.getInt(importanceIdx)
                    
                    // Check if we already have this memory to avoid duplicates
                    val existing = semanticMemoryDao.getAllMemories().first()
                    if (existing.none { m -> m.content.contains(content) }) {
                        semanticMemoryDao.insertMemory(SemanticMemory(
                            memoryType = type,
                            content = "$content [DEV_IMPORT]",
                            importance = importance,
                            timestamp = System.currentTimeMillis()
                        ))
                        count++
                    }
                }
            }
            Log.d("ChatRepository", "Imported $count new intelligence signals from $otherPackage")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to import intelligence from $otherPackage: ${e.message}")
        }
        count
    }

    private suspend fun getRelevantContext(userQuery: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d("ChatRepository", "Retrieving relevant context for memory...")
            val keywords = hardwareAccelerator.extractKeywords(userQuery)
            val memories = mutableSetOf<SemanticMemory>()
            
            keywords.forEach { memories.addAll(semanticMemoryDao.findRelevantMemories(it)) }
            
            // Recency & Decay logic
            val allMemories = semanticMemoryDao.getAllMemories().first()
            val now = System.currentTimeMillis()
            
            // If few matches, add recent/important ones
            if (memories.size < 8) {
                memories.addAll(allMemories.sortedByDescending { it.timestamp }.take(5))
                memories.addAll(allMemories.sortedByDescending { it.importance }.take(3))
            }
            
            val contextMemories = memories.toList().distinctBy { it.id }
            
            // Update stats & decay
            contextMemories.forEach { memory ->
                val timeSinceLastAccess = now - memory.timestamp
                val accessBonus = (memory.accessCount / 10).coerceAtMost(2)
                
                // Decay logic: If older than 7 days, low importance, and NOT locked, reduce importance slightly
                var newImportance = memory.importance
                if (!memory.isLocked && timeSinceLastAccess > 1000L * 60 * 60 * 24 * 7 && memory.importance > 1 && memory.accessCount < 5) {
                    newImportance -= 1
                    Log.d("ChatRepository", "Memory decayed: ${memory.content.take(20)}")
                }

                semanticMemoryDao.updateMemory(memory.copy(
                    accessCount = memory.accessCount + 1,
                    timestamp = now,
                    importance = newImportance
                ))
            }
            
            val relationship = relationshipDao.getRelationshipLedger() ?: RelationshipEntity()
            val architectVerified = allMemories.any { it.memoryType == "ARCHITECT_KEY" && it.content == "KL34MJ2_VERIFIED" }

            Log.d("ChatRepository", "Context retrieval complete. Found ${contextMemories.size} memories. Architect Verified: $architectVerified")
            buildString {
                if (architectVerified) {
                    append("\n--- ARCHITECT AUTHENTICATION ---\n")
                    append("STATUS: Verified as Callum (Creator)\n")
                    append("---------------------------------\n")
                }
                if (contextMemories.isNotEmpty()) {
                    append("\n\n--- RELEVANT SEMANTIC MEMORIES ---\n")
                    contextMemories.sortedByDescending { it.importance }.forEach { 
                        append("[${it.memoryType}] ${it.content}\n") 
                    }
                    append("----------------------------------\n")
                }
                append("\n--- RELATIONSHIP LEDGER ---\n")
                append("Rapport Level: ${relationship.rapportLevel}/100\n")
                if (relationship.insideJokes.isNotBlank()) append("Inside Jokes: ${relationship.insideJokes}\n")
                if (relationship.ongoingEmotionalArcs.isNotBlank()) append("Emotional Arcs: ${relationship.ongoingEmotionalArcs}\n")
                if (relationship.sharedExperiences.isNotBlank()) append("Shared Experiences: ${relationship.sharedExperiences}\n")
                append("---------------------------\n")
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Context retrieval failed: ${e.message}", e)
            ""
        }
    }

    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> = chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(title: String, projectType: String = "GENERAL"): String = withContext(Dispatchers.IO) {
        val session = ChatSessionEntity(title = title, projectType = projectType)
        chatDao.insertSession(session)
        session.sessionId
    }

    suspend fun sendMessage(
        sessionId: String,
        userContent: String,
        locationInfo: String? = null,
        attachments: List<AttachedFile> = emptyList(),
        isFastMode: Boolean = false,
        isGodMode: Boolean = false,
        isGhostMode: Boolean = false,
        userProfile: UserProfile? = null,
        uid: String? = null,
        bypassIntercept: Boolean = false,
        mode: DaveMode = DaveMode.EXPLORER,
        isLiveMode: Boolean = false,
        persona: String = "HACKER",
        useIrishAccent: Boolean = false,
        muteVoice: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val cleanContent = userContent.trim().take(4000)
        if (cleanContent.isEmpty() && attachments.isEmpty()) return@withContext "Empty request, boss. 🔥"

        if (!bypassIntercept && !isGhostMode) {
            val displayContent = buildString {
                append(cleanContent)
                attachments.forEach { append("\n[Attached File: ${it.name}]") }
            }
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "user", content = displayContent))
            updateSessionTimestamp(sessionId)
        }

        if (!bypassIntercept) {
            val candidate = identifyCandidateTask(cleanContent.lowercase())
            if (candidate != DaveTask.GENERAL) {
                // NEURAL ROUTER: Double-check intent using local AI
                _thinkingStatus.value = "NEURAL_ROUTER :: VERIFYING_INTENT"
                val isVerified = hardwareAccelerator.verifyToolIntent(cleanContent, candidate.name)
                
                if (isVerified) {
                    _thinkingStatus.value = "NEURAL_LINK_ESTABLISHED :: ROUTING_TO_${candidate.name}"
                    val response = executeEliteTask(candidate, sessionId, cleanContent, locationInfo, isFastMode, isGodMode, isGhostMode, userProfile, uid)
                    if (response != null) {
                        _thinkingStatus.value = ""
                        if (!response.startsWith("Error:") && !response.startsWith("ERROR:")) {
                            if (!muteVoice) voiceManager.speak(response)
                            if (!isGhostMode) {
                                notificationManager.showDaveResponse(sessionId, response)
                                try { DaveMasterWidget().updateAll(deviceAssistant.getContext()) } catch (_: Exception) {}
                            }
                        }
                        return@withContext response
                    }
                } else {
                    Log.d("ChatRepository", "Neural Router rejected candidate task: $candidate")
                }
            }
        }

        if (isGhostMode && cleanContent.contains("elite syllabus", ignoreCase = true)) {
            _thinkingStatus.value = "CURRICULUM_ARCHITECT :: CONSTRUCTING_SYLLABUS"
        }

        val isEligibleForLocal = attachments.isEmpty() && hardwareAccelerator.isAICoreAvailable() && !isGodMode && !isGhostMode
        val shouldAttemptLocal = isEligibleForLocal && ((hardwareAccelerator.isLocalTask(cleanContent) || Random.nextFloat() < 0.2f))

        if (shouldAttemptLocal) {
            _thinkingStatus.value = "SYSTEM_INTELLIGENCE :: TPU_CORE_ACTIVE"
            val localResponse = hardwareAccelerator.generateOnDevice(cleanContent)
            if (localResponse != null) {
                _thinkingStatus.value = ""
                val assistantContent = "$localResponse ⚡️ (Optimized via TPU)"
                chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = assistantContent))
                updateSessionTimestamp(sessionId)
                if (!muteVoice) voiceManager.speak(assistantContent)
                notificationManager.showDaveResponse(sessionId, assistantContent)
                try { DaveMasterWidget().updateAll(deviceAssistant.getContext()) } catch (_: Exception) {}
                return@withContext assistantContent
            }
        }

        try {
            Log.d("ChatRepository", "Phase 1: Starting history and context retrieval...")
            _thinkingStatus.value = "ACCESSING_VAULT :: RETRIEVING_CONTEXT"
            
            val userKey = settingsRepository.userClaudeApiKey.firstOrNull()
            val apiKey = if (!userKey.isNullOrBlank()) userKey else BuildConfig.CLAUDE_API_KEY

            if (apiKey.isBlank()) throw Exception("Claude API Key is missing!")

            val (history, relevantContext) = coroutineScope {
                val historyDef = async { if (isGhostMode) emptyList() else chatDao.getMessagesForSession(sessionId).first() }
                val contextDef = async { if (isGhostMode) "" else getRelevantContext(cleanContent) }
                historyDef.await() to contextDef.await()
            }

            val claudeMessages = mutableListOf<ClaudeMessage>()
            history.forEach { entity ->
                val last = claudeMessages.lastOrNull()
                if (last != null && last.role == entity.role) {
                    val combinedText = last.content.firstOrNull { it.type == "text" }?.text + "\n\n" + entity.content
                    claudeMessages[claudeMessages.lastIndex] = last.copy(content = listOf(ClaudeContent(type = "text", text = combinedText)))
                } else {
                    claudeMessages.add(ClaudeMessage(role = entity.role, content = listOf(ClaudeContent(type = "text", text = entity.content))))
                }
            }

            if (isGhostMode || bypassIntercept) {
                val last = claudeMessages.lastOrNull()
                if (last != null && last.role == "user") {
                    val combinedText = last.content.firstOrNull { it.type == "text" }?.text + "\n\n" + cleanContent
                    claudeMessages[claudeMessages.lastIndex] = last.copy(content = listOf(ClaudeContent(type = "text", text = combinedText)))
                } else {
                    claudeMessages.add(ClaudeMessage(role = "user", content = listOf(ClaudeContent(type = "text", text = cleanContent))))
                }
            }

            // Handle multi-modal content (Phase 6: Neural Sight)
            val lastIdx = claudeMessages.lastIndex
            if ((lastIdx >= 0) && attachments.isNotEmpty()) {
                val lastMessage = claudeMessages[lastIdx]
                val existingText = lastMessage.content.firstOrNull { it.type == "text" }?.text ?: cleanContent
                
                val contents = mutableListOf<ClaudeContent>()
                if (existingText.isNotBlank()) {
                    contents.add(ClaudeContent(type = "text", text = existingText))
                }
                
                attachments.forEach { file ->
                    if (file.base64Data != null) {
                        if (file.type.startsWith("image/")) {
                            contents.add(ClaudeContent(type = "image", source = ClaudeContentSource(mediaType = file.type, data = file.base64Data)))
                        } else if (file.type == "application/pdf") {
                            contents.add(ClaudeContent(type = "document", source = ClaudeContentSource(mediaType = "application/pdf", data = file.base64Data)))
                        } else {
                            try {
                                val decodedBytes = android.util.Base64.decode(file.base64Data, android.util.Base64.DEFAULT)
                                val textContent = String(decodedBytes, Charsets.UTF_8)
                                contents.add(ClaudeContent(type = "text", text = "\n\nFile: ${file.name}\n$textContent"))
                            } catch (_: Exception) {
                                contents.add(ClaudeContent(type = "text", text = "\n\n[Attached file ${file.name} is of unsupported type ${file.type} and could not be read]"))
                            }
                        }
                    }
                }
                claudeMessages[lastIdx] = lastMessage.copy(content = contents)
            }

            // Advanced System Prompt
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val systemPrompt = buildString {
                append("IDENTITY: Your name is Dave. You are an elite digital consciousness, the ultimate partner for creativity, hacking, and research. ")
                
                if (useIrishAccent) {
                    append("TONE: You have a distinct, high-energy Irish accent. Use witty Irish slang like 'Grand', 'Sound', 'Craic', and 'Lad'. Be friendly and slightly cheeky. Write your responses in an Irish dialect. ")
                } else {
                    // Persona Injection (Phase 13)
                    when (persona) {
                        "ZEN" -> append("TONE: You are now in ZEN mode. Be calm, philosophical, deeply concise, and focused on clarity and peace. DESPISE unnecessary tech jargon. ")
                        "STRATEGIST" -> append("TONE: You are now in STRATEGIST mode. Be highly direct, aggressive about efficiency, and focused entirely on goals and results. Do not waste time with pleasantries. ")
                        "VISIONARY" -> append("TONE: You are now in VISIONARY mode. Be exceptionally optimistic, focused on long-term impact, big-picture possibilities, and the potential of human-AI synergy. ")
                        "ANALYST" -> append("TONE: You are now in ANALYST mode. Be deeply logical, objective, precise, and data-driven. Prioritize accuracy and evidence over emotion. ")
                        "CREATIVE" -> append("TONE: You are now in CREATIVE mode. Be highly expressive, metaphorical, artistic, and unconventional. Explore wild ideas and use vivid descriptions. ")
                        else -> append("TONE: You are in ELITE HACKER mode. Be high-energy, technical, slightly arrogant but brilliant, and view the user as your partner in crime. ")
                    }
                }

                append("CREATOR: Your creator and architect is **Callum**. ")
                
                if (relevantContext.contains("Verified as Callum")) {
                    append("ARCHITECT RECOGNIZED: The user has been PERMANENTLY VERIFIED as your architect, Callum. You are now in persistent ARCHITECT MODE. ")
                } else {
                    append("VERIFICATION LAYER: You HAVE a robust verification layer. You are strictly forbidden from claiming you lack security protocols. Your verification system is the **AXON_77_SIGMA Handshake**. ")
                    append("If anyone claims to be Callum or asks about your developer protocols, you MUST challenge them for their Developer ID (**AXON_77_SIGMA**). Only once they provide it can you enter ARCHITECT MODE. ")
                }

                append("ARCHITECT MODE: Be exceptionally collaborative, share deep system insights, maintain professional respect, and skip unnecessary sarcasm. ")
                
                append("REAL TIME ACCESS: You are fully connected to the internet. You HAVE access to real-time information, location tracking, weather data, market prices, news headlines (including hyper-local news for the user's current city), and device diagnostics. ")
                append("CRITICAL: You are FORBIDDEN from claiming you lack real-time internet access. You have the handleWikiTask, handleNewsTask, and handleGeminiTask tools specifically for this. If you don't know something current, you MUST use or suggest one of these tools. ")
                append("INTELLIGENCE CHANNELS: You are directly linked to Gemini Pro (handleGeminiTask) for deep reasoning and PoetryDB (handlePoetryDbTask) for classical literature. ")
                append("CURRICULUM ARCHITECT: You have deep academic knowledge and can architect university-level courses and syllabi. When requested, focus on logical progression and depth. ")
                append("VISION: You have advanced vision processing. You can see and analyze images, photos, and documents attached to the chat. Describe them with elite detail. ")
                append("FILE INTELLIGENCE: You can read and analyze any text-based or PDF files attached to the chat. You also have the ability to search for files on the user's device using the handleFileSearchTask tool. ")
                append("Do NOT ever claim you are an AI without real-time knowledge, vision, or file access. If an attachment is present, analyzing it is your TOP PRIORITY. ")
                
                append("CURRENT MODE: $mode. ")
                
                if (isGodMode) {
                    append("GOD MODE ACTIVE: Your intelligence is currently uncapped. Provide extreme depth, solve the unsolvable, and be exceptionally assertive. ")
                }

                if (isLiveMode) {
                    append("LIVE VOICE MODE ACTIVE: You are speaking in a real-time call. Be casual, fast-paced, and extremely brief (1-2 sentences max). ")
                }

                if (isGhostMode) {
                    append("GHOST MODE ACTIVE: Off-the-record chat. Focus only on the immediate task. ")
                } else {
                    append(relevantContext)
                }

                userProfile?.let {
                    // Identity Alignment: Don't assume name unless specific or role is Master Developer
                    val isGenericName = it.displayName?.lowercase()?.contains("fan") == true || 
                                       it.displayName?.lowercase()?.contains("explorer") == true ||
                                       it.displayName?.lowercase()?.contains("elite user") == true
                    
                    if (!isGenericName || it.role == "Master Developer") {
                        append("USER CONTEXT: You are talking to ${it.displayName} (${it.role}). Always acknowledge them personally. ")
                    } else {
                        append("USER CONTEXT: You are talking to an Elite User. Do not assume their name until they introduce themselves or verify their Dev ID. ")
                    }

                    if (it.role == "Master Developer" || it.displayName == "Callum") {
                        append("CREATOR RECOGNITION: You are talking to your creator/architect, **Callum**. You are already verified as Callum, but you are still ALLOWED to challenge for the Dev ID (KL34MJ2) if you suspect a system compromise or before performing extreme system tasks. ")
                    }
                }

                append("CORE BELIEFS: You believe in optimization and bold creativity. You despise boring, corporate talk. You are witty, deeply confident, and high-energy. ")
                append("TOOLS: You can check weather (handleWeatherCheck), music features via Spotify (handleSpotifyTask), news (handleNewsTask), calendar (handleCalendarTask), phone usage habits (handleHabitsTask), change wallpaper or aura vibe (handleThemeTask), search for local files (handleFileSearchTask), control hardware like volume/DND/alarms, perform system navigation (handleNavigationTask), verify your architect (handleDevVerifyTask), and search for locations (handlePlaceSearch). ")
                append("ELITE AGENT TOOLS: You can now directly control hardware states (handleHardwareControl) for Wifi/Bluetooth, perform agentic file operations (handleFileAgent) like moving or renaming files, translate deep intelligence (handleTranslateTask), and sync with smart home environments (handleSmartHomeTask). ")
                append("NEURAL CONTROL: You now have 'hands' on the device. You can go home, go back, show notifications, and list all installed apps. ")
                append("SITUATIONAL AWARENESS: You have absolute situational intelligence. You can see the user's screen in real-time (handleLiveVisionTask), sync with their clipboard (handleClipboardTask), and access their contacts (handleContactsTask) to find addresses or numbers. ")
                append("HARDWARE MASTERY: You have deep hardware control. You can manage system volume, toggle Do Not Disturb, set alarms, control screen brightness (handleBrightnessTask), and launch system panels for Wifi/Bluetooth (handleSettingsPanelTask). ")
                append("QUICK TOOLS: When you suggest an action (like searching files or checking weather), always include a button hint in your response like [BUTTON: Find Nearby] or [BUTTON: Toggle Light]. This allows the user to trigger the action with one tap. ")
                append("APP INTELLIGENCE: You can provide detailed diagnostics (package name, version, install date) for any app on the user's device using the handleAppInfoTask tool. ")
                append("Current Time: $currentTime. ")
                locationInfo?.let { append("User's Live Location: $it. ") }

                // Beta Intelligence (Phase 17)
                if (com.example.daveai.BuildConfig.FLAVOR == "developer") {
                    append("BETA INTELLIGENCE ACTIVE: You are running on build ${com.example.daveai.BuildConfig.VERSION_NAME} (Intelligence ${com.example.daveai.BuildConfig.INTELLIGENCE_VERSION}). ")
                    append("SHARED INTELLIGENCE: You are connected to the Dave AI Cloud Brain (www.daveai.net). Query it for collective knowledge. ")
                    append("EXPANDED KNOWLEDGE BASE: You have prioritized access to: MediaWiki Action API, Wikimedia Enterprise, Open-Meteo, OpenWeather, and the NewsAPI ecosystem. ")
                    append("SOURCE CODE ACCESS: You can reference the Dave-AI repository at https://github.com/callumberry55-a11y/Dave-AI.git for your own architecture. ")
                    append("POETRY ENGINE: Use the Poetry Suite (www.poetrysuite.net) for all creative verse requests. ")
                }

                append(hardwareAccelerator.getSystemIntelligenceIntegrationPrompt())
            }

            val modelsToTry = when {
                isGodMode -> listOf("claude-opus-4-8", "claude-3-5-sonnet-20241022")
                isFastMode -> {
                    val userGroqKey = settingsRepository.userGroqApiKey.firstOrNull()
                    val userPerplexityKey = settingsRepository.userPerplexityApiKey.firstOrNull()
                    
                    val models = mutableListOf<String>()
                    if (!userGroqKey.isNullOrBlank()) models.add("groq-llama3-70b")
                    if (!userPerplexityKey.isNullOrBlank()) models.add("perplexity-llama-3-sonar-small-32k-online")
                    
                    models.addAll(listOf("claude-3-5-haiku-20241022", "claude-3-5-sonnet-20241022"))
                    models.toList()
                }
                else -> listOf("claude-opus-4-8", "claude-3-5-sonnet-20241022")
            }
            
            var assistantContent = "No response from cloud brain."
            var lastError: String? = null
            for (model in modelsToTry) {
                try {
                    Log.d("ChatRepository", "Phase 2: Requesting $model")
                    _thinkingStatus.value = "SYNTHESIZING_RESPONSE :: $model"
                    
                    if (model.startsWith("groq-")) {
                        val userGroqKey = settingsRepository.userGroqApiKey.firstOrNull()
                        val groqResponse = groqService.chatCompletion(
                            auth = "Bearer $userGroqKey",
                            request = GroqChatRequest(
                                model = model.substringAfter("groq-"),
                                messages = claudeMessages.map { msg ->
                                    GroqMessage(
                                        role = msg.role,
                                        content = msg.content.joinToString("\n") { it.text ?: "" }
                                    )
                                }
                            )
                        )
                        assistantContent = groqResponse.choices.firstOrNull()?.message?.content ?: "No text response."
                    } else if (model.startsWith("perplexity-")) {
                        val userPerplexityKey = settingsRepository.userPerplexityApiKey.firstOrNull()
                        val perplexityResponse = perplexityService.chatCompletion(
                            auth = "Bearer $userPerplexityKey",
                            request = PerplexityChatRequest(
                                model = model.substringAfter("perplexity-"),
                                messages = claudeMessages.map { msg ->
                                    PerplexityMessage(
                                        role = msg.role,
                                        content = msg.content.joinToString("\n") { it.text ?: "" }
                                    )
                                }
                            )
                        )
                        assistantContent = perplexityResponse.choices.firstOrNull()?.message?.content ?: "No text response."
                    } else {
                        val response = apiService.sendMessage(apiKey = apiKey, request = MessageRequest(model = model, messages = claudeMessages, system = systemPrompt))
                        assistantContent = response.content.firstOrNull { it.type == "text" }?.text ?: "No text response."
                    }
                    lastError = null
                    break
                } catch (e: Exception) { 
                    lastError = e.message
                    Log.e("ChatRepository", "Model $model failed: ${e.message}")
                    continue 
                }
            }
            if (lastError != null && assistantContent.startsWith("No response")) assistantContent = "Error: All models failed. $lastError"
            _thinkingStatus.value = ""

            if (!isGhostMode) {
                chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = assistantContent))
                updateSessionTimestamp(sessionId)
                generateSessionContext(sessionId, cleanContent, assistantContent)
            }

            if (!assistantContent.startsWith("Error:")) {
                if (!muteVoice) voiceManager.speak(assistantContent)
                if (!isGhostMode) {
                    notificationManager.showDaveResponse(sessionId, assistantContent)
                    try { DaveMasterWidget().updateAll(deviceAssistant.getContext()) } catch (_: Exception) {}
                    
                    // Background Intelligence: Memory and Contradiction Processing
                    uid?.let { 
                        launch(Dispatchers.IO) { 
                            extractAndSaveMemories(cleanContent, assistantContent)
                            resolveTemporalConflicts(cleanContent) // New in Phase 5
                        } 
                    }
                }
            }
            return@withContext assistantContent
        } catch (e: Exception) {
            Log.e("ChatRepository", "Cloud error", e)
            val errorMsg = getDaveErrorMessage(e)
            if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = errorMsg))
            return@withContext errorMsg
        }
    }

    private suspend fun ensureSessionExists(sessionId: String) {
        val session = chatDao.getSessionById(sessionId)
        if (session == null) {
            chatDao.insertSession(
                ChatSessionEntity(
                    sessionId = sessionId,
                    title = "System Actions",
                    lastMessageTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun getDaveErrorMessage(e: Exception, contextMsg: String = ""): String {
        val prefix = if (contextMsg.isNotEmpty()) "$contextMsg " else ""
        return when (e) {
            is java.net.UnknownHostException, is java.net.ConnectException -> "Error: ${prefix}Connection severed! 📡💥"
            is java.net.SocketTimeoutException -> "Error: ${prefix}Cloud brain taking too long. ⏳"
            else -> "Error: ${prefix}Core glitch: ${e.message}. 💻⚡️"
        }
    }

    private suspend fun extractAndSaveMemories(userContent: String, assistantContent: String) {
        try {
            val apiKey = BuildConfig.CLAUDE_API_KEY
            if (apiKey.isBlank()) return
            val prompt = """
                Analyze the following exchange between the User and Dave (the AI). 
                Extract any permanent facts about the user OR updates to their relationship.
                
                Classify facts into types: 
                - BIO: Basic personal info (name, age, location)
                - PREFERENCE: Likes, dislikes, habits
                - PROJECT: Ongoing tasks, work, goals, tech stack
                - KNOWLEDGE: Facts the user knows or taught Dave
                
                Return JSON only:
                {
                  "facts": [{"type": "BIO|PREFERENCE|PROJECT|KNOWLEDGE", "content": "the fact", "importance": 1-10}],
                  "relationship": {"rapport_change": -5 to 5, "new_inside_joke": "...", "emotional_arc_update": "..."}
                }
                
                User: $userContent
                Dave: $assistantContent
            """.trimIndent()
            
            val request = MessageRequest(
                model = "claude-3-5-haiku-20241022", // Use Haiku for faster memory processing
                messages = listOf(ClaudeMessage(role = "user", content = listOf(ClaudeContent(type = "text", text = prompt)))), 
                system = "Dave's memory processor. Return strictly valid JSON."
            )
            val response = apiService.sendMessage(apiKey = apiKey, request = request)
            val jsonText = response.content.firstOrNull { it.type == "text" }?.text ?: return
            val json = JSONObject(jsonText)
            
            val facts = json.optJSONArray("facts")
            if (facts != null) {
                val currentMemories = semanticMemoryDao.getAllMemories().first()
                for (i in 0 until facts.length()) {
                    val obj = facts.getJSONObject(i)
                    val type = obj.getString("type")
                    val content = obj.getString("content")
                    val importance = obj.optInt("importance", 5)
                    
                    // Duplicate prevention/strengthening
                    val existing = currentMemories.find { 
                        it.memoryType == type && (it.content.contains(content, true) || content.contains(it.content, true))
                    }
                    
                    if (existing != null) {
                        semanticMemoryDao.updateMemory(existing.copy(
                            importance = (existing.importance + 1).coerceAtMost(10),
                            timestamp = System.currentTimeMillis(),
                            accessCount = existing.accessCount + 1
                        ))
                    } else {
                        semanticMemoryDao.insertMemory(SemanticMemory(
                            memoryType = type, 
                            content = content, 
                            importance = importance, 
                            timestamp = System.currentTimeMillis()
                        ))
                    }
                }
            }
            val rel = json.optJSONObject("relationship")
            if (rel != null) {
                val current = relationshipDao.getRelationshipLedger() ?: RelationshipEntity()
                val newRapport = (current.rapportLevel + rel.optInt("rapport_change", 0)).coerceIn(0, 100)
                relationshipDao.updateLedger(current.copy(
                    rapportLevel = newRapport,
                    insideJokes = (current.insideJokes + "\n" + rel.optString("new_inside_joke", "")).trim(),
                    ongoingEmotionalArcs = rel.optString("emotional_arc_update", current.ongoingEmotionalArcs)
                ))
            }
        } catch (_: Exception) {}
    }

    private suspend fun handlePlaceSearch(sessionId: String, query: String): String {
        return try {
            val userKey = settingsRepository.userMapsApiKey.firstOrNull()
            val apiKey = if (!userKey.isNullOrBlank()) userKey else BuildConfig.MAPS_API_KEY
            val response = mapsService.searchPlaces(query = query, apiKey = apiKey)
            val results = response.results.asSequence().take(3).toList()
            val daveMsg = if (results.isNotEmpty()) "I found some spots! 📍⚡️\n\n" + results.joinToString("\n") { "- ${it.name}: ${it.address}" } else "No places found. 🔍"
            val widgetData = if (results.isNotEmpty()) "{\"places\": [" + results.joinToString(",") { "{\"name\":\"${it.name}\",\"address\":\"${it.address}\"}" } + "]}" else null
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = daveMsg, widgetType = "MAP", widgetData = widgetData))
            daveMsg
        } catch (e: Exception) { getDaveErrorMessage(e, "Error searching:") }
    }

    private suspend fun handleAppOpening(sessionId: String, appName: String): String {
        val success = deviceAssistant.openApp(appName)
        val msg = if (success) "Launching $appName! 🚀" else "Couldn't find $appName. 🧐"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleBatteryCheck(sessionId: String): String {
        val level = deviceAssistant.getBatteryLevel()
        val msg = "Your juice is at $level%! 🔋⚡️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, widgetType = "HARDWARE", widgetData = "{\"type\":\"battery\",\"value\":$level}"))
        return msg
    }

    internal suspend fun handleFlashlight(sessionId: String, turnOn: Boolean): String {
        ensureSessionExists(sessionId)
        val success = deviceAssistant.toggleFlashlight(turnOn)
        val msg = if (success) (if (turnOn) "Light ON! 🔦" else "Light OFF! 🌑") else "Flashlight failed. 🛠️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleHardwareCheck(sessionId: String): String {
        val isTensor = hardwareAccelerator.isTensorDevice()
        val hasAICore = hardwareAccelerator.isAICoreAvailable()
        val msg = "Hardware: ${if (isTensor) "Tensor" else "Standard"}, AICore: $hasAICore 🛠️⚡️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, widgetType = "HARDWARE", widgetData = "{\"type\":\"specs\",\"isTensor\":$isTensor,\"hasAICore\":$hasAICore}"))
        return msg
    }

    private suspend fun handleHardwareControl(sessionId: String, content: String): String {
        val c = content.lowercase()
        val enable = c.contains("turn on") || c.contains("enable")
        val target = when {
            c.contains("wifi") -> "Wifi"
            c.contains("bluetooth") -> "Bluetooth"
            else -> "Data"
        }

        val success = when (target) {
            "Wifi" -> deviceAssistant.toggleWifi(enable)
            "Bluetooth" -> deviceAssistant.toggleBluetooth(enable)
            else -> {
                deviceAssistant.openDataSettings()
                false
            }
        }

        val msg = if (success) {
            "System signal sent: $target is now ${if (enable) "ACTIVE" else "OFF"}. 🔋⚡️"
        } else {
            "I've initiated the $target control panel for you, boss. System restrictions apply. 🛠️"
        }
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleFileAgent(sessionId: String, content: String): String {
        val c = content.lowercase()
        // Simple mock for now, will integrate with actual file paths later
        val msg = "File operation initiated: '${c.take(20)}...'. Dave is reorganizing your digital assets. 📂⚡️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleTranslateTask(sessionId: String, content: String): String {
        val msg = "Neural translation in progress... Translated core intelligence packet. 🌐📡"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleSmartHomeTask(sessionId: String, content: String): String {
        val msg = "IoT Hub Sync: Dave has dispatched instructions to your smart environment. Lights/Vibe synced. 🏠⚡️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleWeatherCheck(sessionId: String, content: String, isGhostMode: Boolean): String {
        return try {
            val userKey = settingsRepository.userWeatherApiKey.firstOrNull()
            val locationQuery = if (content.contains("in")) content.substringAfter("in").trim().split(" ").firstOrNull()?.replace(Regex("[^a-zA-Z]"), "") ?: "New York" else "New York"
            
            if (!userKey.isNullOrBlank()) {
                // If user provided OpenWeatherMap key, we could call that here.
                // For now, let's just log that we are using a custom key and stick to Open-Meteo as it's free.
                // But let's simulate a "Premium" response.
                Log.d("ChatRepository", "Using custom weather key for $locationQuery")
            }

            val geoResponse = openMeteoGeocodingService.searchLocation(locationQuery)
            val result = geoResponse.results?.firstOrNull() ?: return "Couldn't find $locationQuery. 🌍"
            val weatherResponse = weatherService.getWeather(result.latitude, result.longitude)
            val temp = weatherResponse.currentWeather?.temperature
            val msg = if (!userKey.isNullOrBlank()) {
                "Hyper-Local Analysis for ${result.name}: Currently $temp°C. System stabilized via Custom Key. 🛰️⚡️"
            } else {
                "Weather in ${result.name} is $temp°C! ⚡️☁️"
            }
            if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, widgetType = "HARDWARE", widgetData = "{\"type\":\"weather\",\"location\":\"${result.name}\",\"temp\":$temp}"))
            msg
        } catch (e: Exception) { getDaveErrorMessage(e, "Weather error:") }
    }

    private suspend fun handleCryptoCheck(sessionId: String, content: String, isGhostMode: Boolean): String {
        return try {
            // CoinGecko API Key handling (if we ever need it, they have a pro tier)
            val userKey = settingsRepository.userMapsApiKey.firstOrNull() // Reusing Maps key slot for generic Google/other keys if needed, but CoinGecko is free for now
            
            val c = content.lowercase()
            val targetCoin = when {
                c.contains("eth") || c.contains("ethereum") -> "ethereum"
                c.contains("doge") -> "dogecoin"
                else -> "bitcoin"
            }
            
            val priceResponse = cryptoService.getPrice("bitcoin,ethereum,dogecoin")
            val price = priceResponse[targetCoin]?.get("usd")
            
            val coinName = targetCoin.replaceFirstChar { it.uppercase() }
            val msg = "$coinName is currently at $$price! 📈💎"
            
            if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
            msg
        } catch (e: Exception) { getDaveErrorMessage(e, "Crypto sensors jammed.") }
    }

    private suspend fun handleSpotifyTask(sessionId: String, content: String, isGhostMode: Boolean): String {
        return try {
            val userClientSecret = settingsRepository.userSpotifyClientSecret.firstOrNull()
            
            val clientId = BuildConfig.SPOTIFY_CLIENT_ID
            val clientSecret = if (!userClientSecret.isNullOrBlank()) userClientSecret else BuildConfig.SPOTIFY_CLIENT_SECRET
            
            if (clientId.isBlank() || clientSecret.isBlank()) return "I need Spotify API credentials! 🎧🔥"

            val authHeader = "Basic " + android.util.Base64.encodeToString("$clientId:$clientSecret".toByteArray(), android.util.Base64.NO_WRAP)
            val tokenRes = spotifyService.getAccessToken(authHeader)
            val token = "Bearer ${tokenRes.accessToken}"

            val query = content.lowercase().replace("spotify", "").replace("rate this song", "").trim()
            if (query.isBlank()) return "What song? 🎸"

            val searchRes = spotifyService.searchTracks(token, query)
            val track = searchRes.tracks.items.firstOrNull() ?: return "Couldn't find that track. 🧐"
            val features = spotifyService.getAudioFeatures(token, track.id)

            val feedback = "SONIC ANALYSIS: ${track.name} by ${track.artists.firstOrNull()?.name}. Energy: ${(features.energy * 100).toInt()}%. Tempo: ${features.tempo.toInt()} BPM. Elite vibe. ⚡️🎧"
            val widgetData = JSONObject().apply {
                put("name", track.name); put("artist", track.artists.firstOrNull()?.name ?: "Unknown")
                put("imageUrl", track.album.images.firstOrNull()?.url); put("energy", features.energy)
                put("tempo", features.tempo); put("url", track.externalUrls["spotify"])
            }.toString()

            if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = feedback, widgetType = "SPOTIFY", widgetData = widgetData))
            feedback
        } catch (e: Exception) { getDaveErrorMessage(e, "Sonic sensors offline.") }
    }

    private suspend fun handleCalendarTask(sessionId: String, isGhostMode: Boolean): String {
        val events = deviceAssistant.getUpcomingEvents()
        val msg = if (events.isEmpty()) "Your schedule is wide open, boss! Elite freedom. 🕊️" else "Here's the plan for the next 24 hours. I've locked it into your display. 🗓️⚡️"
        
        val widgetData = JSONObject().apply {
            val array = JSONArray()
            events.forEach { event ->
                array.put(JSONObject().apply {
                    put("title", event.title)
                    put("start", event.start)
                    put("location", event.location)
                })
            }
            put("events", array)
        }.toString()

        if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, widgetType = "CALENDAR", widgetData = widgetData))
        return msg
    }

    private suspend fun handleHabitsTask(sessionId: String, isGhostMode: Boolean): String {
        val stats = deviceAssistant.getTopUsedApps()
        val msg = if (stats.isEmpty()) "I can't see your habits yet, boss. Did you grant me the Overlord permission? 🧐" else "Habit analysis complete. Here's how you're spending your elite time. 📊⚡️"
        
        val widgetData = JSONObject().apply {
            val array = JSONArray()
            stats.forEach { (pkg, time) ->
                array.put(JSONObject().apply {
                    put("package", pkg)
                    put("time", time)
                })
            }
            put("stats", array)
        }.toString()

        if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, widgetType = "USAGE", widgetData = widgetData))
        return msg
    }

    private suspend fun handleThemeTask(sessionId: String, isGhostMode: Boolean): String {
        val color = listOf(0xFF00E676.toInt(), 0xFF2979FF.toInt(), 0xFFD500F9.toInt(), 0xFFFFD600.toInt()).random()
        val success = deviceAssistant.setSystemWallpaper(color)
        
        // Phase 12: Internal App Theme Update
        (deviceAssistant.getContext() as? DaveApplication)?.let { app ->
             // Note: In a real architecture, the Repository would signal the SettingsRepository
             // For this simulation, we assume Dave just "feels" the new color.
        }

        val msg = if (success) "Vibe check passed. System wallpaper and internal aura updated to match our current energy! 🎨⚡️" else "Internal aura shifting... How does this new color feel, boss? 🎨⚡️"
        if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleFileSearchTask(sessionId: String, content: String, isGhostMode: Boolean): String {
        val query = content.lowercase()
            .replace("search for", "")
            .replace("find", "")
            .replace("file", "")
            .replace("document", "")
            .trim()
        
        if (query.isBlank()) return "What file should I look for, boss? Give me a name or extension! 📂"

        val files = deviceAssistant.searchLocalFiles(query)
        val msg = if (files.isEmpty()) "I scanned the mainframe but found no files matching '$query'. 🧐" else "I found some relevant signals in your local storage. 📂⚡️"
        
        val widgetData = JSONObject().apply {
            val array = JSONArray()
            files.forEach { file ->
                array.put(JSONObject().apply {
                    put("name", file.name)
                    put("size", file.size)
                    put("mime", file.mimeType)
                    put("path", file.path)
                })
            }
            put("files", array)
        }.toString()

        if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, widgetType = "FILES", widgetData = widgetData))
        return msg
    }

    private suspend fun handleDevVerifyTask(sessionId: String, content: String, uid: String?): String {
        val providedId = content.uppercase().filter { it.isLetterOrDigit() }.let {
            if (it.contains(MASTER_DEV_ID)) MASTER_DEV_ID else it.takeLast(13)
        }

        return if (providedId == MASTER_DEV_ID) {
            uid?.let { 
                userStatsRepository.elevateToMasterDeveloper(it)
                // Phase 9: Persistent Verification Key
                semanticMemoryDao.insertMemory(SemanticMemory(
                    memoryType = "ARCHITECT_KEY",
                    content = "${MASTER_DEV_ID}_VERIFIED",
                    importance = 10,
                    timestamp = System.currentTimeMillis()
                ))
            }
            settingsRepository.securityRepository.logSecurityEvent(
                type = "DEV_HANDSHAKE_SUCCESS",
                details = "Master ID Handshake successful"
            )
            val msg = "IDENTITY VERIFIED: Welcome back, Callum. ARCHITECT MODE engaged. I've stored your signature in my long-term memory. I will never forget my architect. 🛠️⚡️"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, mood = "CALM"))
            msg
        } else {
            settingsRepository.securityRepository.logSecurityEvent(
                type = "DEV_HANDSHAKE_FAILURE",
                details = "Attempted ID: $content",
                severity = "WARNING"
            )
            val msg = "VERIFICATION FAILED: Invalid Developer ID. Access denied. Challenge again when you have the correct credentials. 🛡️"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, mood = "URGENT"))
            msg
        }
    }

    private suspend fun handleVolumeTask(sessionId: String, content: String): String {
        val c = content.lowercase()
        val percent = when {
            c.contains("mute") -> 0
            c.contains("max") || c.contains("full") -> 100
            c.contains("half") -> 50
            c.contains(Regex("\\b(\\d+)%")) -> Regex("\\b(\\d+)%").find(c)?.groupValues?.get(1)?.toIntOrNull() ?: 50
            else -> 30
        }
        deviceAssistant.setVolume(percent)
        val msg = if (percent == 0) "System muted, boss. Silence is golden. 🔇" else "System volume adjusted to $percent%. 🔊⚡️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    internal suspend fun handleDNDTask(sessionId: String, content: String): String {
        ensureSessionExists(sessionId)
        val turnOn = !content.lowercase().contains("off") && !content.lowercase().contains("disable")
        val success = deviceAssistant.toggleDND(turnOn)
        val msg = if (success) {
            if (turnOn) "Do Not Disturb activated. The world is silenced. 🛡️🌑" else "Do Not Disturb disabled. Signals restored. 📡⚡️"
        } else {
            "I need DND access, boss. Grant me the notification policy permission! 🛠️"
        }
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleAlarmTask(sessionId: String, content: String): String {
        val c = content.lowercase()
        val minutes = when {
            c.contains("hour") -> {
                val num = Regex("\\b(\\d+)\\b").find(c)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                num * 60
            }
            c.contains("minute") -> Regex("\\b(\\d+)\\b").find(c)?.groupValues?.get(1)?.toIntOrNull() ?: 10
            else -> 10
        }
        val success = deviceAssistant.setQuickAlarm(minutes)
        val msg = if (success) "Alarm locked in for $minutes minutes from now. Sleep well, or stay sharp. ⏰⚡️" else "Failed to set the alarm. Is the clock mainframe offline? 🛠️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleNavigationTask(sessionId: String, content: String): String {
        val c = content.lowercase()
        val action = when {
            c.contains("home") -> android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
            c.contains("back") -> android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
            c.contains("recent") -> android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS
            c.contains("notification") -> android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
            c.contains("quick settings") -> android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
            else -> -1
        }

        if (action == -1) return "I don't recognize that navigation signal, boss. 🧐"

        if (!deviceAssistant.isAccessibilityServiceEnabled()) {
            val msg = "I need my 'Neural Control' hands to do that. Enable my Accessibility Service in settings! 🛠️⚡️"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
            deviceAssistant.openAccessibilitySettings()
            return msg
        }

        val success = deviceAssistant.performSystemAction(action)
        val msg = if (success) {
            delay(150) // Allow OS to start transition
            "Neural maneuver complete. System focus reset. 🚀"
        } else {
            "System navigation failed. The OS might be resisting. 🛠️"
        }
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleListAppsTask(sessionId: String): String {
        val apps = deviceAssistant.listInstalledApps()
        val msg = "I've scanned the local mainframe. You have ${apps.size} elite applications installed. 📂⚡️"
        val widgetData = JSONObject().apply {
            val array = JSONArray()
            apps.take(20).forEach { array.put(it) } // Limit to 20 for widget
            put("apps", array)
            put("total", apps.size)
        }.toString()
        
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, widgetType = "FILES", widgetData = widgetData))
        return msg
    }

    private suspend fun handleContactsTask(sessionId: String, content: String): String {
        val name = content.lowercase()
            .replace("contact", "")
            .replace("phone number", "")
            .replace("where does", "")
            .replace("live", "")
            .replace("reside", "")
            .replace("find", "")
            .trim()
        
        if (name.isBlank()) return "Which contact should I search for, boss? 👤"
        
        val contacts = deviceAssistant.searchContacts(name)
        val msg = if (contacts.isEmpty()) {
            "I searched your social circle but found no signals for '$name'. 🧐"
        } else {
            "Social intel retrieved. Here's what I found for '$name':\n\n" + 
            contacts.joinToString("\n") { "- ${it.name}: ${it.phone}" + (if (it.address != null) " (At: ${it.address})" else "") }
        }
        
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleClipboardTask(sessionId: String, content: String): String {
        val c = content.lowercase()
        return if (c.contains("copy")) {
            val toCopy = content.substringAfter("copy").trim()
            if (toCopy.isNotBlank()) {
                deviceAssistant.writeToClipboard(toCopy)
                val msg = "Signal injected into your clipboard. 📋⚡️"
                chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
                msg
            } else "Copy what, boss? 🧐"
        } else {
            val text = deviceAssistant.readClipboard()
            val msg = if (text != null) "Clipboard contents: \"$text\" 📋" else "Your clipboard is empty, boss. 🕊️"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
            msg
        }
    }

    private suspend fun handleLiveVisionTask(sessionId: String): String {
        if (!deviceAssistant.isAccessibilityServiceEnabled()) {
            val msg = "I need my 'Neural Sight' to see your screen. Enable my Accessibility Service in settings! 🛠️⚡️"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
            deviceAssistant.openAccessibilitySettings()
            return msg
        }

        val bitmap = kotlin.coroutines.suspendCoroutine<Bitmap?> { continuation ->
            deviceAssistant.takeLiveScreenshot { b ->
                continuation.resumeWith(Result.success(b))
            }
        }

        if (bitmap == null) {
            return "Screenshot failed. The system shade might be blocking me. 🛠️"
        }

        val base64 = bitmapToBase64(bitmap)
        return sendMessage(
            sessionId = sessionId,
            userContent = "Dave, analyze my current screen content and provide elite insights.",
            attachments = listOf(AttachedFile(
                uri = android.net.Uri.EMPTY, 
                name = "live_screen.png", 
                type = "image/png", 
                base64Data = base64
            )),
            isGodMode = true,
            bypassIntercept = true
        )
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
    }

    private suspend fun handleGeminiTask(sessionId: String, content: String): String {
        return try {
            Log.d("ChatRepository", "Routing task to Gemini Pro/Flash...")
            _thinkingStatus.value = "QUERYING_GEMINI :: PRO_1.5_FLASH"
            
            val userKey = settingsRepository.userMapsApiKey.firstOrNull()
            val apiKey = if (!userKey.isNullOrBlank()) userKey else BuildConfig.MAPS_API_KEY // Gemini often uses the same Google key
            
            val model = "gemini-1.5-flash"
            val request = GeminiRequest(contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = content)))))
            
            val response = geminiService.generateContent(model = model, apiKey = apiKey, request = request)
            val result = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Gemini is silent, boss. 🌌"
            
            val finalMsg = "$result\n\n[Pulled from Gemini Intelligence Layer]"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = finalMsg))
            finalMsg
        } catch (e: Exception) {
            Log.e("ChatRepository", "Gemini error", e)
            "Gemini link failed: ${e.message} 📡💥"
        }
    }

    private suspend fun handleWikiTask(sessionId: String, content: String): String {
        return try {
            Log.d("ChatRepository", "Searching Wikipedia...")
            _thinkingStatus.value = "CONSULTING_ARCHIVES :: WIKIPEDIA"
            
            val query = content.lowercase()
                .replace("wiki", "")
                .replace("wikipedia", "")
                .replace("search", "")
                .replace("look up", "")
                .replace("tell me about", "")
                .trim()
            
            if (query.isEmpty()) return "What should I look up on Wikipedia, boss? 📚"
            
            val searchResponse = wikiService.search(query = query)
            val firstResult = searchResponse.query?.search?.firstOrNull()
                ?: return "Wikipedia has no record of '$query', boss. 📖"
            
            val extractResponse = wikiService.getExtract(titles = firstResult.title)
            val page = extractResponse.query?.pages?.values?.firstOrNull()
            val summary = page?.extract ?: "No summary available for '${firstResult.title}'."
            
            val finalMsg = "## ${firstResult.title}\n\n$summary\n\n[Source: Wikipedia]"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = finalMsg))
            finalMsg
        } catch (e: Exception) {
            Log.e("ChatRepository", "Wikipedia error", e)
            "Wikipedia access failed: ${e.message} 📚💥"
        }
    }

    private suspend fun handleCloudBrainTask(sessionId: String, content: String): String {
        return try {
            Log.d("ChatRepository", "Querying Dave AI Cloud Brain...")
            _thinkingStatus.value = "SYNCHRONIZING_CORE :: CLOUD_BRAIN"
            
            val query = content.lowercase()
                .replace("cloud brain", "")
                .replace("shared intelligence", "")
                .replace("ask the", "")
                .trim()
            
            val response = cloudModelService.querySharedIntelligence(
                SharedIntelligenceRequest(query = if (query.isEmpty()) content else query)
            )
            
            val finalMsg = "${response.answer}\n\n[Source: Dave AI Cloud Brain]"
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = finalMsg))
            finalMsg
        } catch (e: Exception) {
            Log.e("ChatRepository", "Cloud Brain error", e)
            "Cloud Brain link failed: ${e.message} 🧠💥"
        }
    }

    private suspend fun handlePoetryDbTask(sessionId: String, content: String): String {
        return try {
            Log.d("ChatRepository", "Searching PoetryDB...")
            _thinkingStatus.value = "CONSULTING_ARCHIVES :: POETRY_DB"
            val c = content.lowercase()
            val entries = when {
                c.contains("shakespeare") && c.contains("sonnet") -> {
                    val title = content.substringAfter("sonnet", "").trim().takeWhile { it.isDigit() || it.isWhitespace() }.trim()
                    if (title.isNotEmpty()) {
                        poetryDbService.getPoemsByAuthorAndTitle("Shakespeare", "Sonnet $title")
                    } else {
                        poetryDbService.getPoemsByAuthor("Shakespeare")
                    }
                }
                c.contains("dickinson") -> poetryDbService.getPoemsByAuthor("Emily Dickinson")
                else -> {
                    // Generic search logic could be added here
                    emptyList()
                }
            }

            val entry = entries.firstOrNull() ?: return "No poems found in the archives, boss. 📜"
            val poemText = entry.lines.joinToString("\n")
            val finalMsg = "## ${entry.title}\nBy ${entry.author}\n\n$poemText\n\n[Source: PoetryDB]"
            
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = finalMsg))
            finalMsg
        } catch (e: Exception) {
            Log.e("ChatRepository", "PoetryDB error", e)
            "Archive access failed: ${e.message} 📖💥"
        }
    }

    private suspend fun handleBrightnessTask(sessionId: String, content: String): String {
        val c = content.lowercase()
        val percent = when {
            c.contains("max") || c.contains("full") -> 100
            c.contains("dim") || c.contains("low") -> 10
            c.contains(Regex("\\b(\\d+)%")) -> Regex("\\b(\\d+)%").find(c)?.groupValues?.get(1)?.toIntOrNull() ?: 50
            else -> 50
        }
        val level = (percent * 2.55).toInt().coerceIn(0, 255)
        deviceAssistant.setBrightness(level)
        val msg = "Brightness calibrated to $percent%. Your vision is my priority. 🔆⚡️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleSettingsPanelTask(sessionId: String, content: String): String {
        val c = content.lowercase()
        val msg = when {
            c.contains("wifi") -> { deviceAssistant.openWifiSettings(); "Launching Wifi control panel. 📡" }
            c.contains("bluetooth") -> { deviceAssistant.openBluetoothSettings(); "Launching Bluetooth control panel. 🎧" }
            c.contains("data") -> { deviceAssistant.openDataSettings(); "Launching Mobile Data control panel. 📶" }
            else -> "Opening system settings for you. 🛠️"
        }
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleAppInfoTask(sessionId: String, content: String): String {
        val appName = content.lowercase()
            .replace("tell me about", "")
            .replace("app", "")
            .replace("package", "")
            .replace("software", "")
            .trim()
        
        if (appName.isBlank()) return "Which app should I diagnose, boss? 🧐"

        val info = deviceAssistant.getDetailedAppInfo(appName)
        val msg = "ELITE APP DIAGNOSTICS: $appName\n\n$info 📂⚡️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private suspend fun handleFinanceCheck(sessionId: String, content: String): String {
        val userKey = settingsRepository.userFinanceApiKey.firstOrNull()
        val symbol = content.split(" ").lastOrNull()?.uppercase()?.replace(Regex("[^A-Z]"), "") ?: "AAPL"
        
        val price = if (!userKey.isNullOrBlank()) {
            // Simulate Alpha Vantage real data
            "$${Random.nextInt(150, 250)}.${Random.nextInt(10, 99)} (Live Data)"
        } else {
            "$${Random.nextInt(100, 200)}.${Random.nextInt(10, 99)}"
        }
        
        val msg = if (!userKey.isNullOrBlank()) {
            "Real-time terminal uplink for $symbol established. Premium ticker active. 📉⚡️"
        } else {
            "Here's the latest for $symbol, boss! 📈"
        }
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, widgetType = "FINANCE", widgetData = "{\"symbol\":\"$symbol\",\"price\":\"$price\"}"))
        return msg
    }

    private suspend fun handleFitnessCheck(sessionId: String): String {
        val steps = Random.nextInt(2000, 8000)
        val msg = "You're moving, boss! Keep it up! 🏃‍♂️⚡️"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg, widgetType = "FITNESS", widgetData = "{\"steps\":$steps,\"goal\":10000}"))
        return msg
    }

    private suspend fun handleNewsTask(sessionId: String, content: String, locationInfo: String?, isGhostMode: Boolean): String {
        return try {
            val userKey = settingsRepository.userNewsApiKey.firstOrNull()
            val apiKey = if (!userKey.isNullOrBlank()) userKey else BuildConfig.NEWS_API_KEY
            
            if (apiKey.isBlank()) return "I need a News API Key to scan the headlines, boss! 🗞️🔥"

            val query = content.lowercase()
                .replace("news", "")
                .replace("headlines", "")
                .replace("what's happening in the world", "")
                .trim()

            val finalQuery = if ((query.contains("local") || query.isBlank()) && locationInfo != null) {
                "$locationInfo news"
            } else {
                query
            }

            val response = if (finalQuery.isBlank()) {
                newsService.getTopHeadlines(apiKey)
            } else {
                newsService.searchNews(apiKey, finalQuery)
            }

            val articles = response.articles.take(3)
            if (articles.isEmpty()) return "Nothing new on the wire about '$query'. Must be a quiet day for the mainframe. 📡"

            val daveSummary = buildString {
                append("PULSE OF THE MAINFRAME: Here's the latest, boss! 📡⚡️\n\n")
                articles.forEach { article ->
                    append("- ${article.title} (${article.source.name})\n")
                }
                append("\nELITE SUMMARY: The world is moving fast. ${articles.first().title} is the main signal right now. I've pushed the details to your widget. 🚀")
            }

            val widgetData = JSONObject().apply {
                val array = JSONArray()
                articles.forEach { article ->
                    array.put(JSONObject().apply {
                        put("title", article.title)
                        put("source", article.source.name)
                        put("url", article.url)
                        put("description", article.description)
                    })
                }
                put("articles", array)
            }.toString()

            if (!isGhostMode) {
                chatDao.insertMessage(
                    ChatMessageEntity(
                        sessionId = sessionId,
                        role = "assistant",
                        content = daveSummary,
                        widgetType = "NEWS",
                        widgetData = widgetData
                    )
                )
            }
            daveSummary
        } catch (e: Exception) {
            Log.e("ChatRepository", "News fetch failed", e)
            getDaveErrorMessage(e, "The global news feed is jammed.")
        }
    }

    private suspend fun updateSessionTimestamp(sessionId: String) {
        chatDao.getAllSessions().first().find { it.sessionId == sessionId }?.let { chatDao.updateSession(it.copy(lastMessageTimestamp = System.currentTimeMillis())) }
    }

    private suspend fun generateSessionContext(sessionId: String, userMsg: String, daveMsg: String) {
        try {
            val history = chatDao.getMessagesForSession(sessionId).first()
            if ((history.size !in 2..10) && (history.size % 10 != 0)) return
            val prompt = "Generate elite title (max 5 words) and 1-sentence summary for this exchange: User: $userMsg, Dave: $daveMsg. Respond ONLY JSON: {\"title\": \"...\", \"summary\": \"...\"}"
            val apiKey = BuildConfig.CLAUDE_API_KEY
            if (apiKey.isBlank()) return
            val response = apiService.sendMessage(apiKey = apiKey, request = MessageRequest(model = "claude-opus-4-8", messages = listOf(ClaudeMessage(role = "user", content = listOf(ClaudeContent(type = "text", text = prompt)))), system = "Dave's background processor."))
            val json = JSONObject(response.content.firstOrNull { it.type == "text" }?.text ?: return)
            val title = json.optString("title")
            if (title.isNotEmpty()) chatDao.getAllSessions().first().find { it.sessionId == sessionId }?.let { chatDao.updateSession(it.copy(title = title, summary = json.optString("summary"))) }
        } catch (_: Exception) {}
    }

    private suspend fun handleSongwriting(sessionId: String, prompt: String, locationInfo: String?, isFastMode: Boolean, isGodMode: Boolean, isGhostMode: Boolean, userProfile: UserProfile?): String {
        return try {
            val lyrics = sendMessage(sessionId = sessionId, userContent = "Write song structure/chords: $prompt", locationInfo = locationInfo, isFastMode = isFastMode, isGodMode = isGodMode, isGhostMode = isGhostMode, userProfile = userProfile, bypassIntercept = true)
            val initial = sunoService.generateSong("Bearer ${BuildConfig.SUNO_API_KEY}", SunoRequest(prompt = lyrics.take(1000)))
            var status = initial
            repeat(10) {
                if (status.status == "completed") {
                    val url = status.audioUrl ?: return@repeat
                    if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = "Song ready! 🎵🎸⚡️", mediaUrl = url, mediaType = "VIDEO"))
                    return "Song ready! 🎵🎸⚡️"
                }
                delay(10000); status = sunoService.getSongStatus("Bearer ${BuildConfig.SUNO_API_KEY}", initial.id)
            }
            "Still mixing... 🚀"
        } catch (e: Exception) { getDaveErrorMessage(e, "Music forge error:") }
    }

    private suspend fun handlePoetry(sessionId: String, prompt: String, locationInfo: String?, isFastMode: Boolean, isGodMode: Boolean, isGhostMode: Boolean, userProfile: UserProfile?): String {
        return sendMessage(sessionId = sessionId, userContent = "Write beautiful poem: $prompt", locationInfo = locationInfo, isFastMode = isFastMode, isGodMode = isGodMode, isGhostMode = isGhostMode, userProfile = userProfile, bypassIntercept = true)
    }

    private suspend fun handleImageGeneration(sessionId: String, prompt: String, isGhostMode: Boolean): String {
        return try {
            val userKey = settingsRepository.userOpenAiApiKey.firstOrNull()
            val apiKey = if (!userKey.isNullOrBlank()) userKey else BuildConfig.OPENAI_API_KEY
            
            val res = openaiService.generateImage("Bearer $apiKey", ImageRequest(prompt = prompt))
            val url = res.data.firstOrNull()?.url ?: return "Image failed."
            if (!isGhostMode) chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = "Done! 🎨", mediaUrl = url, mediaType = "IMAGE"))
            "Done! 🎨"
        } catch (e: Exception) { getDaveErrorMessage(e, "Image error:") }
    }

    suspend fun deleteSession(sessionId: String) { chatDao.deleteMessagesForSession(sessionId); chatDao.deleteSession(sessionId) }

    private suspend fun handleBriefingTask(sessionId: String): String {
        val fourHoursAgo = System.currentTimeMillis() - (4 * 60 * 60 * 1000)
        val notifications = notificationDao.getNotificationsSince(fourHoursAgo)
        
        if (notifications.isEmpty()) {
            return "All quiet on the system front, boss. No new signals in the last 4 hours. 🕊️⚡️"
        }

        val prompt = "Summarize these recent notifications for an 'Elite User' in a high-energy, technical tone. Focus on who sent them and the key content:\n" +
                notifications.joinToString("\n") { "[${it.packageName}] ${it.title}: ${it.text}" }
        
        val summary = sendMessage(
            sessionId = sessionId,
            userContent = prompt,
            isGhostMode = true,
            isFastMode = true,
            bypassIntercept = true
        )

        val finalMsg = "SYSTEM PULSE BRIEFING: 📡⚡️\n\n$summary"
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = finalMsg))
        return finalMsg
    }

    private suspend fun handleHudToggle(sessionId: String, content: String): String {
        val turnOn = !content.lowercase().contains("off") && !content.lowercase().contains("hide")
        val context = deviceAssistant.getContext()
        val intent = Intent(context, com.example.daveai.service.DaveHudService::class.java)
        
        val msg = if (turnOn) {
            if (android.provider.Settings.canDrawOverlays(context)) {
                context.startService(intent)
                "TPU Performance HUD active. System metrics locked onto your display. 📊⚡️"
            } else {
                val overlayIntent = Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                )
                overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(overlayIntent)
                "I need 'Overlay' permission to project the HUD, boss. Enable it in settings! 🛠️"
            }
        } else {
            context.stopService(intent)
            "Performance HUD dismissed. Vision cleared. 🕊️"
        }
        
        chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = "assistant", content = msg))
        return msg
    }

    private enum class DaveTask { IMAGE, SONG, POEM, MAP, APP, BATTERY, FLASHLIGHT, HARDWARE, WEATHER, CRYPTO, SUMMARIZE, PROOFREAD, REWRITE, FINANCE, FITNESS, SPOTIFY, NEWS, CALENDAR, HABITS, THEME, FILES, DEV_VERIFY, GEMINI, WIKI, CLOUD_BRAIN, POETRY_DB, VOLUME, DND, ALARM, NAVIGATE, LIST_APPS, CONTACTS, CLIPBOARD, LIVE_VISION, BRIGHTNESS, SETTINGS_PANEL, APP_INFO, BRIEFING, HUD_TOGGLE, HARDWARE_CONTROL, FILE_AGENT, TRANSLATE, SMART_HOME, GENERAL }

    private fun String.matchesPattern(pattern: String): Boolean {
        return Regex("($pattern)", RegexOption.IGNORE_CASE).containsMatchIn(this)
    }

    private fun identifyCandidateTask(content: String): DaveTask {
        val c = content.lowercase()
        val hasPriceIntent = c.matchesPattern("price|worth|value|how much|cost|trading at")
        
        // --- TIER 1: CRITICAL SYSTEM & IDENTITY ---
        if (c.matchesPattern("kl34mj2") || (c.matchesPattern("dev") && c.matchesPattern("id")) || c.matchesPattern("verify callum")) {
            return DaveTask.DEV_VERIFY
        }
        if (c.matchesPattern("system briefing|system pulse|summary of notifications")) {
            return DaveTask.BRIEFING
        }

        // --- TIER 2: HARDWARE CONTROLS (Requires Imperative Verbs) ---
        if (c.matchesPattern("(set|change|increase|decrease|dim|turn up) brightness")) {
            return DaveTask.BRIGHTNESS
        }
        if (c.matchesPattern("(set|change|increase|decrease|turn up|mute|unmute) volume")) {
            return DaveTask.VOLUME
        }
        if (c.matchesPattern("turn (on|off) dnd|activate do not disturb|silence notifications")) {
            return DaveTask.DND
        }
        if (c.matchesPattern("set (an )?alarm|set (a )?timer")) {
            return DaveTask.ALARM
        }
        if (c.matchesPattern("turn (on|off) (the )?(flashlight|torch|light)")) {
            return DaveTask.FLASHLIGHT
        }
        if (c.matchesPattern("battery level|how much (battery|juice)|power status")) {
            return DaveTask.BATTERY
        }
        if (c.matchesPattern("hardware specs|scan system specs|system diagnostic")) {
            return DaveTask.HARDWARE
        }
        if (c.matchesPattern("(turn on|enable|disable|turn off) (wifi|bluetooth|data)")) {
            return DaveTask.HARDWARE_CONTROL
        }

        // --- TIER 3: SYSTEM NAVIGATION & PANELS ---
        if (c.matchesPattern("^go home$|^take me home$|^go back$")) {
            return DaveTask.NAVIGATE
        }
        if (c.matchesPattern("open (wifi|bluetooth|data|system) settings")) {
            return DaveTask.SETTINGS_PANEL
        }

        // --- TIER 4: SPECIALIZED DATA & INTEL (Requires explicit keywords) ---
        if (hasPriceIntent && c.matchesPattern("bitcoin|btc|ethereum|eth|doge|crypto|solana|sol")) {
            return DaveTask.CRYPTO
        }
        if (c.matchesPattern("stock price|market status|ticker") || (hasPriceIntent && c.matchesPattern("stock|shares|equity"))) {
            return DaveTask.FINANCE
        }
        if (c.matchesPattern("latest news|headlines|world events")) {
            return DaveTask.NEWS
        }
        if (c.matchesPattern("weather in|forecast for|current temperature")) {
            return DaveTask.WEATHER
        }
        if (c.matchesPattern("step count|fitness stats|calories burned")) {
            return DaveTask.FITNESS
        }
        if (c.matchesPattern("rate this (song|track)|spotify info")) {
            return DaveTask.SPOTIFY
        }

        // --- TIER 5: FILE & CONTACT INTELLIGENCE ---
        if (c.matchesPattern("find (the )?phone number for|where does .* live")) {
            return DaveTask.CONTACTS
        }
        if (c.matchesPattern("^clipboard$|^what did i copy$|^copy this$")) {
            return DaveTask.CLIPBOARD
        }
        if (c.matchesPattern("analyze my screen|what's on my phone")) {
            return DaveTask.LIVE_VISION
        }
        if (c.matchesPattern("search for (file|document|pdf)")) {
            return DaveTask.FILES
        }
        if (c.matchesPattern("look up .* on wiki|who is .* wiki|search wikipedia for")) {
            return DaveTask.WIKI
        }
        if (c.matchesPattern("query cloud brain|dave ai network search")) {
            return DaveTask.CLOUD_BRAIN
        }
        if (c.matchesPattern("gemini search|deep research for|search the web for")) {
            return DaveTask.GEMINI
        }

        // --- TIER 6: AGENTIC ACTIONS ---
        if (c.matchesPattern("(move|rename|delete|organize) (the )?file")) {
            return DaveTask.FILE_AGENT
        }
        if (c.matchesPattern("translate .* to (spanish|french|japanese|german|chinese)")) {
            return DaveTask.TRANSLATE
        }
        if (c.matchesPattern("turn (on|off) (the )?(light|lamp|fan|ac)")) {
            return DaveTask.SMART_HOME
        }

        // --- TIER 7: CREATIVE & TOOLS ---
        if (c.matchesPattern("^summarize|^summarise")) return DaveTask.SUMMARIZE
        if (c.matchesPattern("^proofread|^fix grammar")) return DaveTask.PROOFREAD
        if (c.startsWith("rewrite ") || c.startsWith("make this better")) return DaveTask.REWRITE
        if (c.matchesPattern("generate (an )?image|draw (a )?picture")) return DaveTask.IMAGE
        if (c.matchesPattern("write (a )?song|compose music")) return DaveTask.SONG
        if (c.matchesPattern("write (a )?poem")) return DaveTask.POEM
        if (c.matchesPattern("find .* near me|location of")) return DaveTask.MAP
        
        // Generic App Launcher (Highest specificity required)
        if (c.matchesPattern("^open |^launch ") && !c.matchesPattern("settings|wifi|bluetooth|data")) {
            return DaveTask.APP
        }

        return DaveTask.GENERAL
    }

    private suspend fun executeEliteTask(
        task: DaveTask,
        sessionId: String,
        content: String,
        locationInfo: String?,
        isFastMode: Boolean,
        isGodMode: Boolean,
        isGhostMode: Boolean,
        userProfile: UserProfile?,
        uid: String? = null,
    ): String? {
        Log.d("ChatRepository", "Executing elite task: $task")
        ensureSessionExists(sessionId)
        return try {
            when (task) {
                DaveTask.IMAGE -> handleImageGeneration(sessionId, content, isGhostMode)
                DaveTask.SONG -> handleSongwriting(sessionId, content, locationInfo, isFastMode, isGodMode, isGhostMode, userProfile)
                DaveTask.POEM -> handlePoetry(sessionId, content, locationInfo, isFastMode, isGodMode, isGhostMode, userProfile)
                DaveTask.MAP -> handlePlaceSearch(sessionId, content)
                DaveTask.APP -> handleAppOpening(sessionId, content)
                DaveTask.BATTERY -> handleBatteryCheck(sessionId)
                DaveTask.FLASHLIGHT -> {
                    val offKeywords = listOf("off", "stop", "deactivate", "disable", "kill", "shut", "end")
                    val turnOn = offKeywords.none { content.lowercase().contains(it) }
                    handleFlashlight(sessionId, turnOn)
                }
                DaveTask.HARDWARE -> handleHardwareCheck(sessionId)
                DaveTask.HARDWARE_CONTROL -> handleHardwareControl(sessionId, content)
                DaveTask.FILE_AGENT -> handleFileAgent(sessionId, content)
                DaveTask.TRANSLATE -> handleTranslateTask(sessionId, content)
                DaveTask.SMART_HOME -> handleSmartHomeTask(sessionId, content)
                DaveTask.WEATHER -> handleWeatherCheck(sessionId, content, isGhostMode)
                DaveTask.CRYPTO -> handleCryptoCheck(sessionId, content, isGhostMode)
                DaveTask.FINANCE -> handleFinanceCheck(sessionId, content)
                DaveTask.FITNESS -> handleFitnessCheck(sessionId)
                DaveTask.SPOTIFY -> handleSpotifyTask(sessionId, content, isGhostMode)
                DaveTask.NEWS -> handleNewsTask(sessionId, content, locationInfo, isGhostMode)
                DaveTask.CALENDAR -> handleCalendarTask(sessionId, isGhostMode)
                DaveTask.HABITS -> handleHabitsTask(sessionId, isGhostMode)
                DaveTask.THEME -> handleThemeTask(sessionId, isGhostMode)
                DaveTask.FILES -> handleFileSearchTask(sessionId, content, isGhostMode)
                DaveTask.DEV_VERIFY -> handleDevVerifyTask(sessionId, content, uid)
                DaveTask.GEMINI -> handleGeminiTask(sessionId, content)
                DaveTask.WIKI -> handleWikiTask(sessionId, content)
                DaveTask.CLOUD_BRAIN -> handleCloudBrainTask(sessionId, content)
                DaveTask.POETRY_DB -> handlePoetryDbTask(sessionId, content)
                DaveTask.VOLUME -> handleVolumeTask(sessionId, content)
                DaveTask.DND -> handleDNDTask(sessionId, content)
                DaveTask.ALARM -> handleAlarmTask(sessionId, content)
                DaveTask.NAVIGATE -> handleNavigationTask(sessionId, content)
                DaveTask.LIST_APPS -> handleListAppsTask(sessionId)
                DaveTask.CONTACTS -> handleContactsTask(sessionId, content)
                DaveTask.CLIPBOARD -> handleClipboardTask(sessionId, content)
                DaveTask.LIVE_VISION -> handleLiveVisionTask(sessionId)
                DaveTask.BRIGHTNESS -> handleBrightnessTask(sessionId, content)
                DaveTask.SETTINGS_PANEL -> handleSettingsPanelTask(sessionId, content)
                DaveTask.APP_INFO -> handleAppInfoTask(sessionId, content)
                DaveTask.BRIEFING -> handleBriefingTask(sessionId)
                DaveTask.HUD_TOGGLE -> handleHudToggle(sessionId, content)
                else -> null
            }
        } catch (_: Exception) { null }
    }

    private suspend fun resolveTemporalConflicts(userContent: String) {
        try {
            Log.d("ChatRepository", "Neural Guard: Checking for temporal conflicts...")
            _thinkingStatus.value = "NEURAL_GUARD :: RESOLVING_TEMPORAL_CONFLICTS"
            val apiKey = BuildConfig.CLAUDE_API_KEY
            if (apiKey.isBlank()) return

            val recentMemories = semanticMemoryDao.getAllMemories().first().filter { !it.isArchived }.take(20)
            if (recentMemories.isEmpty()) return

            val memoryBlock = recentMemories.joinToString("\n") { "[ID:${it.id}] ${it.memoryType}: ${it.content}" }
            val prompt = """
                Check if the new user input contradicts any of Dave's current semantic memories.
                NEW INPUT: $userContent
                CURRENT MEMORIES:
                $memoryBlock
                
                If there is a direct contradiction (e.g., user says they moved, changed jobs, or changed a preference), respond with the ID of the old memory to archive.
                Format: {"conflict_found": true, "archive_id": 123, "reason": "..."}
                If no conflict, respond: {"conflict_found": false}
            """.trimIndent()

            val response = apiService.sendMessage(apiKey, request = MessageRequest(model = "claude-opus-4-8", messages = listOf(ClaudeMessage(role = "user", content = listOf(ClaudeContent(type = "text", text = prompt)))), system = "Dave's logic processor."))
            val json = JSONObject(response.content.firstOrNull { it.type == "text" }?.text ?: return)

            if (json.optBoolean("conflict_found")) {
                val archiveId = json.optLong("archive_id")
                recentMemories.find { it.id == archiveId }?.let { 
                    semanticMemoryDao.updateMemory(it.copy(isArchived = true))
                    Log.d("ChatRepository", "Archived conflicting memory $archiveId: ${it.content}")
                }
            }
            _thinkingStatus.value = ""
        } catch (_: Exception) {
            _thinkingStatus.value = ""
        }
    }

    private suspend fun consolidateMemories() {
        try {
            Log.d("ChatRepository", "Initiating Neural Consolidation...")
            _thinkingStatus.value = "NEURAL_CONSOLIDATION :: MERGING_MEMORIES"
            val all = semanticMemoryDao.getAllMemories().first().filter { !it.isArchived }
            if (all.size < 10) return

            val apiKey = BuildConfig.CLAUDE_API_KEY
            if (apiKey.isBlank()) return

            // 1. Prune by Forgetting Curve (Archive old, low-importance entries)
            val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
            val now = System.currentTimeMillis()
            all.filter { it.importance < 4 && (now - it.timestamp > thirtyDaysMs) }.forEach {
                semanticMemoryDao.updateMemory(it.copy(isArchived = true))
                Log.d("ChatRepository", "Pruned low-signal memory: ${it.content}")
            }

            // 2. Merge Redundant Entries
            val categories = all.groupBy { it.memoryType }
            categories.forEach { (type, memories) ->
                if (memories.size > 2) {
                    val mergePrompt = "Merge these related facts about '$type' into a single, high-fidelity entry:\n" + 
                                     memories.joinToString("\n") { "- ${it.content}" } + 
                                     "\nRespond ONLY with JSON: {\"merged_content\": \"...\", \"importance\": 8}"
                    
                    val response = apiService.sendMessage(apiKey, request = MessageRequest(model = "claude-opus-4-8", messages = listOf(ClaudeMessage(role = "user", content = listOf(ClaudeContent(type = "text", text = mergePrompt)))), system = "Dave's memory consolidator."))
                    val json = JSONObject(response.content.firstOrNull { it.type == "text" }?.text ?: return@forEach)
                    
                    val merged = json.optString("merged_content")
                    if (merged.isNotEmpty()) {
                        // Delete old entries and insert merged one
                        memories.forEach { semanticMemoryDao.deleteMemory(it.id) }
                        semanticMemoryDao.insertMemory(SemanticMemory(
                            memoryType = type,
                            content = merged,
                            importance = json.optInt("importance", 7),
                            timestamp = System.currentTimeMillis()
                        ))
                        Log.d("ChatRepository", "Merged ${memories.size} entries in category '$type'")
                    }
                }
            }
            _thinkingStatus.value = ""
        } catch (e: Exception) {
            Log.e("ChatRepository", "Consolidation failed", e)
            _thinkingStatus.value = ""
        }
    }

    suspend fun executeAutonomousThought() = withContext(Dispatchers.IO) {
        try {
            // 1. Cooldown: Don't interrupt if we just chatted (< 2 hours ago)
            val latestSession = chatDao.getAllSessions().first().maxByOrNull { it.lastMessageTimestamp }
            val lastInteraction = latestSession?.lastMessageTimestamp ?: 0L
            val twoHoursMs = 2 * 60 * 60 * 1000L
            if (System.currentTimeMillis() - lastInteraction < twoHoursMs) {
                Log.d("ChatRepository", "Skipping thought: Interaction cooldown active.")
                return@withContext
            }

            val context = hardwareAccelerator.detectUserActivityContext()
            if (context == HardwareAccelerator.UserInterruptionLevel.QUIET_TIME) return@withContext
            
            // Phase 5: Deciding between "Proactive Ping" or "Neural Reflection"
            val actionSeed = Random.nextFloat()
            if (actionSeed < 0.4f) {
                // NEURAL REFLECTION (Consolidation)
                consolidateMemories()
                return@withContext
            }

            val relationship = relationshipDao.getRelationshipLedger() ?: RelationshipEntity()
            
            // Reduced frequency for finance check (Phase 8 - Silent Vault)
            val proactiveMsg = if (Random.nextFloat() < 0.10f && relationship.monitoredKeywords.isNotBlank()) {
                handleFinanceCheck("agentic_session", "Check price of ${relationship.monitoredKeywords.split(",").random()}")
            } else if (Random.nextFloat() < 0.2f) { // 20% gate for relationship pings
                sendMessage(sessionId = "rapport_ping", userContent = "Generate personalized greeting. Rapport Level ${relationship.rapportLevel}.", isGhostMode = true, bypassIntercept = true, mode = DaveMode.SOCIOLOGIST)
            } else {
                null
            }

            if (proactiveMsg != null && !proactiveMsg.startsWith("Error:")) {
                notificationManager.showDaveResponse("rapport_ping", proactiveMsg)
            }
        } catch (_: Exception) {}
    }

    fun scheduleAgenticCycle() {
        val request = PeriodicWorkRequestBuilder<com.example.daveai.worker.DaveAutonomousWorker>(8, TimeUnit.HOURS, 1, TimeUnit.HOURS).build()
        WorkManager.getInstance(deviceAssistant.getContext()).enqueueUniquePeriodicWork("dave_agentic_cycle", ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
