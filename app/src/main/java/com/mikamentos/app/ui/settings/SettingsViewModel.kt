package com.mikamentos.app.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.mikamentos.app.data.model.UserSettings
import com.mikamentos.app.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.settings
    val prefs: SharedPreferences get() = repository.prefs

    fun updateLanguage(language: String) {
        repository.saveSettings(settings.value.copy(language = language))
    }

    fun updateSilenceSeconds(seconds: Int) {
        repository.saveSettings(settings.value.copy(silenceSeconds = seconds))
    }

    fun update24HourFormat(is24Hour: Boolean) {
        repository.saveSettings(settings.value.copy(is24HourFormat = is24Hour))
    }

    fun updateUserGender(gender: String) {
        repository.saveSettings(settings.value.copy(userGender = gender))
    }

    fun updateUserName(name: String) {
        repository.saveSettings(settings.value.copy(userName = name))
    }

    fun updateDefaultRepetitions(repetitions: Int) {
        repository.saveSettings(settings.value.copy(defaultRepetitions = repetitions))
    }

    fun updateRepetitionMinutes(minutes: Int) {
        repository.saveSettings(settings.value.copy(repetitionMinutes = minutes))
    }

    fun updateThemeMode(mode: Int) {
        repository.saveSettings(settings.value.copy(themeMode = mode))
    }

    fun updateAlarmRingtone(uri: String, name: String) {
        repository.saveSettings(settings.value.copy(alarmRingtoneUri = uri, alarmRingtoneName = name))
    }

    fun exportAllData(): String {
        return repository.exportAllData()
    }

    fun importAllData(json: String): Boolean {
        return repository.importAllData(json)
    }
}
