package com.example.daveai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daveai.ui.riddle.RiddleScreen
import com.example.daveai.ui.riddle.RiddleViewModel
import com.example.daveai.ui.theme.DaveAITheme

class RiddleVaultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as DaveApplication
        val chatRepository = app.chatRepository
        val voiceManager = app.voiceManager
        val riddleSoundManager = app.riddleSoundManager
        val riddleDao = chatRepository.getRiddleDao()

        setContent {
            DaveAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val viewModel: RiddleViewModel = viewModel {
                        RiddleViewModel(riddleDao, voiceManager, riddleSoundManager, chatRepository)
                    }
                    RiddleScreen(
                        viewModel = viewModel,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
