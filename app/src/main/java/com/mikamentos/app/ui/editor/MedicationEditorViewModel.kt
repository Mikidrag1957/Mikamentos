package com.mikamentos.app.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikamentos.app.data.model.CatalogMedication
import com.mikamentos.app.data.model.Medication
import com.mikamentos.app.data.repository.MedicationRepository
import com.mikamentos.app.service.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MedicationEditorViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicationId: String? = savedStateHandle.get<String>("medicationId")

    val catalogMedications: StateFlow<List<CatalogMedication>> = repository.catalogMedications

    private val _selectedCatalogMed = MutableStateFlow<CatalogMedication?>(null)
    val selectedCatalogMed: StateFlow<CatalogMedication?> = _selectedCatalogMed.asStateFlow()

    private val _hour = MutableStateFlow(8)
    val hour: StateFlow<Int> = _hour.asStateFlow()

    private val _minute = MutableStateFlow(0)
    val minute: StateFlow<Int> = _minute.asStateFlow()

    private val _daysOfWeek = MutableStateFlow(List(7) { false })
    val daysOfWeek: StateFlow<List<Boolean>> = _daysOfWeek.asStateFlow()

    private val _repetitions = MutableStateFlow(1)
    val repetitions: StateFlow<Int> = _repetitions.asStateFlow()

    private val _repetitionMinutes = MutableStateFlow(1)
    val repetitionMinutes: StateFlow<Int> = _repetitionMinutes.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _photoPath = MutableStateFlow<String?>(null)
    val photoPath: StateFlow<String?> = _photoPath.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    private var existingMedication: Medication? = null

    init {
        if (medicationId != null) {
            val existing = repository.medications.value.find { it.id == medicationId }
            if (existing != null) {
                existingMedication = existing
                _hour.value = existing.hour
                _minute.value = existing.minute
                _daysOfWeek.value = existing.daysOfWeek
                _repetitions.value = existing.repetitions
                _repetitionMinutes.value = existing.repetitionMinutes
                _notes.value = existing.notes ?: ""
                _photoPath.value = existing.photoPath
                val catMed = repository.getCatalogMedicationById(existing.catalogMedicationId)
                _selectedCatalogMed.value = catMed
            }
        } else {
            val settings = repository.settings.value
            _repetitions.value = settings.defaultRepetitions
            _repetitionMinutes.value = settings.repetitionMinutes
        }
    }

    fun clearProfileError() { _profileError.value = null }

    fun selectCatalogMedication(med: CatalogMedication) {
        _selectedCatalogMed.value = med
    }

    fun updateHour(value: Int) { _hour.value = value }
    fun updateMinute(value: Int) { _minute.value = value }
    fun updateDay(index: Int, enabled: Boolean) {
        val current = _daysOfWeek.value.toMutableList()
        current[index] = enabled
        _daysOfWeek.value = current
    }
    fun updateRepetitions(value: Int) { _repetitions.value = value }
    fun updateRepetitionMinutes(value: Int) { _repetitionMinutes.value = value }
    fun updateNotes(value: String) { _notes.value = value }
    fun updatePhotoPath(value: String?) { _photoPath.value = value }

    fun save(onSuccess: () -> Unit, onProfileRequired: () -> Unit) {
        val catMed = _selectedCatalogMed.value ?: return

        val settings = repository.settings.value
        if (settings.userGender.isBlank() || settings.userName.isBlank()) {
            _profileError.value = "profile_required"
            onProfileRequired()
            return
        }

        val daysSelected = _daysOfWeek.value.any { it }

        val notes = _notes.value.trim().ifEmpty { null }
        val medication = if (medicationId != null && existingMedication != null) {
            existingMedication!!.copy(
                catalogMedicationId = catMed.id,
                title = catMed.title,
                description = catMed.description,
                hour = _hour.value,
                minute = _minute.value,
                daysOfWeek = _daysOfWeek.value,
                isEnabled = if (daysSelected) existingMedication!!.isEnabled else false,
                repetitions = _repetitions.value,
                repetitionMinutes = _repetitionMinutes.value,
                notes = notes,
                photoPath = _photoPath.value
            )
        } else {
            Medication(
                catalogMedicationId = catMed.id,
                title = catMed.title,
                description = catMed.description,
                hour = _hour.value,
                minute = _minute.value,
                daysOfWeek = _daysOfWeek.value,
                isEnabled = daysSelected,
                repetitions = _repetitions.value,
                repetitionMinutes = _repetitionMinutes.value,
                notes = notes,
                photoPath = _photoPath.value
            )
        }

        if (medicationId != null) {
            repository.updateMedication(medication)
            if (existingMedication != null) {
                alarmScheduler.cancel(existingMedication!!.hour, existingMedication!!.minute)
            }
        } else {
            repository.addMedication(medication)
        }

        if (medication.isEnabled) {
            alarmScheduler.schedule(medication)
        }

        onSuccess()
    }
}
