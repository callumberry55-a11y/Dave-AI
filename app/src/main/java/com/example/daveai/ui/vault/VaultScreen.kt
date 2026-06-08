package com.example.daveai.ui.vault

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.components.CountdownTimer
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralTextField
import com.example.daveai.ui.components.NeuralTopBar
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    onNavigateToSecurity: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val releaseDate = remember {
        Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 28, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    var isUnlocked by remember { mutableStateOf(System.currentTimeMillis() >= releaseDate) }

    Scaffold(
        topBar = {
            NeuralTopBar(
                title = "DIGITAL VAULT",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                isProactive = false,
                actions = {
                    IconButton(onClick = onNavigateToSecurity) {
                        Icon(
                            Icons.Rounded.Security,
                            contentDescription = "Security Setup",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        AnimatedContent(
            targetState = isUnlocked,
            transitionSpec = {
                (fadeIn(animationSpec = tween(1000)) + scaleIn(initialScale = 0.95f, animationSpec = tween(1000)))
                    .togetherWith(fadeOut(animationSpec = tween(500)))
            },
            label = "vault_unlock",
            modifier = Modifier.padding(padding)
        ) { unlocked ->
            if (unlocked) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "API KEY MANAGEMENT",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    
                    Text(
                        "Override system keys with your own for full control.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    KeyEntrySection(
                        icon = Icons.Rounded.Key,
                        label = "Claude API Key",
                        value = uiState.userClaudeApiKey ?: "",
                        onValueChange = { viewModel.updateClaudeApiKey(it) },
                        placeholder = "sk-ant-api03-..."
                    )

                    Spacer(Modifier.height(16.dp))

                    KeyEntrySection(
                        icon = Icons.Rounded.Key,
                        label = "OpenAI API Key",
                        value = uiState.userOpenAiApiKey ?: "",
                        onValueChange = { viewModel.updateOpenAiApiKey(it) },
                        placeholder = "sk-..."
                    )

                    Spacer(Modifier.height(16.dp))

                    KeyEntrySection(
                        icon = Icons.Rounded.Public,
                        label = "Google Maps / Gemini Key",
                        value = uiState.userMapsApiKey ?: "",
                        onValueChange = { viewModel.updateMapsApiKey(it) },
                        placeholder = "AIza..."
                    )

                    Spacer(Modifier.height(16.dp))

                    KeyEntrySection(
                        icon = Icons.Rounded.Newspaper,
                        label = "News API Key",
                        value = uiState.userNewsApiKey ?: "",
                        onValueChange = { viewModel.updateNewsApiKey(it) },
                        placeholder = "..."
                    )

                    Spacer(Modifier.height(16.dp))

                    KeyEntrySection(
                        icon = Icons.Rounded.Speed,
                        label = "Groq API Key",
                        value = uiState.userGroqApiKey ?: "",
                        onValueChange = { viewModel.updateGroqApiKey(it) },
                        placeholder = "gsk_..."
                    )

                    Spacer(Modifier.height(16.dp))

                    KeyEntrySection(
                        icon = Icons.Rounded.Terminal,
                        label = "Perplexity API Key",
                        value = uiState.userPerplexityApiKey ?: "",
                        onValueChange = { viewModel.updatePerplexityApiKey(it) },
                        placeholder = "pplx-..."
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "UTILITY & MEDIA",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    KeyEntrySection(
                        icon = Icons.Rounded.AudioFile,
                        label = "ElevenLabs API Key",
                        value = uiState.userElevenLabsApiKey ?: "",
                        onValueChange = { viewModel.updateElevenLabsApiKey(it) },
                        placeholder = "..."
                    )

                    Spacer(Modifier.height(16.dp))

                    KeyEntrySection(
                        icon = Icons.Rounded.CloudQueue,
                        label = "OpenWeather API Key",
                        value = uiState.userWeatherApiKey ?: "",
                        onValueChange = { viewModel.updateWeatherApiKey(it) },
                        placeholder = "..."
                    )

                    Spacer(Modifier.height(16.dp))

                    KeyEntrySection(
                        icon = Icons.Rounded.AccountBalance,
                        label = "Alpha Vantage (Finance)",
                        value = uiState.userFinanceApiKey ?: "",
                        onValueChange = { viewModel.updateFinanceApiKey(it) },
                        placeholder = "..."
                    )

                    Spacer(Modifier.height(16.dp))

                    KeyEntrySection(
                        icon = Icons.Rounded.Key,
                        label = "Spotify Client Secret",
                        value = uiState.userSpotifyClientSecret ?: "",
                        onValueChange = { viewModel.updateSpotifyClientSecret(it) },
                        placeholder = "..."
                    )

                    Spacer(Modifier.height(32.dp))

                    NeuralCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "VAULT SECURITY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Your keys are stored locally on this device. They are never shared with anyone except the intended service providers. Leave a field blank to use Dave's default credentials.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.HourglassEmpty,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    
                    Spacer(Modifier.height(24.dp))

                    Text(
                        "RELEASE PROTOCOL INITIATED",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )

                    Text(
                        "CUSTOM APIs v1.0",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(Modifier.height(48.dp))

                    CountdownTimer(
                        targetTimestamp = releaseDate,
                        onExpire = { isUnlocked = true }
                    )

                    Spacer(Modifier.height(48.dp))

                    NeuralCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "STATUS: ENCRYPTED",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Direct integration with third-party LLMs and services will be available once the neural handshake is complete on June 28th.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyEntrySection(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    var isVisible by remember { mutableStateOf(false) }

    NeuralTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
                Icon(
                    if (isVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = if (isVisible) "Hide" else "Show",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true
    )
}
