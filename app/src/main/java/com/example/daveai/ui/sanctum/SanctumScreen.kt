package com.example.daveai.ui.sanctum

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralTopBar
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctumScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    var neuralRam by remember { mutableFloatStateOf(0.4f) }
    var coreTemp by remember { mutableIntStateOf(42) }
    var vaultSync by remember { mutableFloatStateOf(0.98f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            neuralRam = (neuralRam + Random.nextFloat() * 0.1f - 0.05f).coerceIn(0.1f, 0.9f)
            coreTemp = (coreTemp + Random.nextInt(3) - 1).coerceIn(35, 55)
            vaultSync = (vaultSync + 0.001f).coerceIn(0.9f, 1.0f)
        }
    }

    Scaffold(
        topBar = {
            NeuralTopBar(
                title = "THE SANCTUM",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                isProactive = true
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NEURAL SERVER CORE",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(32.dp))

            MetricCard(
                icon = Icons.Rounded.Memory,
                label = "NEURAL RAM USAGE",
                value = "${(neuralRam * 100).toInt()}%",
                progress = neuralRam,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            MetricCard(
                icon = Icons.Rounded.Speed,
                label = "TPU CORE TEMP",
                value = "$coreTemp°C",
                progress = (coreTemp - 30) / 30f,
                color = if (coreTemp > 50) Color.Red else MaterialTheme.colorScheme.tertiary
            )

            Spacer(Modifier.height(16.dp))

            MetricCard(
                icon = Icons.Rounded.Storage,
                label = "VAULT SYNC",
                value = "${(vaultSync * 100).toInt()}%",
                progress = vaultSync,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.weight(1f))

            NeuralCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "SERVER LOGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "OS_INIT :: SUCCESS\n" +
                        "ENCRYPTION_LAYER :: ACTIVE\n" +
                        "NEURAL_LINK :: STABLE\n" +
                        "AURA_EMITTER :: MODULATING",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 16.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    NeuralCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = color,
                    trackColor = color.copy(alpha = 0.1f)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }
}
