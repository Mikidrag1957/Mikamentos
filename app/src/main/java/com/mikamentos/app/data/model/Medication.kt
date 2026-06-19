package com.mikamentos.app.data.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val catalogMedicationId: String,
    val title: String,
    val description: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: List<Boolean> = listOf(false, false, false, false, false, false, false),
    val ringtoneUri: String? = null,
    val isEnabled: Boolean = true,
    val repetitions: Int = 1,
    val repetitionMinutes: Int = 1,
    val notes: String? = null,
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getTimeString(is24Hour: Boolean): String {
        return if (is24Hour) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val period = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            String.format("%d:%02d %s", displayHour, minute, period)
        }
    }

    fun getDayNames(): List<String> {
        val dayNames = listOf("L", "M", "X", "J", "V", "S", "D")
        return dayNames.filterIndexed { index, _ -> daysOfWeek.getOrNull(index) == true }
    }
}
