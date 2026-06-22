package com.mikamentos.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikamentos.app.data.model.CatalogMedication
import com.mikamentos.app.data.network.DrugSearchRepository
import com.mikamentos.app.data.repository.MedicationRepository
import com.mikamentos.app.service.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MedicationCatalogViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val drugSearchRepository: DrugSearchRepository,
    private val ttsManager: TtsManager
) : ViewModel() {

    val catalogMedications: StateFlow<List<CatalogMedication>> = repository.catalogMedications
    val settings = repository.settings

    private val _showDeleteError = MutableStateFlow<String?>(null)
    val showDeleteError: StateFlow<String?> = _showDeleteError.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _translatedDescriptions = MutableStateFlow<Map<String, String>>(emptyMap())
    val translatedDescriptions: StateFlow<Map<String, String>> = _translatedDescriptions.asStateFlow()

    private val _translationVersion = MutableStateFlow(0L)
    val translationVersion: StateFlow<Long> = _translationVersion.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    fun loadTranslations() {
        if (_isTranslating.value) return
        val prefs = repository.prefs
        val language = prefs.getString("language", "es") ?: "es"
        val meds = repository.catalogMedications.value
        drugSearchRepository.resetTranslateState()
        android.util.Log.d("CatalogVM", "loadTranslations: lang=$language, meds=${meds.size}")
        _isTranslating.value = true
        viewModelScope.launch {
            val results = mutableMapOf<String, String>()
            for (med in meds) {
                if (med.description.isNotBlank()) {
                    val detected = drugSearchRepository.detectLanguage(med.description)
                    if (detected == language) {
                        results[med.id] = med.description
                    } else {
                        val translated = drugSearchRepository.translateDescription(med.description, language, force = true)
                        results[med.id] = translated
                    }
                    _translatedDescriptions.value = results.toMap()
                    _translationVersion.value++
                }
            }
            _translatedDescriptions.value = results.toMap()
            _translationVersion.value++
            _isTranslating.value = false
            android.util.Log.d("CatalogVM", "Done: ${results.size} translations")
        }
    }

    fun clearDeleteError() { _showDeleteError.value = null }
    fun clearSearchError() { _searchError.value = null }

    fun addCatalogMedication(title: String, description: String) {
        repository.addCatalogMedication(
            CatalogMedication(title = title.trim(), description = description.trim())
        )
        loadTranslations()
    }

    fun updateCatalogMedication(med: CatalogMedication, title: String, description: String) {
        repository.updateCatalogMedication(med.copy(title = title.trim(), description = description.trim()))
        loadTranslations()
    }

    fun deleteCatalogMedication(med: CatalogMedication) {
        val success = repository.deleteCatalogMedication(med.id)
        if (!success) {
            _showDeleteError.value = med.title
        } else {
            loadTranslations()
        }
    }

    fun speakDescription(text: String, language: String) {
        ttsManager.init {
            ttsManager.speak(text, language)
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun searchDrugDescription(name: String, onResult: (String) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null

            val language = repository.prefs.getString("language", "es") ?: "es"

            val result = drugSearchRepository.searchDrugDescription(name, language)
            result.onSuccess { description ->
                onResult(description)
            }.onFailure { error ->
                _searchError.value = error.message ?: "Error al buscar"
            }

            _isSearching.value = false
        }
    }
}
