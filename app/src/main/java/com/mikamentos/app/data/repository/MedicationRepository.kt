package com.mikamentos.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mikamentos.app.data.model.Appointment
import com.mikamentos.app.data.model.CatalogMedication
import com.mikamentos.app.data.model.DoseLog
import com.mikamentos.app.data.model.Medication
import com.mikamentos.app.data.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class MedicationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val prefs: SharedPreferences =
        context.getSharedPreferences("mikamentos_prefs", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val dataDir: File by lazy {
        File(context.filesDir, "Mikamentos").also { it.mkdirs() }
    }

    private val catalogFile: File get() = File(dataDir, "catalog.json")
    private val medicationsFile: File get() = File(dataDir, "medications.json")
    private val settingsFile: File get() = File(dataDir, "settings.json")
    private val doseLogsFile: File get() = File(dataDir, "dose_logs.json")
    private val appointmentsFile: File get() = File(dataDir, "appointments.json")

    private val _doseLogs = MutableStateFlow<List<DoseLog>>(emptyList())
    val doseLogs: StateFlow<List<DoseLog>> = _doseLogs.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private fun loadDoseLogs() {
        try {
            val json = if (doseLogsFile.exists()) doseLogsFile.readText() else null
            if (json != null) {
                val type = object : TypeToken<List<DoseLog>>() {}.type
                val items: List<DoseLog>? = gson.fromJson(json, type)
                if (!items.isNullOrEmpty()) {
                    _doseLogs.value = items
                }
            }
        } catch (_: Exception) {}
    }

    private val _catalogMedications = MutableStateFlow<List<CatalogMedication>>(emptyList())
    val catalogMedications: StateFlow<List<CatalogMedication>> = _catalogMedications.asStateFlow()

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    init {
        loadCatalog()
        loadMedications()
        loadSettings()
        loadDoseLogs()
        loadAppointments()
    }

    // --- CATALOG ---

    private fun loadCatalog() {
        var json: String? = null
        var items: List<CatalogMedication>? = null

        try {
            json = prefs.getString(KEY_CATALOG, null)
            if (json != null) {
                val type = object : TypeToken<List<CatalogMedication>>() {}.type
                items = gson.fromJson(json, type)
            }
        } catch (e: Exception) {
            android.util.Log.e("MedicationRepo", "Catalog SP failed: ${e.message}")
        }

        if (items.isNullOrEmpty()) {
            try {
                json = if (catalogFile.exists()) catalogFile.readText() else null
                if (json != null) {
                    val type = object : TypeToken<List<CatalogMedication>>() {}.type
                    items = gson.fromJson(json, type)
                    if (!items.isNullOrEmpty()) {
                        prefs.edit().putString(KEY_CATALOG, json).apply()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MedicationRepo", "Catalog file failed: ${e.message}")
            }
        }

        if (!items.isNullOrEmpty()) {
            _catalogMedications.value = items
        }
    }

    fun saveCatalog(items: List<CatalogMedication>) {
        _catalogMedications.value = items
        val json = gson.toJson(items)
        prefs.edit().putString(KEY_CATALOG, json).commit()
        try { catalogFile.writeText(json) } catch (_: Exception) {}
    }

    fun addCatalogMedication(med: CatalogMedication) {
        val current = _catalogMedications.value.toMutableList()
        current.add(med)
        saveCatalog(current)
    }

    fun updateCatalogMedication(med: CatalogMedication) {
        val current = _catalogMedications.value.toMutableList()
        val index = current.indexOfFirst { it.id == med.id }
        if (index != -1) {
            current[index] = med
            saveCatalog(current)
        }
    }

    fun deleteCatalogMedication(id: String): Boolean {
        val isScheduled = _medications.value.any { it.catalogMedicationId == id }
        if (isScheduled) return false
        val current = _catalogMedications.value.toMutableList()
        current.removeAll { it.id == id }
        saveCatalog(current)
        return true
    }

    fun isCatalogMedicationScheduled(catalogId: String): Boolean {
        return _medications.value.any { it.catalogMedicationId == catalogId }
    }

    fun getCatalogMedicationById(id: String): CatalogMedication? {
        return _catalogMedications.value.find { it.id == id }
    }

    // --- SCHEDULED MEDICATIONS ---

    private fun loadMedications() {
        var json: String? = null
        var items: List<Medication>? = null

        try {
            json = prefs.getString(KEY_MEDICATIONS, null)
            if (json != null) {
                val type = object : TypeToken<List<Medication>>() {}.type
                items = gson.fromJson(json, type)
            }
        } catch (e: Exception) {
            android.util.Log.e("MedicationRepo", "SP deserialize failed: ${e.message}")
        }

        if (items.isNullOrEmpty()) {
            try {
                json = if (medicationsFile.exists()) medicationsFile.readText() else null
                if (json != null) {
                    val type = object : TypeToken<List<Medication>>() {}.type
                    items = gson.fromJson(json, type)
                    if (!items.isNullOrEmpty()) {
                        prefs.edit().putString(KEY_MEDICATIONS, json).apply()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MedicationRepo", "File deserialize failed: ${e.message}")
            }
        }

        if (!items.isNullOrEmpty()) {
            val fixed = items.map { med ->
                if (!med.isEnabled && med.daysOfWeek.any { it }) med.copy(isEnabled = true) else med
            }
            _medications.value = fixed
            if (fixed != items) {
                val fixedJson = gson.toJson(fixed)
                prefs.edit().putString(KEY_MEDICATIONS, fixedJson).commit()
                try { medicationsFile.writeText(fixedJson) } catch (_: Exception) {}
            }
        } else {
            android.util.Log.w("MedicationRepo", "No medications found on disk")
        }
    }

    fun saveMedications(medications: List<Medication>) {
        _medications.value = medications
        val json = gson.toJson(medications)
        prefs.edit().putString(KEY_MEDICATIONS, json).commit()
        try { medicationsFile.writeText(json) } catch (_: Exception) {}
    }

    fun addMedication(medication: Medication) {
        val current = _medications.value.toMutableList()
        current.add(medication)
        saveMedications(current)
    }

    fun updateMedication(medication: Medication) {
        val current = _medications.value.toMutableList()
        val index = current.indexOfFirst { it.id == medication.id }
        if (index != -1) {
            current[index] = medication
            saveMedications(current)
        }
    }

    fun deleteMedication(id: String) {
        val current = _medications.value.toMutableList()
        current.removeAll { it.id == id }
        saveMedications(current)
    }

    fun importMedicationsFromJson(medsJson: String) {
        try {
            val type = object : TypeToken<List<Medication>>() {}.type
            val items: List<Medication>? = gson.fromJson(medsJson, type)
            if (!items.isNullOrEmpty()) {
                _medications.value = items
                prefs.edit().putString(KEY_MEDICATIONS, medsJson).commit()
                try { medicationsFile.writeText(medsJson) } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    fun getMedicationsForTime(hour: Int, minute: Int, dayOfWeek: Int): List<Medication> {
        return _medications.value.filter { med ->
            med.isEnabled && med.hour == hour && med.minute == minute &&
                med.daysOfWeek.getOrNull(dayOfWeek) == true
        }
    }

    // --- SETTINGS ---

    private fun loadSettings() {
        var json = prefs.getString(KEY_SETTINGS, null)
        if (json == null) {
            try {
                json = if (settingsFile.exists()) settingsFile.readText() else null
            } catch (_: Exception) {}
        }
        if (json != null) {
            val settings = gson.fromJson(json, UserSettings::class.java)
            if (settings != null) {
                _settings.value = settings
            }
        }
    }

    fun saveSettings(settings: UserSettings) {
        _settings.value = settings
        val json = gson.toJson(settings)
        prefs.edit()
            .putString(KEY_SETTINGS, json)
            .putString("language", settings.language)
            .putInt("theme_mode", settings.themeMode)
            .commit()
        try { settingsFile.writeText(json) } catch (_: Exception) {}
    }

    private val translationCacheFile: File get() = File(dataDir, "translations_cache.json")
    private var diskTranslationCache: Map<String, String> = emptyMap()

    init {
        loadTranslationCache()
    }

    private fun loadTranslationCache() {
        try {
            val json = if (translationCacheFile.exists()) translationCacheFile.readText() else null
            if (json != null) {
                val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                diskTranslationCache = gson.fromJson(json, type) ?: emptyMap()
            }
        } catch (_: Exception) {
            diskTranslationCache = emptyMap()
        }
    }

    fun getTranslationFromCache(key: String): String? {
        return diskTranslationCache[key]
    }

    fun saveTranslationToCache(key: String, value: String) {
        diskTranslationCache = diskTranslationCache + (key to value)
        try {
            translationCacheFile.writeText(gson.toJson(diskTranslationCache))
        } catch (_: Exception) {}
    }

    fun clearTranslationCache() {
        diskTranslationCache = emptyMap()
        try {
            translationCacheFile.delete()
        } catch (_: Exception) {}
    }

    fun addDoseLog(log: DoseLog) {
        val current = _doseLogs.value.toMutableList()
        current.add(log)
        _doseLogs.value = current
        try {
            doseLogsFile.writeText(gson.toJson(current))
        } catch (_: Exception) {}
    }

    // --- APPOINTMENTS ---

    private fun loadAppointments() {
        try {
            val json = if (appointmentsFile.exists()) appointmentsFile.readText() else null
            if (json != null) {
                val type = object : TypeToken<List<Appointment>>() {}.type
                val items: List<Appointment>? = gson.fromJson(json, type)
                if (!items.isNullOrEmpty()) {
                    _appointments.value = items
                }
            }
        } catch (_: Exception) {}
    }

    fun saveAppointment(appointment: Appointment) {
        val current = _appointments.value.toMutableList()
        val index = current.indexOfFirst { it.id == appointment.id }
        if (index != -1) {
            current[index] = appointment
        } else {
            current.add(appointment)
        }
        _appointments.value = current
        try {
            appointmentsFile.writeText(gson.toJson(current))
        } catch (_: Exception) {}
    }

    fun deleteAppointment(id: String) {
        val current = _appointments.value.toMutableList()
        current.removeAll { it.id == id }
        _appointments.value = current
        try {
            appointmentsFile.writeText(gson.toJson(current))
        } catch (_: Exception) {}
    }

    fun getMyMemoryEmail(): String? {
        val email = prefs.getString("mymemory_email", null)
        return if (email.isNullOrBlank()) null else email
    }

    fun exportAllData(): String {
        val data = mapOf(
            "export_version" to 1,
            "export_date" to System.currentTimeMillis(),
            "medications" to _medications.value,
            "catalog" to _catalogMedications.value,
            "settings" to _settings.value,
            "appointments" to _appointments.value,
            "dose_logs" to _doseLogs.value
        )
        return gson.toJson(data)
    }

    fun importAllData(json: String): Boolean {
        return try {
            val obj = com.google.gson.JsonParser.parseString(json).asJsonObject

            if (obj.has("medications")) {
                val type = object : TypeToken<List<Medication>>() {}.type
                val items: List<Medication> = gson.fromJson(obj.get("medications"), type)
                _medications.value = items
                prefs.edit().putString(KEY_MEDICATIONS, gson.toJson(items)).commit()
                try { medicationsFile.writeText(gson.toJson(items)) } catch (_: Exception) {}
            }
            if (obj.has("catalog")) {
                val type = object : TypeToken<List<CatalogMedication>>() {}.type
                val items: List<CatalogMedication> = gson.fromJson(obj.get("catalog"), type)
                _catalogMedications.value = items
                prefs.edit().putString(KEY_CATALOG, gson.toJson(items)).commit()
                try { catalogFile.writeText(gson.toJson(items)) } catch (_: Exception) {}
            }
            if (obj.has("settings")) {
                val item: UserSettings = gson.fromJson(obj.get("settings"), UserSettings::class.java)
                saveSettings(item)
            }
            if (obj.has("appointments")) {
                val type = object : TypeToken<List<Appointment>>() {}.type
                val items: List<Appointment> = gson.fromJson(obj.get("appointments"), type)
                _appointments.value = items
                try { appointmentsFile.writeText(gson.toJson(items)) } catch (_: Exception) {}
            }
            if (obj.has("dose_logs")) {
                val type = object : TypeToken<List<DoseLog>>() {}.type
                val items: List<DoseLog> = gson.fromJson(obj.get("dose_logs"), type)
                _doseLogs.value = items
                try { doseLogsFile.writeText(gson.toJson(items)) } catch (_: Exception) {}
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val KEY_CATALOG = "catalog"
        private const val KEY_MEDICATIONS = "medications"
        private const val KEY_SETTINGS = "settings"
    }
}
