package com.mikamentos.app.data.network

import retrofit2.http.GET
import retrofit2.http.Query

data class MyMemoryResponse(
    val responseData: MyMemoryData,
    val responseStatus: Int
)

data class MyMemoryData(
    val translatedText: String,
    val match: Float
)

interface TranslationService {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String,
        @Query("de") email: String? = null
    ): MyMemoryResponse
}
