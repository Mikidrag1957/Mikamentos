package com.mikamentos.app.data.network

import retrofit2.http.GET
import retrofit2.http.Path

data class LingvaResponse(
    val data: LingvaData
)

data class LingvaData(
    val translation: String
)

interface LingvaService {
    @GET("api/v1/{source}/{target}/{query}")
    suspend fun translate(
        @Path("source") source: String,
        @Path("target") target: String,
        @Path("query") query: String
    ): LingvaResponse
}
