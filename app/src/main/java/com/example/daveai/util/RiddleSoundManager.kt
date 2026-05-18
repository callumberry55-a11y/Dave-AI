package com.example.daveai.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

class RiddleSoundManager(context: Context) {

    // ToneGenerator(Stream Type, Volume)
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private val handler = Handler(Looper.getMainLooper())

    fun playCorrect() {
        // High pitched double-beep for success
        toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 200)
    }

    fun playWrong() {
        // Low pitched "buh-uh" for wrong answer
        toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 300)
    }

    fun playTierUnlock() {
        // A sequence of ascending tones
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 150)
        handler.postDelayed({
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, 150)
        }, 200)
        handler.postDelayed({
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_9, 150)
        }, 400)
    }

    fun release() {
        toneGenerator.release()
    }
}
