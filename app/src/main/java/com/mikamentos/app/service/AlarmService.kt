package com.mikamentos.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mikamentos.app.MainActivity
import com.mikamentos.app.R
import com.mikamentos.app.data.model.Medication
import com.mikamentos.app.data.repository.MedicationRepository
import com.mikamentos.app.ui.MikamentosTranslations
import java.util.Calendar
import java.util.Calendar.DAY_OF_WEEK

class AlarmService : Service() {

    private var ringtonePlayer: android.media.MediaPlayer? = null
    private var ttsManager: TtsManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                releaseAll()
                return START_NOT_STICKY
            }
        }

        acquireWakeLock()
        startForeground(NOTIFICATION_ID, createNotification())

        val isAppointment = intent?.getBooleanExtra(AlarmScheduler.EXTRA_IS_APPOINTMENT, false) ?: false

        if (isAppointment) {
            val message = intent?.getStringExtra(AlarmScheduler.EXTRA_APPOINTMENT_MESSAGE) ?: "Recordatorio"
            startAppointmentAlarm(message)
            return START_NOT_STICKY
        }

        val hour = intent?.getIntExtra(AlarmScheduler.EXTRA_HOUR, -1) ?: -1
        val minute = intent?.getIntExtra(AlarmScheduler.EXTRA_MINUTE, -1) ?: -1

        if (hour == -1 || minute == -1) {
            releaseAll()
            return START_NOT_STICKY
        }

        val repository = MedicationRepository(this)
        val dayOfWeek = AlarmScheduler.calendarDayToIndex(Calendar.getInstance().get(DAY_OF_WEEK))
        val medications = repository.getMedicationsForTime(hour, minute, dayOfWeek)
        val settings = repository.settings.value

        if (medications.isEmpty()) {
            releaseAll()
            return START_NOT_STICKY
        }

        startAlarm(medications, settings)
        return START_NOT_STICKY
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            WAKE_LOCK_TAG
        )
        wakeLock?.acquire(WAKE_LOCK_TIMEOUT_MS)
    }

    private fun startAlarm(medications: List<Medication>, settings: com.mikamentos.app.data.model.UserSettings) {
        startRingtone(medications, settings)

        val gender = settings.userGender
        val name = settings.userName
        val silenceMs = settings.silenceSeconds * 1000L

        ttsManager = TtsManager(this)
        ttsManager!!.init {
            handler.postDelayed({
                speakInitial(medications, 0, gender, name, silenceMs, settings.language)
            }, 500)
        }
    }

    private fun speakInitial(
        medications: List<Medication>,
        index: Int,
        gender: String,
        name: String,
        silenceMs: Long,
        language: String
    ) {
        val mgr = ttsManager ?: return
        if (index >= medications.size) {
            scheduleRepetitions(medications, gender, name, silenceMs, language)
            return
        }

        val med = medications[index]
        val text = buildSingleMedicationMessage(med, gender, name, language)

        pauseRingtone()
        mgr.speak(text, languageCode = language) {
            handler.postDelayed({
                resumeRingtone()
                val nextDelay = if (index < medications.size - 1) silenceMs else 200L
                handler.postDelayed({
                    speakInitial(medications, index + 1, gender, name, silenceMs, language)
                }, nextDelay)
            }, 200)
        }
    }

    private fun scheduleRepetitions(
        medications: List<Medication>,
        gender: String,
        name: String,
        silenceMs: Long,
        language: String
    ) {
        resumeRingtone()
        for (med in medications) {
            if (med.repetitions > 1) {
                for (rep in 1 until med.repetitions) {
                    val repDelay = rep * med.repetitionMinutes * 60 * 1000L
                    handler.postDelayed({
                        speakRepetition(med, gender, name, language)
                    }, repDelay)
                }
            }
        }
    }

    private fun speakRepetition(
        med: Medication,
        gender: String,
        name: String,
        language: String
    ) {
        val mgr = ttsManager ?: return
        val text = buildSingleMedicationMessage(med, gender, name, language)

        pauseRingtone()
        mgr.speak(text, languageCode = language) {
            handler.postDelayed({
                resumeRingtone()
            }, 200)
        }
    }

    private fun startRingtone(medications: List<Medication>, settings: com.mikamentos.app.data.model.UserSettings) {
        val ringtoneUri = medications.firstOrNull()?.ringtoneUri?.let {
            android.net.Uri.parse(it)
        } ?: if (settings.alarmRingtoneUri.isNotBlank()) {
            android.net.Uri.parse(settings.alarmRingtoneUri)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        try {
            ringtonePlayer = android.media.MediaPlayer().apply {
                setDataSource(this@AlarmService, ringtoneUri)
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start ringtone: ${e.message}")
        }
    }

    private fun pauseRingtone() {
        try {
            ringtonePlayer?.pause()
        } catch (_: Exception) {}
    }

    private fun resumeRingtone() {
        try {
            if (ringtonePlayer != null && !ringtonePlayer!!.isPlaying) {
                ringtonePlayer?.start()
            }
        } catch (_: Exception) {}
    }

    private fun buildSingleMedicationMessage(medication: Medication, gender: String, name: String, language: String): String {
        val localizedText = getLocalizedTimeString(language)
        val sb = StringBuilder()
        if (gender.isNotEmpty()) sb.append("$gender ")
        if (name.isNotEmpty()) sb.append("$name, ")
        sb.append(localizedText)
        sb.append(", ")
        sb.append(medication.title)
        if (!medication.notes.isNullOrBlank()) {
            sb.append(". ")
            sb.append(medication.notes)
        }
        sb.append(".")
        return sb.toString()
    }

    private fun getLocalizedTimeString(language: String): String {
        return MikamentosTranslations.get(language, "es_hora_de_tomar")
            ?: getString(R.string.es_hora_de_tomar)
    }

    private fun startAppointmentAlarm(message: String) {
        val settings = MedicationRepository(this).settings.value
        val ringtoneUri = if (settings.alarmRingtoneUri.isNotBlank()) {
            android.net.Uri.parse(settings.alarmRingtoneUri)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
        try {
            ringtonePlayer = android.media.MediaPlayer().apply {
                setDataSource(this@AlarmService, ringtoneUri)
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start ringtone: ${e.message}")
        }

        ttsManager = TtsManager(this)
        ttsManager!!.init {
            handler.postDelayed({
                pauseRingtone()
                ttsManager!!.speak(message, languageCode = settings.language) {
                    handler.postDelayed({
                        resumeRingtone()
                    }, 200)
                }
            }, 500)
        }
    }

    private fun releaseAll() {
        handler.removeCallbacksAndMessages(null)
        try {
            ringtonePlayer?.stop()
            ringtonePlayer?.release()
        } catch (_: Exception) {}
        ringtonePlayer = null
        ttsManager?.shutdown()
        ttsManager = null
        wakeLock?.let {
            try { it.release() } catch (_: Exception) {}
        }
        wakeLock = null
        @Suppress("DEPRECATION")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun rescheduleAll() {
        val repository = MedicationRepository(this)
        val scheduler = AlarmScheduler(this)
        repository.medications.value.forEach { medication ->
            if (medication.isEnabled) {
                scheduler.schedule(medication)
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

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            NOTIFICATION_ID,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AlarmReceiver.CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.alarm_playing))
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.dismiss),
                stopPendingIntent
            )
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAll()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.mikamentos.app.ACTION_STOP_ALARM"
        private const val WAKE_LOCK_TAG = "Mikamentos:WakeLock"
        private const val WAKE_LOCK_TIMEOUT_MS = 10 * 60 * 1000L
    }
}
