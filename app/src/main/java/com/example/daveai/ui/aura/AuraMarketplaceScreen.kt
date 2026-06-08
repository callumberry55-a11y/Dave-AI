package com.example.daveai.ui.aura

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.chat.ChatViewModel
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralTopBar

data class AuraPreset(
    val name: String,
    val description: String,
    val primaryColor: Color,
    val meshSpeed: Float,
    val glow: Float,
    val blur: Float,
    val persona: String
)

val PRESETS = listOf(
    AuraPreset("CYBERPUNK", "High intensity, neon aesthetics.", Color(0xFF00FF88), 2.0f, 0.8f, 0.7f, "HACKER"),
    AuraPreset("MINIMALIST", "Clean, focused, calm.", Color(0xFFE0E0E0), 0.5f, 0.2f, 0.3f, "ZEN"),
    AuraPreset("COMMANDER", "Strategic, efficient, bold.", Color(0xFFFF3D00), 1.2f, 0.6f, 0.5f, "STRATEGIST"),
    AuraPreset("NEBULA", "Dreamy, fluid, expansive.", Color(0xFFBB86FC), 0.8f, 0.9f, 1.0f, "CREATIVE"),
    AuraPreset("SOLARIS", "Radiant, high-energy intelligence.", Color(0xFFFFD600), 1.5f, 1.0f, 0.4f, "VISIONARY"),
    AuraPreset("DEEP SEA", "Submerged, calm, analytical depth.", Color(0xFF2979FF), 0.6f, 0.4f, 0.8f, "ANALYST"),
    AuraPreset("CRIMSON", "Aggressive, high-performance hacking.", Color(0xFFFF1744), 2.5f, 0.9f, 0.6f, "HACKER"),
    AuraPreset("GLACIER", "Frosty, slow, minimalist precision.", Color(0xFF00E5FF), 0.3f, 0.3f, 1.0f, "ZEN"),
    AuraPreset("SYNTHWAVE", "Retro-future, neon pink aesthetics.", Color(0xFFF50057), 1.2f, 0.8f, 0.5f, "CREATIVE"),
    AuraPreset("VOID", "Absolute focus, dark mode dominance.", Color(0xFF212121), 0.1f, 0.1f, 0.2f, "ANALYST"),
    AuraPreset("FOREST", "Organic, balanced, growth-oriented.", Color(0xFF00C853), 0.8f, 0.5f, 0.4f, "VISIONARY"),
    AuraPreset("AMBER", "Retro terminal vibes, sharp logic.", Color(0xFFFFAB00), 1.0f, 0.7f, 0.0f, "HACKER")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraMarketplaceScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            NeuralTopBar(
                title = "AURA MARKETPLACE",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "ELITE PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
            }
            
            items(PRESETS) { preset ->
                AuraPresetCard(preset) {
                    viewModel.updatePrimaryColor(preset.primaryColor.value.toInt())
                    viewModel.updateAnimationSpeed(preset.meshSpeed)
                    viewModel.updateGlowStrength(preset.glow)
                    viewModel.updateBlurIntensity(preset.blur)
                    viewModel.updateDigitalPersona(preset.persona)
                }
            }
        }
    }
}

@Composable
private fun AuraPresetCard(preset: AuraPreset, onApply: () -> Unit) {
    NeuralCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(preset.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Palette, contentDescription = null, tint = preset.primaryColor)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(preset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(preset.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = preset.primaryColor.copy(alpha = 0.1f),
                    contentColor = preset.primaryColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("APPLY", fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
    }
}
