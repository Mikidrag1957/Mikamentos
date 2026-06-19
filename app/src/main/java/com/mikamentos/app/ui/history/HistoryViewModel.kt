package com.mikamentos.app.ui.history

import androidx.lifecycle.ViewModel
import com.mikamentos.app.data.model.DoseLog
import com.mikamentos.app.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {
    val logs: StateFlow<List<DoseLog>> = repository.doseLogs
}
