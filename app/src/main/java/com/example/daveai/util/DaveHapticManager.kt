package com.example.daveai.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Manages haptic feedback for Dave, leveraging Android 16+ richer haptic APIs.
 */
class DaveHapticManager(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * Dave's "Thinking" haptic signature.
     * A subtle, rhythmic pulse to indicate processing.
     */
    fun pulseThinking() {
        if (Build.VERSION.SDK_INT >= 36) { // Android 16+ (Washi)
            // Use new composition APIs for richer feedback
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.3f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.5f, 100)
                .compose()
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        }
    }

    /**
     * Dave's "Action Successful" haptic signature.
     * A sharp, satisfying double click.
     */
    fun signalSuccess() {
        if (Build.VERSION.SDK_INT >= 36) {
             val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.7f, 50)
                .compose()
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    }

    /**
     * Dave's "Error" haptic signature.
     * A jagged, uncomfortable vibration.
     */
    fun signalError() {
        if (Build.VERSION.SDK_INT >= 36) {
            // Using frequency/amplitude curves if available in newer APIs (conceptual for API 36/37)
            val effect = VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 50), intArrayOf(0, 255, 0, 255), -1)
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        }
    }

    /**
     * Synthesizes haptic patterns based on Dave's current mood.
     */
    fun signalMood(mood: String) {
        if (Build.VERSION.SDK_INT >= 36) {
            val composition = VibrationEffect.startComposition()
            when (mood.uppercase()) {
                "HACKER", "URGENT" -> {
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 1.0f)
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f, 50)
                }
                "EMPATHETIC", "CALM" -> {
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 0.4f)
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.3f, 200)
                }
                "HYPED" -> {
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f)
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f, 50)
                    composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 50)
                }
                else -> composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f)
            }
            vibrator.vibrate(composition.compose())
        } else {
            val effect = when (mood.uppercase()) {
                "URGENT" -> VibrationEffect.EFFECT_DOUBLE_CLICK
                "CALM" -> VibrationEffect.EFFECT_TICK
                else -> VibrationEffect.EFFECT_CLICK
            }
            vibrator.vibrate(VibrationEffect.createPredefined(effect))
        }
    }
}
