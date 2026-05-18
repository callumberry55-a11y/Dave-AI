package com.example.daveai.service

import android.content.Intent
import android.speech.RecognitionService

class DaveRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        // No-op for now, using standard speech recognizer in app
    }

    override fun onCancel(listener: Callback?) {
    }

    override fun onStopListening(listener: Callback?) {
    }
}
