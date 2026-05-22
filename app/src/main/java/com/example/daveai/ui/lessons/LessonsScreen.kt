package com.example.daveai.ui.lessons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.chat.BouncyButton
import com.example.daveai.ui.chat.StructuredContent
import com.example.daveai.ui.components.NeuralCard
import com.example.daveai.ui.components.NeuralTextField
import com.example.daveai.ui.components.NeuralTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    viewModel: LessonsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val accentColor = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NeuralTopBar(
                title = "Dave University",
                onNavigationClick = {
                    if (uiState.currentLessonContent != null) {
                        viewModel.closeLesson()
                    } else {
                        onBack()
                    }
                },
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            if (uiState.isLoading && uiState.currentLessonContent == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = accentColor)
                        Spacer(Modifier.height(16.dp))
                        Text("Drafting Curriculum...", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (uiState.currentLessonContent != null) {
                // Active Lesson View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text("CURRENT MODULE", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(Modifier.height(24.dp))
                    StructuredContent(text = uiState.currentLessonContent!!, contentColor = MaterialTheme.colorScheme.onSurface)
                    
                    Spacer(Modifier.height(32.dp))
                    BouncyButton(
                        onClick = { viewModel.closeLesson() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        containerColor = accentColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text("Complete Module", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } else if (uiState.activeSyllabus == null) {
                // Setup View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.School, contentDescription = null, tint = accentColor, modifier = Modifier.size(50.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "What do you want to master today?",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Dave will instantly generate an elite, bite-sized curriculum on any topic.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(32.dp))
                    
                    NeuralTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputChanged,
                        label = "e.g. Quantum Physics, Jetpack Compose...",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (uiState.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(24.dp))

                    BouncyButton(
                        onClick = { viewModel.generateSyllabus(uiState.inputText) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        containerColor = accentColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text("Build Curriculum", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } else {
                // Curriculum View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "SYLLABUS",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        uiState.activeSyllabus!!.topic,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        uiState.activeSyllabus!!.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(32.dp))

                    uiState.activeSyllabus!!.modules.forEachIndexed { index, module ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(initialOffsetY = { 50 * (index + 1) }) + fadeIn()
                        ) {
                            NeuralCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable(enabled = !module.completed) {
                                        viewModel.startLesson(module.id)
                                    },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = if (module.completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (module.completed) MaterialTheme.colorScheme.surfaceVariant else accentColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (module.completed) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4ADE80))
                                        } else {
                                            Text(module.id.toString(), color = accentColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        module.title,
                                        color = if (module.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (!module.completed) {
                                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Start", tint = accentColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
