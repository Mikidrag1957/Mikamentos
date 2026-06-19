package com.mikamentos.app.data.model

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val message: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDisplayDateTime(is24Hour: Boolean): String {
        val dateStr = String.format("%02d/%02d/%d", day, month, year)
        val timeStr = if (is24Hour) {
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
        return "$dateStr $timeStr"
    }
}
