import java.util.Properties

val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { props.load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.daveai"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.daveai"
        minSdk = 36
        targetSdk = 37
        versionCode = 15
        versionName = "BP45.2026.15"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey = localProps.getProperty("CLAUDE_API_KEY") ?: ""
        buildConfigField("String", "CLAUDE_API_KEY", "\"$apiKey\"")

        val mapsKey = localProps.getProperty("MAPS_API_KEY") ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")

        val googleClientId = localProps.getProperty("GOOGLE_CLIENT_ID") ?: ""
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")

        val openaiKey = localProps.getProperty("OPENAI_API_KEY") ?: ""
        buildConfigField("String", "OPENAI_API_KEY", "\"$openaiKey\"")

        val sunoKey = localProps.getProperty("SUNO_API_KEY") ?: ""
        buildConfigField("String", "SUNO_API_KEY", "\"$sunoKey\"")

        val spotifyId = localProps.getProperty("SPOTIFY_CLIENT_ID") ?: ""
        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"$spotifyId\"")

        val spotifySecret = localProps.getProperty("SPOTIFY_CLIENT_SECRET") ?: ""
        buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"$spotifySecret\"")

        val newsKey = localProps.getProperty("NEWS_API_KEY") ?: ""
        buildConfigField("String", "NEWS_API_KEY", "\"$newsKey\"")

        val elevenKey = localProps.getProperty("ELEVENLABS_API_KEY") ?: ""
        buildConfigField("String", "ELEVENLABS_API_KEY", "\"$elevenKey\"")

        val firestoreKey = localProps.getProperty("FIRESTORE_API_KEY") ?: "AIzaSyAUuKyeoKa2V694n9SA04Rln6kw-IY9PNI"
        buildConfigField("String", "FIRESTORE_API_KEY", "\"$firestoreKey\"")

        buildConfigField("String", "INTELLIGENCE_VERSION", "\"V1.0.0\"")
        manifestPlaceholders["intelligenceAuthority"] = "com.example.daveai.intelligence"
        manifestPlaceholders["appPackageName"] = "com.example.daveai"
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.coil.compose)
    implementation(libs.converter.moshi)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.logging.interceptor)
    implementation(libs.material)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.biometric)
    implementation(libs.googleid)
    implementation(libs.maps.compose)
    implementation(libs.retrofit)
    
    // Video Player support
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    
    // Google AI Edge SDK for on-device TPU acceleration (Gemini Nano)
    implementation(libs.generativeai)
    
    // ML Kit GenAI (AICore) for deep Android system integration
    implementation(libs.genai.prompt)
    
    // WorkManager for background tasks (Lessons check-ins)
    implementation(libs.work.runtime.ktx)

    // Widgets support (Glance)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // Security & Encryption
    implementation(libs.sqlcipher.android)
    implementation(libs.security.crypto)
    // Removed sqlite-ktx as it's not strictly necessary for basic SQLCipher integration 
    // unless we need specific KTX features for SupportSQLiteDatabase

    testImplementation(libs.androidx.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)
}
