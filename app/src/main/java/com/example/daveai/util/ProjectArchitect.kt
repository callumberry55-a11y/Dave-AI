package com.example.daveai.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ProjectArchitect(private val context: Context) {

    fun generateProject(appName: String, packageName: String): File {
        val rootDir = File(context.cacheDir, "DaveAppFactory/${appName.replace(" ", "_")}")
        rootDir.mkdirs()

        // Generate MainActivity.kt
        val srcDir = File(rootDir, "app/src/main/java/${packageName.replace(".", "/")}")
        srcDir.mkdirs()
        File(srcDir, "MainActivity.kt").writeText("""
            package $packageName

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.material3.Text

            class MainActivity : ComponentActivity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContent {
                        Text("Hello from Dave-built $appName!")
                    }
                }
            }
        """.trimIndent())

        // Generate AndroidManifest.xml
        val manifestDir = File(rootDir, "app/src/main")
        manifestDir.mkdirs()
        File(manifestDir, "AndroidManifest.xml").writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <application
                    android:allowBackup="true"
                    android:label="$appName"
                    android:supportsRtl="true">
                    <activity
                        android:name=".MainActivity"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent())

        // Zip it up
        val zipFile = File(context.cacheDir, "${appName.replace(" ", "_")}.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            rootDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entryName = file.relativeTo(rootDir).path
                    zos.putNextEntry(ZipEntry(entryName))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
        
        return zipFile
    }

    fun generateBlueprint(appName: String): List<BlueprintItem> {
        return listOf(
            BlueprintItem("Activity", "MainActivity", "Core UI Entry Point"),
            BlueprintItem("Theme", "AppTheme", "Material 3 Adaptive Design"),
            BlueprintItem("Layout", "MainScreen", "Compose-based Fluid UI"),
            BlueprintItem("Dependency", "Compose BOM", "v2026.05.00"),
            BlueprintItem("Dependency", "Material3", "v1.4.0-alpha")
        )
    }
}

data class BlueprintItem(
    val type: String,
    val name: String,
    val description: String
)
