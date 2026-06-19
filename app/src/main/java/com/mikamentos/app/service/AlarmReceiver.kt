package com.mikamentos.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mikamentos.app.R
import com.mikamentos.app.data.repository.MedicationRepository
import com.mikamentos.app.ui.alarm.AlarmActivity
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)
        val isAppointment = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_APPOINTMENT, false)
        Log.d("AlarmReceiver", "onReceive called! isSnooze=$isSnooze isAppointment=$isAppointment")

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "Mikamentos:AlarmReceiver"
        )
        wakeLock.acquire(10_000L)

        if (isAppointment) {
            handleAppointmentAlarm(context, intent, wakeLock)
            return
        }

        val hour = intent.getIntExtra(AlarmScheduler.EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(AlarmScheduler.EXTRA_MINUTE, -1)

        Log.d("AlarmReceiver", "hour=$hour, minute=$minute")

        if (hour == -1 || minute == -1) {
            Log.e("AlarmReceiver", "Invalid hour/minute, returning")
            wakeLock.release()
            return
        }

        val repository = MedicationRepository(context)
        val dayOfWeek = AlarmScheduler.calendarDayToIndex(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
        Log.d("AlarmReceiver", "Current dayOfWeek=$dayOfWeek")

        val medications = repository.getMedicationsForTime(hour, minute, dayOfWeek)
        Log.d("AlarmReceiver", "Medications matching: ${medications.size}")

        if (medications.isEmpty()) {
            Log.d("AlarmReceiver", "No matching medications")
            if (!isSnooze) {
                rescheduleAll(context, repository)
            }
            wakeLock.release()
            return
        }

        Log.d("AlarmReceiver", "Starting AlarmService...")
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_HOUR, hour)
            putExtra(AlarmScheduler.EXTRA_MINUTE, minute)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        val titles = medications.map { it.title }.toTypedArray()
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(AlarmActivity.EXTRA_MEDICATION_TITLES, titles)
            putExtra(AlarmScheduler.EXTRA_HOUR, hour)
            putExtra(AlarmScheduler.EXTRA_MINUTE, minute)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(activityIntent)

        showAlarmNotification(context, activityIntent)

        if (!isSnooze) {
            rescheduleAll(context, repository)
        } else {
            Log.d("AlarmReceiver", "Snooze alarm fired, not rescheduling")
        }

        wakeLock.release()
    }

    private fun showAlarmNotification(context: Context, activityIntent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alarma Mikamentos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificacion de alarma de medicamentos"
                setSound(null, null)
                enableVibration(true)
                setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            ALERT_NOTIFICATION_ID,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titles = activityIntent.getStringArrayExtra(AlarmActivity.EXTRA_MEDICATION_TITLES)
        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(context.getString(R.string.es_hora_de_tomar))
            .setContentText(titles?.joinToString(", ") ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ALERT_NOTIFICATION_ID, notification)
    }

    private fun handleAppointmentAlarm(context: Context, intent: Intent, wakeLock: PowerManager.WakeLock) {
        val message = intent.getStringExtra(AlarmScheduler.EXTRA_APPOINTMENT_MESSAGE) ?: "Recordatorio"
        Log.d("AlarmReceiver", "Appointment alarm: $message")

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_IS_APPOINTMENT, true)
            putExtra(AlarmScheduler.EXTRA_APPOINTMENT_MESSAGE, message)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(AlarmActivity.EXTRA_IS_APPOINTMENT, true)
            putExtra(AlarmActivity.EXTRA_APPOINTMENT_MESSAGE, message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(activityIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alarma Mikamentos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificacion de alarma de citas"
                setSound(null, null)
                enableVibration(true)
                setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            ALERT_NOTIFICATION_ID + 1,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(context.getString(R.string.recordatorio))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ALERT_NOTIFICATION_ID + 1, notification)

        wakeLock.release()
    }

    private fun rescheduleAll(context: Context, repository: MedicationRepository) {
        val scheduler = AlarmScheduler(context)
        val scheduledSlots = mutableSetOf<Int>()
        for (med in repository.medications.value) {
            if (med.isEnabled) {
                val slot = med.hour * 60 + med.minute
                if (scheduledSlots.add(slot)) {
                    scheduler.schedule(med)
                }
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "medication_alarm_channel"
        const val ALERT_CHANNEL_ID = "medication_alert_channel"
        const val ALERT_NOTIFICATION_ID = 2001
    }
}