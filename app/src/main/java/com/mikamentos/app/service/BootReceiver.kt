package com.mikamentos.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mikamentos.app.data.repository.MedicationRepository

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val repository = MedicationRepository(context)
            val scheduler = AlarmScheduler(context)

            repository.medications.value.forEach { medication ->
                if (medication.isEnabled) {
                    try {
                        scheduler.cancel(medication.hour, medication.minute)
                        scheduler.schedule(medication)
                    } catch (_: Exception) {}
                }
            }
            val now = System.currentTimeMillis()
            repository.appointments.value.forEach { apt ->
                try {
                    if (apt.isEnabled) {
                        val aptTime = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.YEAR, apt.year)
                            set(java.util.Calendar.MONTH, apt.month - 1)
                            set(java.util.Calendar.DAY_OF_MONTH, apt.day)
                            set(java.util.Calendar.HOUR_OF_DAY, apt.hour)
                            set(java.util.Calendar.MINUTE, apt.minute)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        if (aptTime > now) {
                            scheduler.scheduleAppointment(apt)
                        } else {
                            scheduler.cancelAppointment(apt.id)
                        }
                    } else {
                        scheduler.cancelAppointment(apt.id)
                    }
                } catch (_: Exception) {}
            }
        }
    }
}
