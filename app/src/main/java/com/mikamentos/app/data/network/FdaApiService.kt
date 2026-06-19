package com.mikamentos.app.data.network

import retrofit2.http.GET
import retrofit2.http.Query

data class FdaResponse(
    val results: List<FdaResult>?
)

data class FdaResult(
    val openfda: FdaOpenfda?,
    val description: List<String>?,
    val indications_and_usage: List<String>?,
    val purpose: List<String>?,
    val drug_interactions: List<String>?,
    val warnings: List<String>?
)

data class FdaOpenfda(
    val brand_name: List<String>?,
    val generic_name: List<String>?,
    val manufacturer_name: List<String>?,
    val substance_name: List<String>?
)

interface FdaApiService {
    @GET("drug/label.json")
    suspend fun searchDrug(
        @Query("search") query: String,
        @Query("limit") limit: Int = 1
    ): FdaResponse
}
