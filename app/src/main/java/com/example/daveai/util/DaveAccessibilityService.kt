package com.example.daveai.util

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class DaveAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: We only use this for global actions
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    companion object {
        private var instance: DaveAccessibilityService? = null

        fun isEnabled(): Boolean = instance != null

        fun performGlobalAction(action: Int): Boolean {
            return instance?.performGlobalAction(action) ?: false
        }

        fun takeLiveScreenshot(callback: (Bitmap?) -> Unit) {
            if (instance == null) {
                callback(null)
                return
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                instance?.takeScreenshot(
                    android.view.Display.DEFAULT_DISPLAY,
                    instance!!.mainExecutor,
                    object : AccessibilityService.TakeScreenshotCallback {
                        override fun onSuccess(screenshot: AccessibilityService.ScreenshotResult) {
                            val bitmap = Bitmap.wrapHardwareBuffer(screenshot.hardwareBuffer, screenshot.colorSpace)
                            callback(bitmap)
                        }

                        override fun onFailure(errorCode: Int) {
                            Log.e("DaveAccessibility", "Screenshot failed: $errorCode")
                            callback(null)
                        }
                    }
                )
            } else {
                callback(null)
            }
        }
    }
}
