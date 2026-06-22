package com.mikamentos.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikamentos.app.data.model.Medication
import com.mikamentos.app.data.network.DrugSearchRepository
import com.mikamentos.app.data.repository.MedicationRepository
import com.mikamentos.app.service.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler,
    private val drugSearchRepository: DrugSearchRepository
) : ViewModel() {

    val medications: StateFlow<List<Medication>> = repository.medications

    private val _translatedDescriptions = MutableStateFlow<Map<String, String>>(emptyMap())
    val translatedDescriptions: StateFlow<Map<String, String>> = _translatedDescriptions.asStateFlow()

    init {
        loadTranslations()
    }

    fun loadTranslations() {
        val language = repository.settings.value.language
        val meds = repository.medications.value
        viewModelScope.launch {
            val results = mutableMapOf<String, String>()
            for (med in meds) {
                if (med.description.isNotBlank()) {
                    results[med.id] = drugSearchRepository.translateDescription(med.description, language, force = true)
                }
            }
            _translatedDescriptions.value = results
        }
    }

    fun deleteMedication(medication: Medication) {
        repository.deleteMedication(medication.id)
        alarmScheduler.cancel(medication.hour, medication.minute)
        loadTranslations()
    }
}
