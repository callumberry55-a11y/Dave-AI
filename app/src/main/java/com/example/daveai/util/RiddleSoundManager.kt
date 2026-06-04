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
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 100)
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_3, 100) }, 150)
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, 100) }, 300)
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_A, 250) }, 450)
    }

    fun playWrong() {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_8, 200)
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_B, 300) }, 250)
    }

    fun playTierUnlock() {
        // Grand victory fanfare
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 100)
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_2, 100) }, 120)
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_3, 100) }, 240)
        
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_5, 100) }, 400)
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_6, 100) }, 520)
        
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_9, 300) }, 650)
        handler.postDelayed({ toneGenerator.startTone(ToneGenerator.TONE_DTMF_D, 400) }, 800)
    }

    fun release() {
        toneGenerator.release()
    }
}
