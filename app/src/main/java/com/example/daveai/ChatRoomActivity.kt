package com.example.daveai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.example.daveai.ui.chat.ChatScreen
import com.example.daveai.ui.chat.ChatViewModel
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
        val sessionId = intent.getStringExtra("sessionId")

        setContent {
            DaveAITheme {
                var currentScreen by rememberSaveable { mutableStateOf(ChatActivityScreen.CHAT) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    when (currentScreen) {
                        ChatActivityScreen.CHAT -> {
                            val chatViewModel: ChatViewModel = viewModel {
                                ChatViewModel(chatRepository).apply {
                                    sessionId?.let { selectSession(it) }
                                }
                            }
                            ChatScreen(
                                viewModel = chatViewModel,
                                onLogout = { finish() },
                                onEnterRiddleRoom = { currentScreen = ChatActivityScreen.RIDDLE }
                            )
                        }
                        ChatActivityScreen.RIDDLE -> {
                            val riddleViewModel: RiddleViewModel = viewModel {
                                RiddleViewModel(riddleDao, voiceManager, riddleSoundManager)
                            }
                            RiddleScreen(
                                viewModel = riddleViewModel,
                                onBack = { currentScreen = ChatActivityScreen.CHAT }
                            )
                        }
                    }
                }
            }
        }
    }
}
