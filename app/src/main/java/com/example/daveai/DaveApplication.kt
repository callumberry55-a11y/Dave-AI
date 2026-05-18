package com.example.daveai

import android.app.Application
import com.example.daveai.data.db.DaveDatabase
import com.example.daveai.data.network.ClaudeApiService
import com.example.daveai.data.network.GoogleMapsApiService
import com.example.daveai.data.network.OpenAiApiService
import com.example.daveai.data.network.SunoApiService
import com.example.daveai.data.repository.ChatRepository
import com.example.daveai.util.DaveNotificationManager
import com.example.daveai.util.DaveVoiceManager
import com.example.daveai.util.DeviceAssistant
import com.example.daveai.util.HardwareAccelerator
import com.example.daveai.util.RiddleSoundManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class DaveApplication : Application() {

    lateinit var chatRepository: ChatRepository
        private set

    lateinit var notificationManager: DaveNotificationManager
        private set

    lateinit var voiceManager: DaveVoiceManager
        private set

    lateinit var riddleSoundManager: RiddleSoundManager
        private set

    override fun onCreate() {
        super.onCreate()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val database = DaveDatabase.getDatabase(this)
        val chatDao = database.chatDao()
        val riddleDao = database.riddleDao()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                },
            )
            .build()

        val claudeRetrofit = Retrofit.Builder()
            .baseUrl(ClaudeApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val claudeService = claudeRetrofit.create(ClaudeApiService::class.java)

        val openaiRetrofit = Retrofit.Builder()
            .baseUrl(OpenAiApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val openaiService = openaiRetrofit.create(OpenAiApiService::class.java)

        val sunoRetrofit = Retrofit.Builder()
            .baseUrl(SunoApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val sunoService = sunoRetrofit.create(SunoApiService::class.java)

        val mapsRetrofit = Retrofit.Builder()
            .baseUrl(GoogleMapsApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val mapsService = mapsRetrofit.create(GoogleMapsApiService::class.java)

        val deviceAssistant = DeviceAssistant(this)
        val hardwareAccelerator = HardwareAccelerator(this)
        voiceManager = DaveVoiceManager(this, openaiService)
        notificationManager = DaveNotificationManager(this)
        riddleSoundManager = RiddleSoundManager(this)
        
        chatRepository = ChatRepository(
            apiService = claudeService,
            openaiService = openaiService,
            sunoService = sunoService,
            mapsService = mapsService,
            chatDao = chatDao,
            riddleDao = riddleDao,
            hardwareAccelerator = hardwareAccelerator,
            deviceAssistant = deviceAssistant,
            voiceManager = voiceManager,
            notificationManager = notificationManager,
        )

        // Seed the riddles on startup
        kotlinx.coroutines.MainScope().launch {
            chatRepository.seedRiddlesIfEmpty()
        }
    }
}
