package com.mikamentos.app.data.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mikamentos.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class BundledMedication(
    val names: List<String>,
    @SerializedName("principio_activo") val principioActivo: String = "",
    val dosis: String = "",
    @SerializedName("para_que_se_usa") val paraQueSeUsa: String = "",
    @SerializedName("como_se_usa") val comoSeUsa: String = ""
)

data class BundledMedicationData(
    val version: Int = 1,
    val generated: String = "",
    val count: Int = 0,
    val medications: List<BundledMedication> = emptyList()
)

@Singleton
class BundledDrugData @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var medications: List<BundledMedication> = emptyList()
    private var loaded = false
    private val nameToMed = mutableMapOf<String, BundledMedication>()

    fun search(query: String): BundledMedication? {
        if (!loaded) load()
        val lower = query.trim().lowercase()
        return nameToMed[lower]
    }

    private fun load() {
        loaded = true
        try {
            val json = context.resources.openRawResource(R.raw.bundled_medications)
                .bufferedReader().use { it.readText() }
            val data = Gson().fromJson(json, BundledMedicationData::class.java)
            medications = data.medications
            for (med in medications) {
                for (name in med.names) {
                    nameToMed[name.lowercase()] = med
                }
            }
        } catch (_: Exception) {}
    }
}
