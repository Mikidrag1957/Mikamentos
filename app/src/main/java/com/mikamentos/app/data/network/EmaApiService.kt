package com.mikamentos.app.data.network

import retrofit2.http.GET
import retrofit2.http.Query

data class EmaResponse(
    val resourceType: String?,
    val type: String?,
    val total: Int?,
    val entry: List<EmaEntry>?
)

data class EmaEntry(
    val fullUrl: String?,
    val resource: EmaResource?
)

data class EmaResource(
    val resourceType: String?,
    val id: String?,
    val code: EmaCode?,
    val status: String?
)

data class EmaCode(
    val coding: List<EmaCoding>?
)

data class EmaCoding(
    val system: String?,
    val code: String?,
    val display: String?
)

interface EmaApiService {
    @GET("fhir/Bundle")
    suspend fun searchMedications(
        @Query("\$filter") filter: String,
        @Query("_count") count: Int = 5
    ): EmaResponse
}
