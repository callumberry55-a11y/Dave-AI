package com.example.daveai

import android.app.Application
import com.example.daveai.data.db.DaveDatabase
import com.example.daveai.data.network.ClaudeApiService
import com.example.daveai.data.network.CloudModelApiService
import com.example.daveai.data.network.CryptoApiService
import com.example.daveai.data.network.ElevenLabsApiService
import com.example.daveai.data.network.GeminiApiService
import com.example.daveai.data.network.GoogleMapsApiService
import com.example.daveai.data.network.GroqApiService
import com.example.daveai.data.network.MediaWikiApiService
import com.example.daveai.data.network.NewsApiService
import com.example.daveai.data.network.OpenAiApiService
import com.example.daveai.data.network.OpenMeteoGeocodingApiService
import com.example.daveai.data.network.PerplexityApiService
import com.example.daveai.data.network.PoetryApiService
import com.example.daveai.data.network.PoetryDbApiService
import com.example.daveai.data.network.SpotifyApiService
import com.example.daveai.data.network.SunoApiService
import com.example.daveai.data.network.WeatherApiService
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

    lateinit var settingsRepository: com.example.daveai.data.repository.SettingsRepository
        private set

    lateinit var securityRepository: com.example.daveai.data.repository.SecurityRepository
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
        val semanticMemoryDao = database.semanticMemoryDao()
        val securityEventDao = database.securityEventDao()
        securityRepository = com.example.daveai.data.repository.SecurityRepository(this, securityEventDao)

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

        val cryptoRetrofit = Retrofit.Builder()
            .baseUrl(CryptoApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val cryptoService = cryptoRetrofit.create(CryptoApiService::class.java)

        val weatherRetrofit = Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val weatherService = weatherRetrofit.create(WeatherApiService::class.java)

        val openMeteoGeocodingRetrofit = Retrofit.Builder()
            .baseUrl(OpenMeteoGeocodingApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val openMeteoGeocodingService = openMeteoGeocodingRetrofit.create(OpenMeteoGeocodingApiService::class.java)

        val spotifyRetrofit = Retrofit.Builder()
            .baseUrl(SpotifyApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val spotifyService = spotifyRetrofit.create(SpotifyApiService::class.java)

        val newsRetrofit = Retrofit.Builder()
            .baseUrl(NewsApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val newsService = newsRetrofit.create(NewsApiService::class.java)

        val poetryRetrofit = Retrofit.Builder()
            .baseUrl(PoetryApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val poetryService = poetryRetrofit.create(PoetryApiService::class.java)

        val cloudModelRetrofit = Retrofit.Builder()
            .baseUrl(CloudModelApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val cloudModelService = cloudModelRetrofit.create(CloudModelApiService::class.java)

        val geminiRetrofit = Retrofit.Builder()
            .baseUrl(GeminiApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val geminiService = geminiRetrofit.create(GeminiApiService::class.java)

        val poetryDbRetrofit = Retrofit.Builder()
            .baseUrl(PoetryDbApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val poetryDbService = poetryDbRetrofit.create(PoetryDbApiService::class.java)

        val wikiRetrofit = Retrofit.Builder()
            .baseUrl(MediaWikiApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val wikiService = wikiRetrofit.create(MediaWikiApiService::class.java)

        val elevenLabsRetrofit = Retrofit.Builder()
            .baseUrl(ElevenLabsApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val elevenLabsService = elevenLabsRetrofit.create(ElevenLabsApiService::class.java)

        val groqRetrofit = Retrofit.Builder()
            .baseUrl(GroqApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val groqService = groqRetrofit.create(GroqApiService::class.java)

        val perplexityRetrofit = Retrofit.Builder()
            .baseUrl(PerplexityApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val perplexityService = perplexityRetrofit.create(PerplexityApiService::class.java)

        settingsRepository = com.example.daveai.data.repository.SettingsRepository(this, securityRepository)
        val deviceAssistant = DeviceAssistant(this)
        val hardwareAccelerator = HardwareAccelerator(this)
        voiceManager = DaveVoiceManager(this, openaiService, elevenLabsService, settingsRepository)
        notificationManager = DaveNotificationManager(this)
        riddleSoundManager = RiddleSoundManager(this)
        
        chatRepository = ChatRepository(
            apiService = claudeService,
            openaiService = openaiService,
            groqService = groqService,
            perplexityService = perplexityService,
            sunoService = sunoService,
            spotifyService = spotifyService,
            newsService = newsService,
            poetryService = poetryService,
            poetryDbService = poetryDbService,
            wikiService = wikiService,
            geminiService = geminiService,
            cloudModelService = cloudModelService,
            mapsService = mapsService,
            cryptoService = cryptoService,
            weatherService = weatherService,
            openMeteoGeocodingService = openMeteoGeocodingService,
            chatDao = chatDao,
            riddleDao = riddleDao,
            semanticMemoryDao = semanticMemoryDao,
            relationshipDao = database.relationshipDao(),
            notificationDao = database.notificationDao(),
            hardwareAccelerator = hardwareAccelerator,
            deviceAssistant = deviceAssistant,
            voiceManager = voiceManager,
            notificationManager = notificationManager,
            settingsRepository = settingsRepository
        )

        chatRepository.scheduleAgenticCycle()

        // Seed the riddles on startup
        kotlinx.coroutines.MainScope().launch {
            chatRepository.seedRiddlesIfEmpty()
        }
    }
}
