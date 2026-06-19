package com.mikamentos.app.ui.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import com.mikamentos.app.service.AlarmReceiver
import com.mikamentos.app.ui.tr
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikamentos.app.R
import com.mikamentos.app.data.model.DoseLog
import com.mikamentos.app.data.repository.MedicationRepository
import com.mikamentos.app.service.AlarmScheduler
import com.mikamentos.app.service.AlarmService
import com.mikamentos.app.ui.theme.MikamentosTheme

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(AlarmReceiver.ALERT_NOTIFICATION_ID)
        nm.cancel(AlarmReceiver.ALERT_NOTIFICATION_ID + 1)

        acquireScreenWakeLock()

        val isAppointment = intent.getBooleanExtra(EXTRA_IS_APPOINTMENT, false)
        val titles = intent.getStringArrayExtra(EXTRA_MEDICATION_TITLES) ?: emptyArray()
        val hour = intent.getIntExtra(AlarmScheduler.EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(AlarmScheduler.EXTRA_MINUTE, -1)

        if (isAppointment) {
            val message = intent.getStringExtra(EXTRA_APPOINTMENT_MESSAGE) ?: "Recordatorio"
            setContent {
                MikamentosTheme {
                    AppointmentAlarmScreen(
                        message = message,
                        onDismiss = {
                            stopAlarm()
                            finish()
                        }
                    )
                }
            }
            return
        }

        setContent {
            MikamentosTheme {
                AlarmDismissScreen(
                    medicationTitles = titles.toList(),
                    onConfirm = {
                        val repo = MedicationRepository(this@AlarmActivity)
                        titles.forEach { title ->
                            repo.addDoseLog(DoseLog(medicationTitle = title, hour = hour, minute = minute, action = "confirm"))
                        }
                        stopAlarm()
                        finish()
                    },
                    onSnooze = {
                        val scheduler = AlarmScheduler(this@AlarmActivity)
                        scheduler.snooze(hour, minute, 10)
                        stopAlarm()
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(EXTRA_DISMISS, false)) {
            stopAlarm()
            finish()
        }
    }

    private fun acquireScreenWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wl = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "Mikamentos:AlarmActivity"
        )
        wl.acquire(WAKE_LOCK_DURATION_MS)
    }

    private fun stopAlarm() {
        stopService(Intent(this, AlarmService::class.java))
    }

    companion object {
        const val EXTRA_MEDICATION_TITLES = "medication_titles"
        const val EXTRA_MEDICATION_DESCRIPTIONS = "medication_descriptions"
        const val EXTRA_DISMISS = "dismiss"
        const val EXTRA_IS_APPOINTMENT = "is_appointment"
        const val EXTRA_APPOINTMENT_MESSAGE = "appointment_message"
        private const val WAKE_LOCK_DURATION_MS = 5000L
    }
}

@Composable
fun AppointmentAlarmScreen(
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = tr(R.string.recordatorio),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = message,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(48.dp))

        FilledIconButton(
            onClick = onDismiss,
            modifier = Modifier.size(96.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                Icons.Default.AlarmOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = tr(R.string.dismiss),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AlarmDismissScreen(
    medicationTitles: List<String>,
    onConfirm: () -> Unit,
    onSnooze: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = tr(R.string.es_hora_de_tomar),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(medicationTitles) { title ->
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        FilledIconButton(
            onClick = onConfirm,
            modifier = Modifier.size(96.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = tr(R.string.yes_took_them),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSnooze,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Snooze,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = tr(R.string.no_remind_me),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}