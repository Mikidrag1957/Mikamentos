package com.mikamentos.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Handler
import android.os.Looper
import com.mikamentos.app.data.repository.MedicationRepository
import com.mikamentos.app.service.AlarmReceiver
import com.mikamentos.app.service.AlarmScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MikamentosApp : Application() {

    @Inject lateinit var repository: MedicationRepository
    @Inject lateinit var scheduler: AlarmScheduler

    private var initialized = false

    override fun onCreate() {
        super.onCreate()
        initialized = true
        createNotificationChannel()
        Handler(Looper.getMainLooper()).postDelayed({
            rescheduleAllAlarms()
        }, 500)
    }

    fun runOnInit(callback: () -> Unit) {
        if (initialized) {
            callback()
        } else {
            Handler(Looper.getMainLooper()).post(callback)
        }
    }

    private fun rescheduleAllAlarms() {
        val now = System.currentTimeMillis()
        repository.medications.value.forEach { med ->
            if (med.isEnabled) {
                try {
                    scheduler.cancel(med.hour, med.minute)
                    scheduler.schedule(med)
                } catch (_: Exception) {}
            }
        }
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

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AlarmReceiver.CHANNEL_ID,
            getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.alarm_channel_desc)
            enableVibration(true)
            setShowBadge(true)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
