package com.mikamentos.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.mikamentos.app.MainActivity
import com.mikamentos.app.data.model.Appointment
import com.mikamentos.app.data.model.Medication
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }

    fun schedule(medication: Medication) {
        if (medication.daysOfWeek.none { it }) {
            Log.w("AlarmScheduler", "No days selected for ${medication.title}, skipping schedule")
            return
        }

        val requestCode = medication.hour * 60 + medication.minute

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_HOUR, medication.hour)
            putExtra(EXTRA_MINUTE, medication.minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = getNextTriggerTime(medication)

        Log.d("AlarmScheduler", "Scheduling alarm for ${medication.title} at ${medication.hour}:${String.format("%02d", medication.minute)} trigger=$triggerTime requestCode=$requestCode")

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode + 1,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("AlarmScheduler", "setAlarmClock OK")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "setAlarmClock SecurityException: ${e.message}")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "setAlarmClock failed: ${e.message}")
        }

        enableBootReceiver()
    }

    fun cancel(hour: Int, minute: Int) {
        val requestCode = hour * 60 + minute
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun snooze(hour: Int, minute: Int, minutesLater: Int = 15) {
        val cal = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.MINUTE, minutesLater)
        }
        val triggerTime = cal.timeInMillis
        val snoozeHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val snoozeMinute = cal.get(java.util.Calendar.MINUTE)

        Log.d("AlarmScheduler", "Snooze: original=$hour:$minute, scheduling for $snoozeHour:$snoozeMinute (${minutesLater}min later) trigger=$triggerTime")

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_HOUR, hour)
            putExtra(EXTRA_MINUTE, minute)
            putExtra(EXTRA_IS_SNOOZE, true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SNOOZE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val showIntent = Intent(context, MainActivity::class.java)
            val showPendingIntent = PendingIntent.getActivity(
                context,
                SNOOZE_REQUEST_CODE + 1,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("AlarmScheduler", "Snooze setAlarmClock OK")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "snooze setAlarmClock failed: ${e.message}")
        }
    }

    fun cancelSnooze() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SNOOZE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun getNextTriggerTime(medication: Medication): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, medication.hour)
            set(Calendar.MINUTE, medication.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (!trigger.after(now)) {
            trigger.add(Calendar.DAY_OF_MONTH, 1)
        }

        var safety = 0
        while (medication.daysOfWeek.getOrNull(calendarDayToIndex(trigger.get(Calendar.DAY_OF_WEEK))) != true) {
            trigger.add(Calendar.DAY_OF_MONTH, 1)
            safety++
            if (safety > 7) {
                Log.e("AlarmScheduler", "getNextTriggerTime: exceeded 7 days for ${medication.title}")
                break
            }
        }

        return trigger.timeInMillis
    }

    private fun enableBootReceiver() {
        val receiver = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun scheduleAppointment(appointment: Appointment) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, appointment.year)
            set(Calendar.MONTH, appointment.month - 1)
            set(Calendar.DAY_OF_MONTH, appointment.day)
            set(Calendar.HOUR_OF_DAY, appointment.hour)
            set(Calendar.MINUTE, appointment.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val requestCode = APPOINTMENT_BASE + appointment.id.hashCode()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_IS_APPOINTMENT, true)
            putExtra(EXTRA_APPOINTMENT_ID, appointment.id)
            putExtra(EXTRA_APPOINTMENT_MESSAGE, appointment.message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode + 1,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(cal.timeInMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("AlarmScheduler", "Appointment scheduled: ${appointment.message} at ${appointment.getDisplayDateTime(true)}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "scheduleAppointment SecurityException: ${e.message}")
        }
    }

    fun cancelAppointment(appointmentId: String) {
        val requestCode = APPOINTMENT_BASE + appointmentId.hashCode()
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    companion object {
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_HOUR = "hour"
        const val EXTRA_MINUTE = "minute"
        const val EXTRA_IS_SNOOZE = "is_snooze"
        const val EXTRA_IS_APPOINTMENT = "is_appointment"
        const val EXTRA_APPOINTMENT_ID = "appointment_id"
        const val EXTRA_APPOINTMENT_MESSAGE = "appointment_message"
        const val SNOOZE_REQUEST_CODE = 99999
        private const val APPOINTMENT_BASE = 200000

        fun calendarDayToIndex(calendarDay: Int): Int {
            return (calendarDay + 5) % 7
        }
    }
}
