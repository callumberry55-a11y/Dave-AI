package com.example.daveai

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.daveai.data.repository.ChatRepository
import com.example.daveai.ui.auth.AuthScreen
import com.example.daveai.ui.auth.AuthViewModel
import com.example.daveai.ui.chat.ChatScreen
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.landing.LandingScreen
import com.example.daveai.ui.lessons.LessonsScreen
import com.example.daveai.ui.lessons.LessonsViewModel
import com.example.daveai.ui.live.LiveVoiceScreen
import com.example.daveai.ui.navigation.DaveRoute
import com.example.daveai.ui.riddle.RiddleScreen
import com.example.daveai.ui.riddle.RiddleViewModel
import com.example.daveai.ui.theme.DaveAITheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        val referralData = handleIntent(intent)
        val chatRepository = (application as DaveApplication).chatRepository

        setContent {
            DaveAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    DaveApp(chatRepository, referralData)
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?): Map<String, String?> {
        val data = intent?.data ?: return emptyMap()
        val campaign = data.getQueryParameter("cn")
        val source = data.getQueryParameter("cs")
        val medium = data.getQueryParameter("cm")
        
        val referral = mutableMapOf<String, String?>()
        campaign?.let { referral["campaign"] = it }
        source?.let { referral["source"] = it }
        medium?.let { referral["medium"] = it }
        
        if (referral.isNotEmpty()) {
            android.util.Log.d("DaveAI", "Launched with referral: $campaign / $source")
        }
        return referral
    }

    override fun onDestroy() {
        // Stop Dave from talking the second the user leaves or clears the app
        (application as DaveApplication).voiceManager.stop()
        super.onDestroy()
    }
}

@UnstableApi
@Composable
fun DaveApp(
    chatRepository: ChatRepository,
    referralData: Map<String, String?>,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as DaveApplication
    val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
    val startRoute = if (auth?.currentUser == null) DaveRoute.Auth else DaveRoute.Landing
    
    val authViewModel: AuthViewModel = viewModel()
    LaunchedEffect(referralData) {
        authViewModel.setReferralData(referralData)
    }
    
    val chatViewModel: ChatViewModel = viewModel {
        ChatViewModel(chatRepository)
    }

    val riddleViewModel: RiddleViewModel = viewModel {
        RiddleViewModel(
            riddleDao = app.chatRepository.getRiddleDao(),
            voiceManager = app.voiceManager,
            soundManager = app.riddleSoundManager,
            chatRepository = app.chatRepository,
        )
    }

    val lessonsViewModel: LessonsViewModel = viewModel {
        LessonsViewModel(app.chatRepository)
    }

    val backStack = rememberNavBackStack(startRoute)

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = { key ->
                when (key) {
                    is DaveRoute.Auth -> {
                        NavEntry(key) {
                            AuthScreen(
                                viewModel = authViewModel,
                            ) {
                                backStack.clear()
                                backStack.add(DaveRoute.Landing)
                            }
                        }
                    }
                    is DaveRoute.Landing -> {
                        NavEntry(key) {
                            LandingScreen(
                                riddleViewModel = riddleViewModel,
                                onNavigateToChat = { backStack.add(DaveRoute.Chat) },
                                onNavigateToRiddle = { backStack.add(DaveRoute.Riddle) },
                            ) { backStack.add(DaveRoute.Lessons) }
                        }
                    }
                    is DaveRoute.Chat -> {
                        NavEntry(key) {
                            ChatScreen(
                                viewModel = chatViewModel,
                                onLogout = {
                                    auth?.signOut()
                                    backStack.clear()
                                    backStack.add(DaveRoute.Auth)
                                },
                                onEnterRiddleRoom = {
                                    backStack.add(DaveRoute.Riddle)
                                }
                            ) {
                                backStack.add(DaveRoute.LiveVoice)
                            }
                        }
                    }
                    is DaveRoute.Riddle -> {
                        NavEntry(key) {
                            RiddleScreen(
                                viewModel = riddleViewModel,
                            ) { backStack.removeLastOrNull() }
                        }
                    }
                    is DaveRoute.Lessons -> {
                        NavEntry(key) {
                            LessonsScreen(
                                viewModel = lessonsViewModel,
                            ) { backStack.removeLastOrNull() }
                        }
                    }
                    is DaveRoute.LiveVoice -> {
                        NavEntry(key) {
                            LiveVoiceScreen(
                                viewModel = chatViewModel,
                                onClose = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                    else -> NavEntry(key) { Text("Unknown Route") }
                }
            }
        )
    }
}
