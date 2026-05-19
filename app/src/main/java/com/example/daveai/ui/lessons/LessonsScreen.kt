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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daveai.ui.chat.BouncyButton
import com.example.daveai.ui.chat.StructuredContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    viewModel: LessonsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF020617)  // Slate 950
        )
    )
    val accentColor = Color(0xFF38BDF8) // Indigo/Blue accent

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(bgGradient),
        topBar = {
            TopAppBar(
                title = { Text("Dave University", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.currentLessonContent != null) {
                                viewModel.closeLesson()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.currentLessonContent == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = accentColor)
                        Spacer(Modifier.height(16.dp))
                        Text("Drafting Curriculum...", color = Color.White, fontWeight = FontWeight.Bold)
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
                    StructuredContent(text = uiState.currentLessonContent!!, contentColor = Color.White)
                    
                    Spacer(Modifier.height(32.dp))
                    BouncyButton(
                        onClick = { viewModel.closeLesson() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        containerColor = accentColor,
                        contentColor = Color.White
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
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Dave will instantly generate an elite, bite-sized curriculum on any topic.",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(32.dp))
                    
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Quantum Physics, Jetpack Compose...", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = accentColor,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
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
                        contentColor = Color.White
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
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 36.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        uiState.activeSyllabus!!.description,
                        color = Color.LightGray,
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(32.dp))

                    uiState.activeSyllabus!!.modules.forEachIndexed { index, module ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(initialOffsetY = { 50 * (index + 1) }) + fadeIn()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable(enabled = !module.completed) {
                                        viewModel.startLesson(module.id)
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = if (module.completed) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (module.completed) Color.Transparent else accentColor.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (module.completed) Color(0xFF334155) else accentColor.copy(alpha = 0.2f)),
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
                                        color = if (module.completed) Color.Gray else Color.White,
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
