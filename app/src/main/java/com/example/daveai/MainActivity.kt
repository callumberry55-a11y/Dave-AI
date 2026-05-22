package com.example.daveai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.IntentCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.daveai.data.repository.ChatRepository
import com.example.daveai.data.repository.SettingsRepository
import com.example.daveai.ui.auth.AuthScreen
import com.example.daveai.ui.auth.AuthViewModel
import com.example.daveai.ui.chat.AttachedFile
import com.example.daveai.ui.chat.ChatScreen
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.components.AnimatedMeshBackground
import com.example.daveai.ui.components.LocalCyberIntensity
import com.example.daveai.ui.landing.LandingScreen
import com.example.daveai.ui.lessons.LessonsScreen
import com.example.daveai.ui.lessons.LessonsViewModel
import com.example.daveai.ui.live.LiveVoiceScreen
import com.example.daveai.ui.navigation.DaveRoute
import com.example.daveai.ui.riddle.RiddleScreen
import com.example.daveai.ui.riddle.RiddleViewModel
import com.example.daveai.ui.sanctum.SanctumScreen
import com.example.daveai.ui.terminal.EliteTerminalScreen
import com.example.daveai.ui.theme.DaveAITheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val referralData = handleIntent(intent)
        val triggerVault = intent.getBooleanExtra("openVault", false)
        val initialPrompt = intent.getStringExtra("initialPrompt")
        val initialImageUri = IntentCompat.getParcelableExtra(intent, "initialImageUri", Uri::class.java)

        val chatRepository = (application as DaveApplication).chatRepository
        val settingsRepository = SettingsRepository(this)

        setContent {
            val primaryColorInt by settingsRepository.primaryColor.collectAsState(initial = SettingsRepository.DEFAULT_COLOR)
            val useSystemWallpaper by settingsRepository.useSystemWallpaper.collectAsState(initial = false)
            val customWallpaperUri by settingsRepository.customWallpaperUri.collectAsState(initial = null)
            val cyberIntensity by settingsRepository.cyberIntensity.collectAsState(initial = 0.8f)
            val typographyStyle by settingsRepository.typographyStyle.collectAsState(initial = "MODERN")
            val meshAnimationSpeed by settingsRepository.meshAnimationSpeed.collectAsState(initial = 1.0f)
            val primaryColor = Color(primaryColorInt)

            CompositionLocalProvider(LocalCyberIntensity provides cyberIntensity) {
                DaveAITheme(
                    primaryColorOverride = primaryColor,
                    typographyStyle = typographyStyle
                ) {
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
                            DaveApp(
                                chatRepository = chatRepository, 
                                referralData = referralData, 
                                triggerVault = triggerVault,
                                initialPrompt = initialPrompt,
                                initialImageUri = initialImageUri,
                                settingsRepository = settingsRepository
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleIntent(intent: Intent?): Map<String, String?> {
        val data = mutableMapOf<String, String?>()
        intent?.data?.let { uri ->
            if (uri.host == "referral") {
                data["referrer"] = uri.getQueryParameter("id")
                data["source"] = uri.getQueryParameter("src")
            }
        }
        return data
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@UnstableApi
@Composable
fun DaveApp(
    chatRepository: ChatRepository,
    referralData: Map<String, String?>,
    triggerVault: Boolean = false,
    initialPrompt: String? = null,
    initialImageUri: Uri? = null,
    settingsRepository: SettingsRepository
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as DaveApplication
    val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
    val startRoute = if (auth?.currentUser == null) DaveRoute.Auth else {
        if (initialPrompt != null || initialImageUri != null) DaveRoute.Chat else DaveRoute.Landing
    }
    
    val authViewModel: AuthViewModel = viewModel()
    LaunchedEffect(referralData) {
        authViewModel.setReferralData(referralData)
    }
    
    val chatViewModel: ChatViewModel = viewModel {
        ChatViewModel(chatRepository, settingsRepository)
    }

    LaunchedEffect(triggerVault) {
        if (triggerVault) {
            chatViewModel.toggleVault(true)
        }
    }

    LaunchedEffect(initialPrompt, initialImageUri) {
        if (initialPrompt != null) {
            chatViewModel.onInputTextChanged(initialPrompt)
        }
        if (initialImageUri != null) {
            chatViewModel.addAttachment(
                AttachedFile(
                    uri = initialImageUri,
                    name = "Shared Image",
                    type = "image/jpeg"
                )
            )
        }
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
                                },
                                onEnterTerminal = {
                                    backStack.add(DaveRoute.Terminal)
                                },
                                onEnterSanctum = {
                                    backStack.add(DaveRoute.Sanctum)
                                },
                                onEnterLiveMode = {
                                    // backStack.add(DaveRoute.LiveVoice)
                                }
                            )
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
                            ) { backStack.removeLastOrNull() }
                        }
                    }
                    is DaveRoute.Terminal -> {
                        NavEntry(key) {
                            EliteTerminalScreen(
                                viewModel = chatViewModel,
                            ) { backStack.removeLastOrNull() }
                        }
                    }
                    is DaveRoute.Sanctum -> {
                        NavEntry(key) {
                            SanctumScreen(
                                viewModel = chatViewModel,
                            ) { backStack.removeLastOrNull() }
                        }
                    }
                    else -> NavEntry(key) { Text("Unknown Route") }
                }
            }
        )
    }
}
