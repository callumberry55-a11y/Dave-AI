package com.example.daveai.util

import android.app.NotificationManager
import android.app.WallpaperManager
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import java.io.File
import java.util.Calendar

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
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        Log.d("DeviceAssistant", "Battery level requested: $level")
        return level
    }

    fun toggleFlashlight(on: Boolean): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var flashCameraId: String? = null
            
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                
                if (hasFlash && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                    flashCameraId = id
                    break
                }
            }
            
            if (flashCameraId != null) {
                cameraManager.setTorchMode(flashCameraId, on)
                true
            } else {
                false
            }
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

    fun controlMedia(action: String): Boolean {
        if (action == "play_music_app") {
            val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return try {
                context.startActivity(intent)
                true
            } catch (_: Exception) {
                openApp("music")
            }
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val keyCode = when (action) {
            "next" -> KeyEvent.KEYCODE_MEDIA_NEXT
            "previous" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            "play_pause" -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            "play" -> KeyEvent.KEYCODE_MEDIA_PLAY
            "pause" -> KeyEvent.KEYCODE_MEDIA_PAUSE
            else -> return false
        }
        
        val eventTime = android.os.SystemClock.uptimeMillis()
        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        audioManager.dispatchMediaKeyEvent(downEvent)
        
        val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)
        audioManager.dispatchMediaKeyEvent(upEvent)
        return true
    }

    fun openWifiSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun toggleWifi(enable: Boolean): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = enable
                true
            } else {
                // Direct toggle not possible on Q+, must open panel
                openWifiSettings()
                false
            }
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Wifi toggle failed", e)
            false
        }
    }

    fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun toggleBluetooth(enable: Boolean): Boolean {
        return try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter
            if (enable) {
                adapter.enable()
            } else {
                adapter.disable()
            }
            true
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Bluetooth toggle failed", e)
            // Fallback
            openBluetoothSettings()
            false
        }
    }

    fun openDataSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun setRingerMode(mode: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        }
        
        audioManager.ringerMode = mode
    }

    fun setBrightness(level: Int) {
        if (Settings.System.canWrite(context)) {
            val constrainedLevel = level.coerceIn(0, 255)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, constrainedLevel)
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun getSystemUptime(): String {
        val uptimeMillis = android.os.SystemClock.elapsedRealtime()
        val seconds = (uptimeMillis / 1000) % 60
        val minutes = (uptimeMillis / (1000 * 60)) % 60
        val hours = (uptimeMillis / (1000 * 60 * 60))
        return String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun listInstalledApps(): List<String> {
        val pm = context.packageManager
        return pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
            .asSequence()
            .map { it.loadLabel(pm).toString() }
            .distinct()
            .sorted()
            .toList()
    }

    fun getDetailedAppInfo(appName: String): String {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)
        val target = resolvedInfos.find { 
            it.loadLabel(pm).toString().contains(appName, ignoreCase = true) 
        }

        return if (target != null) {
            val packageName = target.activityInfo.packageName
            val info = pm.getPackageInfo(packageName, 0)
            buildString {
                append("Package: $packageName\n")
                append("Version: ${info.versionName ?: "Unknown"}\n")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    append("Build ID: ${info.longVersionCode}\n")
                }
                append("Installed: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(info.firstInstallTime))}")
            }
        } else {
            "App signals for '$appName' not found in the mainframe."
        }
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        return DaveAccessibilityService.isEnabled()
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun performSystemAction(action: Int): Boolean {
        return DaveAccessibilityService.performGlobalAction(action)
    }

    fun takeLiveScreenshot(callback: (Bitmap?) -> Unit) {
        DaveAccessibilityService.takeLiveScreenshot(callback)
    }

    fun searchContacts(name: String): List<ContactIntel> {
        val contacts = mutableListOf<ContactIntel>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        )
        
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$name%")
        
        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val displayName = cursor.getString(0) ?: "Unknown"
                    val number = cursor.getString(1) ?: "No number"
                    val contactId = cursor.getLong(2)
                    
                    // Fetch address if available
                    val address = fetchContactAddress(contactId)
                    
                    contacts.add(ContactIntel(displayName, number, address))
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Contact search failed", e)
        }
        return contacts.distinctBy { it.name }.take(5)
    }

    private fun fetchContactAddress(contactId: Long): String? {
        val uri = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
        val selection = "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId.toString())
        
        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (_: Exception) { null }
    }

    fun readClipboard(): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        return if (clipboard.hasPrimaryClip()) {
            clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        } else null
    }

    fun writeToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Dave AI", text)
        clipboard.setPrimaryClip(clip)
    }

    fun scanNearbyPulse(category: String): String {
        // This is a semantic signal to the Repository to search for 'high-vibe' categories
        return "nearby $category"
    }

    fun getUpcomingEvents(): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION
        )
        
        val startMillis = System.currentTimeMillis()
        val endMillis = startMillis + 24 * 60 * 60 * 1000 // Next 24 hours
        
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())
        
        try {
            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    events.add(CalendarEvent(
                        title = cursor.getString(0) ?: "Unknown",
                        start = cursor.getLong(1),
                        end = cursor.getLong(2),
                        location = cursor.getString(3)
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Calendar query failed", e)
        }
        return events.take(5)
    }

    fun getTopUsedApps(): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000 // Last 24 hours
        
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        if (stats.isNullOrEmpty()) return emptyMap()
        
        return stats.asSequence()
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(5)
            .associate { it.packageName to it.totalTimeInForeground }
    }

    fun setSystemWallpaper(color: Int): Boolean {
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                this.color = color
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, 1080f, 1920f, paint)
            wallpaperManager.setBitmap(bitmap)
            true
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Failed to set wallpaper", e)
            false
        }
    }

    fun searchLocalFiles(query: String): List<LocalFile> {
        val files = mutableListOf<LocalFile>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA
        )

        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")

        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(0) ?: "Unknown"
                    val size = cursor.getLong(1)
                    val mime = cursor.getString(2) ?: "application/octet-stream"
                    val path = cursor.getString(3) ?: ""
                    
                    // Filter for documents and code
                    if (isDocumentOrCode(name, mime)) {
                        files.add(LocalFile(name, size, mime, path))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "File search failed", e)
        }
        return files.take(5)
    }

    private fun isDocumentOrCode(name: String, mime: String): Boolean {
        val ext = name.substringAfterLast(".", "").lowercase()
        return mime.contains("pdf") || mime.contains("text") || 
               listOf("kt", "java", "py", "js", "html", "json", "log").contains(ext)
    }

    fun moveFile(sourcePath: String, destPath: String): Boolean {
        return try {
            val source = File(sourcePath)
            val dest = File(destPath)
            if (source.exists()) {
                source.renameTo(dest)
            } else false
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Move file failed", e)
            false
        }
    }

    fun renameFile(path: String, newName: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                val newFile = File(file.parent, newName)
                file.renameTo(newFile)
            } else false
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Rename file failed", e)
            false
        }
    }

    fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else false
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Delete file failed", e)
            false
        }
    }

    fun setVolume(percent: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxMedia = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetMedia = (maxMedia * percent / 100).coerceIn(0, maxMedia)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetMedia, AudioManager.FLAG_SHOW_UI)
        
        val maxRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        val targetRing = (maxRing * percent / 100).coerceIn(0, maxRing)
        audioManager.setStreamVolume(AudioManager.STREAM_RING, targetRing, 0)
    }

    fun toggleDND(on: Boolean): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return false
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(if (on) {
                NotificationManager.INTERRUPTION_FILTER_NONE
            } else {
                NotificationManager.INTERRUPTION_FILTER_ALL
            })
            return true
        }
        return false
    }

    fun setQuickAlarm(minutesFromNow: Int, label: String = "Dave AI Alarm"): Boolean {
        return try {
            val now = Calendar.getInstance()
            now.add(Calendar.MINUTE, minutesFromNow)
            
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, now.get(Calendar.HOUR_OF_DAY))
                putExtra(AlarmClock.EXTRA_MINUTES, now.get(Calendar.MINUTE))
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("DeviceAssistant", "Alarm failed", e)
            false
        }
    }
}

data class LocalFile(
    val name: String,
    val size: Long,
    val mimeType: String,
    val path: String
)

data class CalendarEvent(
    val title: String,
    val start: Long,
    val end: Long,
    val location: String?
)

data class ContactIntel(
    val name: String,
    val phone: String,
    val address: String?
)
