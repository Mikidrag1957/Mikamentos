package com.mikamentos.app.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

data class CimaResponse(
    val totalFilas: Int,
    val pagina: Int,
    val tamanioPagina: Int,
    val resultados: List<CimaMedicamento>?
)

data class CimaMedicamento(
    val cn: String? = null,
    val nregistro: String? = null,
    val nombre: String? = null,
    val principioActivo: String? = null,
    val formaFarmaceutica: CimaFormaFarmaceutica? = null,
    val labtitular: String? = null,
    val estcom: String? = null,
    val estado: CimaEstado? = null,
    val vtm: CimaVtm? = null,
    val labcomercializador: String? = null,
    val dosis: String? = null
)

data class CimaEstado(
    val cod: String? = null,
    val nombre: String? = null
)

data class CimaFormaFarmaceutica(
    val id: Int = 0,
    val nombre: String? = null
)

data class CimaVtm(
    val id: Long = 0,
    val nombre: String? = null
)

interface CimaApiService {
    @GET("medicamentos")
    suspend fun searchMedicamentos(
        @Query("nombre") nombre: String,
        @Query("pagina") pagina: Int = 1,
        @Query("pagesize") pagesize: Int = 5
    ): CimaResponse

    @Headers("Accept: text/plain")
    @GET("docSegmentado/contenido/2")
    suspend fun getProspecto(@Query("nregistro") nregistro: String): ResponseBody
}
