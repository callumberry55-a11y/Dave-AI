package com.example.daveai.data.network

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class SharedIntelligenceRequest(
    val query: String,
    val context: Map<String, String>? = null,
    val version: String = "V13.1.1"
)

@JsonClass(generateAdapter = true)
data class SharedIntelligenceResponse(
    val answer: String,
    val source: String? = null,
    val confidence: Float = 1.0f
)

@JsonClass(generateAdapter = true)
data class PairingRequestResponse(
    val pairingCode: String,
    val expiresAt: Long
)

@JsonClass(generateAdapter = true)
data class PairingLinkRequest(
    val pairingCode: String,
    val deviceName: String
)

@JsonClass(generateAdapter = true)
data class PairingLinkResponse(
    val success: Boolean,
    val partnerId: String?,
    val partnerName: String?,
    val message: String?
)

@JsonClass(generateAdapter = true)
data class SyncPushRequest(
    val partnerId: String,
    val memories: List<SyncMemoryItem>
)

@JsonClass(generateAdapter = true)
data class SyncMemoryItem(
    val type: String,
    val content: String,
    val importance: Int,
    val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class SyncPullResponse(
    val memories: List<SyncMemoryItem>
)

interface CloudModelApiService {
    companion object {
        const val PREFERRED_NETWORK_URL = "https://app-measurement.com/redirect?url=https://play.google.com/store/apps/details?id=com.example.daveai&gmpaid=1:895901729935:android:103a1cba33bfaee96b9cfa&adid={GAID}&anid=aura&aclid={DynamicParameter}&cs=Aura"
        const val BASE_URL = "https://www.daveai.net/api/v1/"
    }

    @POST("intelligence/shared")
    suspend fun querySharedIntelligence(
        @Body request: SharedIntelligenceRequest
    ): SharedIntelligenceResponse

    @POST("pairing/request")
    suspend fun requestPairingCode(): PairingRequestResponse

    @POST("pairing/link")
    suspend fun linkPartner(
        @Body request: PairingLinkRequest
    ): PairingLinkResponse

    @POST("sync/push")
    suspend fun pushIntelligence(
        @Body request: SyncPushRequest
    ): Map<String, Any>

    @GET("sync/pull")
    suspend fun pullIntelligence(
        @Query("partnerId") partnerId: String,
        @Query("since") since: Long
    ): SyncPullResponse
}
