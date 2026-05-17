package com.example.daveai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.daveai.data.db.DaveDatabase
import com.example.daveai.data.network.*
import com.example.daveai.data.repository.ChatRepository
import com.example.daveai.ui.auth.AuthScreen
import com.example.daveai.ui.auth.AuthViewModel
import com.example.daveai.ui.chat.ChatScreen
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.navigation.DaveRoute
import com.example.daveai.ui.theme.DaveAITheme
import com.google.firebase.auth.FirebaseAuth
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val referralData = handleIntent(intent)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val database = DaveDatabase.getDatabase(this)
        val chatDao = database.chatDao()

        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(
                okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                },
            )
            .build()

        val claudeRetrofit = Retrofit.Builder()
            .baseUrl(ClaudeApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val claudeService = claudeRetrofit.create(ClaudeApiService::class.java)

        val openaiRetrofit = Retrofit.Builder()
            .baseUrl(OpenAiApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val openaiService = openaiRetrofit.create(OpenAiApiService::class.java)

        val lumaRetrofit = Retrofit.Builder()
            .baseUrl(LumaApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val lumaService = lumaRetrofit.create(LumaApiService::class.java)

        val sunoRetrofit = Retrofit.Builder()
            .baseUrl(SunoApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val sunoService = sunoRetrofit.create(SunoApiService::class.java)

        val mapsRetrofit = Retrofit.Builder()
            .baseUrl(GoogleMapsApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val mapsService = mapsRetrofit.create(GoogleMapsApiService::class.java)

        val hardwareAccelerator = com.example.daveai.util.HardwareAccelerator(this)
        val chatRepository = ChatRepository(
            apiService = claudeService,
            openaiService = openaiService,
            lumaService = lumaService,
            sunoService = sunoService,
            mapsService = mapsService,
            chatDao = chatDao,
            hardwareAccelerator = hardwareAccelerator
        )

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
        if (campaign != null) referral["campaign"] = campaign
        if (source != null) referral["source"] = source
        if (medium != null) referral["medium"] = medium
        
        if (referral.isNotEmpty()) {
            android.util.Log.d("DaveAI", "Launched with referral: $campaign / $source")
        }
        return referral
    }
}

@Composable
fun DaveApp(
    chatRepository: ChatRepository,
    referralData: Map<String, String?>,
) {
    val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
    val startRoute = if (auth?.currentUser == null) DaveRoute.Auth else DaveRoute.Chat
    
    val authViewModel: AuthViewModel = viewModel()
    LaunchedEffect(referralData) {
        authViewModel.setReferralData(referralData)
    }
    
    val chatViewModel: ChatViewModel = viewModel {
        ChatViewModel(chatRepository)
    }
    val backStack = rememberNavBackStack(startRoute)
    val currentRoute = (backStack.lastOrNull() as? DaveRoute) ?: startRoute

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = { key ->
                when (key) {
                    is DaveRoute.Auth -> {
                        NavEntry(key) {
                            AuthScreen(
                                viewModel = authViewModel,
                            ) {
                                backStack.clear()
                                backStack.add(DaveRoute.Chat)
                            }
                        }
                    }
                    is DaveRoute.Chat -> {
                        NavEntry(key) {
                            ChatScreen(
                                viewModel = chatViewModel,
                            ) {
                                auth?.signOut()
                                backStack.clear()
                                backStack.add(DaveRoute.Auth)
                            }
                        }
                    }
                    else -> NavEntry(key) { Text("Unknown Route") }
                }
            }
        )

        // Floating Dock
        if (currentRoute !is DaveRoute.Auth) {
            FloatingDock(
                currentRoute = currentRoute,
                onRouteSelected = { route ->
                    if (currentRoute::class != route::class) {
                        backStack.clear()
                        backStack.add(route)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun FloatingDock(
    currentRoute: DaveRoute,
    onRouteSelected: (DaveRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        tonalElevation = 12.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                selected = currentRoute is DaveRoute.Chat,
                onClick = { onRouteSelected(DaveRoute.Chat) },
                icon = Icons.AutoMirrored.Rounded.Chat,
                label = "Chat"
            )
        }
    }
}

@Composable
fun DockItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "dockScale"
    )
    
    val size by animateDpAsState(
        targetValue = if (selected) 52.dp else 44.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "itemSize"
    )
    
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .scale(scale)
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
