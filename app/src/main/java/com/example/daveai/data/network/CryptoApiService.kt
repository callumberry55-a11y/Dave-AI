package com.example.daveai.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoApiService {
    @GET("api/v3/simple/price")
    suspend fun getPrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "usd",
        @Query("include_24hr_change") include24hrChange: Boolean = true,
    ): Map<String, Map<String, Double>>

    companion object {
        const val BASE_URL = "https://api.coingecko.com/"
    }
}
