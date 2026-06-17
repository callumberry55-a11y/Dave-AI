package com.example.daveai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.daveai.data.repository.ChatRepository
import com.example.daveai.data.repository.SettingsRepository
import com.example.daveai.ui.aura.AuraMarketplaceScreen
import com.example.daveai.ui.aura.PersonalityEditorScreen
import com.example.daveai.ui.auth.AuthScreen
import com.example.daveai.ui.auth.AuthViewModel
import com.example.daveai.ui.chat.AttachedFile
import com.example.daveai.ui.chat.ChatScreen
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.components.AnimatedMeshBackground
import com.example.daveai.ui.components.LocalCyberIntensity
import com.example.daveai.ui.landing.LandingScreen
import com.example.daveai.ui.live.LiveVoiceScreen
import com.example.daveai.ui.auth.IdentityVerificationScreen
import com.example.daveai.ui.navigation.DaveRoute
import com.example.daveai.ui.riddle.RiddleScreen
import com.example.daveai.ui.riddle.RiddleViewModel
import com.example.daveai.ui.sanctum.SanctumScreen
import com.example.daveai.ui.developer.DeveloperDashboardScreen
import com.example.daveai.ui.developer.DeveloperDashboardViewModel
import com.example.daveai.ui.theme.DaveAITheme
import com.example.daveai.ui.vault.SecuritySetupScreen
import com.example.daveai.ui.vault.SecurityViewModel
import com.example.daveai.ui.vault.VaultAuthScreen
import com.example.daveai.ui.vault.VaultScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : FragmentActivity() {
    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val referralData = handleIntent(intent)
        val triggerVault = intent.getBooleanExtra("openVault", false)
        val initialPrompt = intent.getStringExtra("initialPrompt")
        val sessionId = intent.getStringExtra("sessionId")
        val initialImageUri = IntentCompat.getParcelableExtra(intent, "initialImageUri", Uri::class.java)

        val app = application as DaveApplication
        val chatRepository = app.chatRepository
        val settingsRepository = app.settingsRepository

        // Start Dave's Sanctum Server
        com.example.daveai.service.DaveServerService.start(this)

        // Request notification permission (Required for Android 13+)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Retrieve the current FCM registration token
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_DEBUG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and Sync Token
            Log.d("FCM_DEBUG", "FCM Registration Token: $token")
            com.google.firebase.installations.FirebaseInstallations.getInstance().id.addOnCompleteListener { idTask ->
                if (idTask.isSuccessful) {
                    Log.d("FCM_DEBUG", "Firebase Instance ID: ${idTask.result}")
                }
            }
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                app.chatRepository.getScope().launch {
                    val userStatsRepo = com.example.daveai.data.repository.UserStatsRepository()
                    userStatsRepo.saveFcmToken(currentUser.uid, token)
                }
            }
        })

        setContent {
            val primaryColorInt by settingsRepository.primaryColor.collectAsState(initial = SettingsRepository.DEFAULT_COLOR)
            val useSystemWallpaper by settingsRepository.useSystemWallpaper.collectAsState(initial = false)
            val customWallpaperUri by settingsRepository.customWallpaperUri.collectAsState(initial = null)
            val cyberIntensity by settingsRepository.cyberIntensity.collectAsState(initial = 0.8f)
            val glowStrength by settingsRepository.glowStrength.collectAsState(initial = 0.5f)
            val blurIntensity by settingsRepository.blurIntensity.collectAsState(initial = 0.5f)
            val typographyStyle by settingsRepository.typographyStyle.collectAsState(initial = "MODERN")
            val meshAnimationSpeed by settingsRepository.meshAnimationSpeed.collectAsState(initial = 1.0f)
            val primaryColor = Color(primaryColorInt)

            CompositionLocalProvider(
                LocalCyberIntensity provides cyberIntensity,
                com.example.daveai.ui.components.LocalGlowStrength provides glowStrength,
                com.example.daveai.ui.components.LocalBlurIntensity provides blurIntensity
            ) {
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
                                sessionId = sessionId,
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
            // Capture all query parameters for general referral tracking
            uri.queryParameterNames.forEach { key ->
                data[key] = uri.getQueryParameter(key)
            }

            // Handle Preferred Network (Aura) redirect parameters
            if (uri.getQueryParameter("anid") == "aura" || uri.getQueryParameter("cs") == "Aura") {
                data["preferred_network"] = "Aura"
                data["click_id"] = uri.getQueryParameter("aclid")
                data["source"] = uri.getQueryParameter("cs") ?: "Aura"
                android.util.Log.d("DaveAuth", "Preferred Network detected: Aura. ClickId: ${data["click_id"]}")
            }

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
    sessionId: String? = null,
    initialImageUri: Uri? = null,
    settingsRepository: SettingsRepository
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as DaveApplication
    val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
    val startRoute = if (auth?.currentUser == null) DaveRoute.Auth else {
        if (initialPrompt != null || initialImageUri != null || sessionId != null) DaveRoute.Chat else DaveRoute.Landing
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

    LaunchedEffect(initialPrompt, initialImageUri, sessionId) {
        if (initialPrompt != null) {
            chatViewModel.onInputTextChanged(initialPrompt)
        }
        if (sessionId != null) {
            chatViewModel.selectSession(sessionId)
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
            settingsRepository = settingsRepository
        )
    }

    val securityViewModel: SecurityViewModel = viewModel {
        SecurityViewModel(settingsRepository)
    }

    val dashboardViewModel: DeveloperDashboardViewModel = viewModel {
        DeveloperDashboardViewModel(
            chatRepository = app.chatRepository,
            userStatsRepository = com.example.daveai.data.repository.UserStatsRepository(),
            securityEventDao = app.chatRepository.getSecurityEventDao()
        )
    }

    val backStack = rememberNavBackStack(startRoute)

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent<DaveRoute?>(
            targetState = backStack.lastOrNull() as? DaveRoute,
            transitionSpec = {
                val duration = 700
                (fadeIn(animationSpec = tween(duration, easing = FastOutSlowInEasing)) + 
                 scaleIn(initialScale = 0.92f, animationSpec = tween(duration, easing = FastOutSlowInEasing)))
                    .togetherWith(fadeOut(animationSpec = tween(duration / 2)) + 
                                  scaleOut(targetScale = 1.08f, animationSpec = tween(duration / 2)))
                    .using(SizeTransform(clip = false))
            },
            label = "nav_transition",
            modifier = Modifier.fillMaxSize()
        ) { targetKey ->
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = { key ->
                    // Filter entries to only show the one matching the current targetState of AnimatedContent
                    // to prevent rendering multiple screens during transition if NavDisplay handles its own internal state.
                    // However, NavDisplay typically only renders the last entry anyway.
                    if (key == targetKey) {
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
                                        viewModel = chatViewModel,
                                        onNavigateToChat = { backStack.add(DaveRoute.Chat) },
                                        onNavigateToRiddle = { backStack.add(DaveRoute.Riddle) },
                                        onEnterVault = { backStack.add(DaveRoute.VaultAuth) },
                                        onEnterSanctum = { backStack.add(DaveRoute.Sanctum) },
                                        onEnterDashboard = { backStack.add(DaveRoute.DeveloperDashboard) },
                                        onEnterMarketplace = { backStack.add(DaveRoute.AuraMarketplace) },
                                        onEnterPersonaEditor = { backStack.add(DaveRoute.PersonalityEditor) },
                                        onLogout = {
                                            authViewModel.logout()
                                            chatViewModel.reset()
                                            backStack.clear()
                                            backStack.add(DaveRoute.Auth)
                                        }
                                    )
                                }
                            }
                            is DaveRoute.Chat -> {
                                NavEntry(key) {
                                    ChatScreen(
                                        viewModel = chatViewModel,
                                        onLogout = {
                                            android.widget.Toast.makeText(context, "Logging out...", android.widget.Toast.LENGTH_SHORT).show()
                                            authViewModel.logout()
                                            chatViewModel.reset()
                                            backStack.clear()
                                            backStack.add(DaveRoute.Auth)
                                        },
                                        onEnterRiddleRoom = {
                                            backStack.add(DaveRoute.Riddle)
                                        },
                                        onEnterDashboard = {
                                            backStack.add(DaveRoute.DeveloperDashboard)
                                        },
                                        onEnterMarketplace = {
                                            backStack.add(DaveRoute.AuraMarketplace)
                                        },
                                        onEnterPersonaEditor = {
                                            backStack.add(DaveRoute.PersonalityEditor)
                                        },
                                        onEnterSanctum = {
                                            backStack.add(DaveRoute.Sanctum)
                                        },
                                        onEnterVault = {
                                            backStack.add(DaveRoute.VaultAuth)
                                        },
                                        onEnterLiveMode = {
                                            // backStack.add(DaveRoute.LiveVoice)
                                        },
                                        onEnterIdentityVerification = {
                                            backStack.add(DaveRoute.IdentityVerification)
                                        },
                                        onBackToHub = {
                                            backStack.removeLastOrNull()
                                        }
                                    )
                                }
                            }
                            is DaveRoute.Riddle -> {
                                NavEntry(key) {
                                    RiddleScreen(
                                        viewModel = riddleViewModel,
                                        onBack = { backStack.removeLastOrNull() },
                                        onEnterVault = { backStack.add(DaveRoute.VaultAuth) },
                                        onEnterSanctum = { backStack.add(DaveRoute.Sanctum) },
                                        onEnterDashboard = { backStack.add(DaveRoute.DeveloperDashboard) },
                                        onEnterMarketplace = { backStack.add(DaveRoute.AuraMarketplace) },
                                        onEnterPersonaEditor = { backStack.add(DaveRoute.PersonalityEditor) },
                                        onLogout = {
                                            authViewModel.logout()
                                            chatViewModel.reset()
                                            backStack.clear()
                                            backStack.add(DaveRoute.Auth)
                                        }
                                    )
                                }
                            }
                            is DaveRoute.LiveVoice -> {
                                NavEntry(key) {
                                    LiveVoiceScreen(
                                        viewModel = chatViewModel,
                                    ) { backStack.removeLastOrNull() }
                                }
                            }
                            is DaveRoute.DeveloperDashboard -> {
                                NavEntry(key) {
                                    DeveloperDashboardScreen(
                                        viewModel = dashboardViewModel,
                                    ) { backStack.removeLastOrNull() }
                                }
                            }
                            is DaveRoute.Sanctum -> {
                                NavEntry(key) {
                                    SanctumScreen(
                                        viewModel = chatViewModel,
                                        onBack = { backStack.removeLastOrNull() },
                                        onEnterVault = {
                                            backStack.add(DaveRoute.VaultAuth)
                                        }
                                    )
                                }
                            }
                            is DaveRoute.Vault -> {
                                NavEntry(key) {
                                    VaultScreen(
                                        viewModel = chatViewModel,
                                        onBack = { backStack.removeLastOrNull() },
                                        onNavigateToSecurity = { backStack.add(DaveRoute.SecuritySetup) }
                                    )
                                }
                            }
                            is DaveRoute.VaultAuth -> {
                                NavEntry(key) {
                                    VaultAuthScreen(
                                        viewModel = securityViewModel,
                                        onBack = { backStack.removeLastOrNull() },
                                        onAuthSuccess = {
                                            backStack.removeLastOrNull()
                                            backStack.add(DaveRoute.Vault)
                                        }
                                    )
                                }
                            }
                            is DaveRoute.SecuritySetup -> {
                                NavEntry(key) {
                                    SecuritySetupScreen(
                                        viewModel = securityViewModel,
                                        onBack = { backStack.removeLastOrNull() },
                                        onComplete = { backStack.removeLastOrNull() }
                                    )
                                }
                            }
                            is DaveRoute.AuraMarketplace -> {
                                NavEntry(key) {
                                    AuraMarketplaceScreen(
                                        viewModel = chatViewModel,
                                        onBack = { backStack.removeLastOrNull() }
                                    )
                                }
                            }
                            is DaveRoute.PersonalityEditor -> {
                                NavEntry(key) {
                                    PersonalityEditorScreen(
                                        viewModel = chatViewModel,
                                        onBack = { backStack.removeLastOrNull() }
                                    )
                                }
                            }
                            is DaveRoute.IdentityVerification -> {
                                NavEntry(key) {
                                    IdentityVerificationScreen(
                                        onBack = { backStack.removeLastOrNull() },
                                        onVerificationComplete = { success ->
                                            if (success) {
                                                backStack.removeLastOrNull()
                                            }
                                        }
                                    )
                                }
                            }
                            else -> NavEntry(key) { Text("Unknown Route") }
                        }
                    } else {
                        // Empty NavEntry for non-matching keys to satisfy NavDisplay
                        NavEntry(key) { Box(Modifier.fillMaxSize()) }
                    }
                }
            )
        }
    }
}
