package com.example.daveai.ui.multimedia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralTextField
import com.example.daveai.ui.components.NeuralTopBar

@Composable
fun MultimediaHubScreen(
    viewModel: MultimediaViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("MUSIC", "POETRY")

    Scaffold(
        topBar = {
            NeuralTopBar(
                title = "MULTIMEDIA HUB",
                onNavigationClick = onBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                when (selectedTab) {
                    0 -> MusicTab(uiState, viewModel)
                    1 -> PoetryTab(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
fun MusicTab(uiState: MultimediaUiState, viewModel: MultimediaViewModel) {
    Column {
        var query by remember { mutableStateOf("") }
        NeuralTextField(
            value = query,
            onValueChange = { query = it },
            label = "Search Spotify signals...",
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { viewModel.searchMusic(query) }) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null)
                }
            }
        )
        
        Spacer(Modifier.height(16.dp))
        
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.spotifyTracks) { track ->
                NeuralCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        track.albumArt?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(16.dp))
                        }
                        Column {
                            Text(track.name, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(track.artist, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PoetryTab(uiState: MultimediaUiState, viewModel: MultimediaViewModel) {
    Column {
        var prompt by remember { mutableStateOf("") }
        var selectedStyle by remember { mutableStateOf("contemporary") }
        val styles = listOf("contemporary", "haiku", "sonnet", "limerick", "cyberpunk")

        NeuralTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = "Forge new verse...",
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { viewModel.generatePoem(prompt, selectedStyle) }) {
                    Icon(Icons.Rounded.AutoStories, contentDescription = null)
                }
            }
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { style ->
                FilterChip(
                    selected = selectedStyle == style,
                    onClick = { selectedStyle = style },
                    label = { Text(style.uppercase(), fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(
                    "NEURAL ARCHIVES :: CLASSIC SELECTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(uiState.poems) { poem ->
                NeuralCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(poem.content, style = MaterialTheme.typography.bodyLarge)
                        poem.author?.let {
                            Text("— $it", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
    }
}
