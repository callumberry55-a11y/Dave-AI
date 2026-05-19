package com.example.daveai.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.util.Log

class DeviceAssistant(private val context: Context) {

    fun getContext() = context

    fun openApp(appName: String): Boolean {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)
        
        // Extract potential target from the input (e.g., "open spotify", "launch maps")
        val cleanedAppName = appName.lowercase().removePrefix("open ").removePrefix("launch ").trim()
        
        // Exact match first
        var targetActivity = resolvedInfos.find { 
            it.loadLabel(pm).toString().equals(cleanedAppName, ignoreCase = true) 
        }
        
        // Partial match if no exact match
        if (targetActivity == null) {
            targetActivity = resolvedInfos.find { 
                it.loadLabel(pm).toString().contains(cleanedAppName, ignoreCase = true) ||
                it.activityInfo.packageName.contains(cleanedAppName, ignoreCase = true)
            }
        }

        // Handle common variations
        if (targetActivity == null) {
            val variations = when (cleanedAppName) {
                "camera" -> listOf("camera", "cam")
                "messages" -> listOf("messages", "messaging", "sms")
                "phone" -> listOf("phone", "dialer", "call")
                "browser" -> listOf("chrome", "browser", "internet", "firefox", "edge")
                "gallery" -> listOf("photos", "gallery")
                "settings" -> listOf("settings", "config")
                "maps" -> listOf("maps", "navigation", "waze")
                "music" -> listOf("music", "spotify", "youtube music", "apple music")
                "mail" -> listOf("mail", "gmail", "outlook")
                else -> emptyList()
            }
            
            for (variant in variations) {
                targetActivity = resolvedInfos.find { 
                    it.loadLabel(pm).toString().contains(variant, ignoreCase = true) 
                }
                if (targetActivity != null) break
            }
        }

        return if (targetActivity != null) {
            val packageName = targetActivity.activityInfo.packageName
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } else {
            false
        }
    }
    
    fun getInstalledAppNames(): List<String> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        return pm.queryIntentActivities(mainIntent, 0)
            .asSequence()
            .map { it.loadLabel(pm).toString() }
            .distinct()
            .sorted()
            .toList()
    }

    fun getBatteryLevel(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        return batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    }

    fun toggleFlashlight(on: Boolean): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, on)
            true
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Flashlight error", e)
            false
        }
    }

    fun getConnectivityStatus(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = cm.activeNetwork ?: return "offline"
        val capabilities = cm.getNetworkCapabilities(network) ?: return "offline"
        
        return when {
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "connected to WiFi"
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "on mobile data"
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> "on ethernet"
            else -> "online"
        }
    }
}
