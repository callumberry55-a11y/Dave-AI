package com.example.daveai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.example.daveai.ui.chat.ChatScreen
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.components.AnimatedMeshBackground
import com.example.daveai.ui.components.LocalCyberIntensity
import com.example.daveai.ui.riddle.RiddleScreen
import com.example.daveai.ui.riddle.RiddleViewModel
import com.example.daveai.ui.theme.DaveAITheme

enum class ChatActivityScreen {
    CHAT, RIDDLE
}

class ChatRoomActivity : ComponentActivity() {
    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as DaveApplication
        val chatRepository = app.chatRepository
        val voiceManager = app.voiceManager
        val riddleSoundManager = app.riddleSoundManager
        val riddleDao = chatRepository.getRiddleDao()
        val settingsRepository = app.settingsRepository
        val sessionId = intent.getStringExtra("sessionId")
        val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (_: Exception) { null }

        setContent {
            val primaryColorInt by settingsRepository.primaryColor.collectAsState(initial = com.example.daveai.data.repository.SettingsRepository.DEFAULT_COLOR)
            val useSystemWallpaper by settingsRepository.useSystemWallpaper.collectAsState(initial = false)
            val customWallpaperUri by settingsRepository.customWallpaperUri.collectAsState(initial = null)
            val cyberIntensity by settingsRepository.cyberIntensity.collectAsState(initial = 0.8f)
            val typographyStyle by settingsRepository.typographyStyle.collectAsState(initial = "MODERN")
            val meshAnimationSpeed by settingsRepository.meshAnimationSpeed.collectAsState(initial = 1.0f)
            val primaryColor = Color(primaryColorInt)

            val thoughtStream by chatRepository.consciousnessStream.collectAsState(initial = emptyList())
            val hasNeuralActivity = thoughtStream.any { System.currentTimeMillis() - it.timestamp < 300_000 }

            CompositionLocalProvider(LocalCyberIntensity provides cyberIntensity) {
                DaveAITheme(
                    primaryColorOverride = primaryColor,
                    typographyStyle = typographyStyle
                ) {
                    var currentScreen by rememberSaveable { mutableStateOf(ChatActivityScreen.CHAT) }

                    AnimatedMeshBackground(
                        primaryColor = primaryColor,
                        useSystemWallpaper = useSystemWallpaper,
                        customWallpaperUri = customWallpaperUri,
                        animationSpeed = meshAnimationSpeed
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent,
                        ) {
                            when (currentScreen) {
                                ChatActivityScreen.CHAT -> {
                                    val chatViewModel: ChatViewModel = viewModel {
                                        ChatViewModel(chatRepository, settingsRepository).apply {
                                            sessionId?.let { selectSession(it) }
                                        }
                                    }
                                    ChatScreen(
                                        viewModel = chatViewModel,
                                        onLogout = { 
                                            auth?.signOut()
                                            finish() 
                                        },
                                    ) { currentScreen = ChatActivityScreen.RIDDLE }
                                }
                                ChatActivityScreen.RIDDLE -> {
                                    val riddleViewModel: RiddleViewModel = viewModel {
                                        RiddleViewModel(riddleDao, voiceManager, riddleSoundManager, chatRepository, settingsRepository)
                                    }
                                    RiddleScreen(
                                        viewModel = riddleViewModel,
                                        onBack = { currentScreen = ChatActivityScreen.CHAT },
                                        onLogout = {
                                            auth?.signOut()
                                            finish()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
