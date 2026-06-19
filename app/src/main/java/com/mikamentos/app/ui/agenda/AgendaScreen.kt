package com.mikamentos.app.ui.agenda

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikamentos.app.R
import com.mikamentos.app.data.model.Appointment
import com.mikamentos.app.data.repository.MedicationRepository
import com.mikamentos.app.service.AlarmScheduler
import com.mikamentos.app.ui.tr
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { MedicationRepository(context) }
    val scheduler = remember { AlarmScheduler(context) }
    val appointments by repository.appointments.collectAsState()
    val settings by repository.settings.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingAppointment by remember { mutableStateOf<Appointment?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(R.string.agenda)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = tr(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingAppointment = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = tr(R.string.new_appointment))
            }
        }
    ) { paddingValues ->
        val sorted = appointments
            .filter { it.isEnabled }
            .sortedBy { "${it.year}${"%02d".format(it.month)}${"%02d".format(it.day)}${"%02d".format(it.hour)}${"%02d".format(it.minute)}" }

        if (sorted.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Text(
                    text = tr(R.string.no_appointments),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sorted, key = { it.id }) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        is24Hour = settings.is24HourFormat,
                        onEdit = {
                            editingAppointment = appointment
                            showDialog = true
                        },
                        onDelete = {
                            scheduler.cancelAppointment(appointment.id)
                            repository.deleteAppointment(appointment.id)
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AppointmentDialog(
            appointment = editingAppointment,
            onDismiss = { showDialog = false; editingAppointment = null },
            onConfirm = { appt ->
                if (editingAppointment != null) {
                    scheduler.cancelAppointment(editingAppointment!!.id)
                    repository.saveAppointment(appt)
                } else {
                    repository.saveAppointment(appt)
                }
                if (appt.isEnabled) {
                    scheduler.scheduleAppointment(appt)
                }
                showDialog = false
                editingAppointment = null
            }
        )
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    is24Hour: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appointment.getDisplayDateTime(is24Hour),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = tr(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(tr(R.string.confirm_delete_title)) },
            text = { Text(tr(R.string.confirm_delete_message).replace("%s", appointment.message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(tr(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(tr(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AppointmentDialog(
    appointment: Appointment?,
    onDismiss: () -> Unit,
    onConfirm: (Appointment) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var year by remember(appointment) { mutableStateOf(appointment?.year ?: calendar.get(Calendar.YEAR)) }
    var month by remember(appointment) { mutableStateOf(appointment?.month ?: (calendar.get(Calendar.MONTH) + 1)) }
    var day by remember(appointment) { mutableStateOf(appointment?.day ?: calendar.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember(appointment) { mutableStateOf(appointment?.hour ?: calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember(appointment) { mutableStateOf(appointment?.minute ?: calendar.get(Calendar.MINUTE)) }
    var message by remember(appointment) { mutableStateOf(appointment?.message ?: "") }

    var showDiscardDialog by remember { mutableStateOf(false) }

    val original = appointment
    fun hasChanges(): Boolean {
        if (original == null) return message.isNotBlank()
        return year != original.year || month != original.month || day != original.day ||
            hour != original.hour || minute != original.minute || message != original.message
    }

    fun confirmDismiss() {
        if (hasChanges()) {
            showDiscardDialog = true
        } else {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { confirmDismiss() },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (appointment == null) tr(R.string.new_appointment) else tr(R.string.edit_appointment),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { confirmDismiss() }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = tr(R.string.close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = tr(R.string.appointment_date),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%02d/%02d/%d", day, month, year),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                TextButton(onClick = {
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            year = y
                            month = m + 1
                            day = d
                        },
                        year, month - 1, day
                    ).show()
                }) {
                    Text(tr(R.string.appointment_date))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tr(R.string.appointment_time),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%02d:%02d", hour, minute),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                TextButton(onClick = {
                    TimePickerDialog(
                        context,
                        { _, h, m ->
                            hour = h
                            minute = m
                        },
                        hour, minute, true
                    ).show()
                }) {
                    Text(tr(R.string.appointment_time))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(tr(R.string.appointment_message)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = message.isNotBlank(),
                onClick = {
                    val appt = Appointment(
                        id = appointment?.id ?: java.util.UUID.randomUUID().toString(),
                        year = year,
                        month = month,
                        day = day,
                        hour = hour,
                        minute = minute,
                        message = message.trim(),
                        isEnabled = true,
                        createdAt = appointment?.createdAt ?: System.currentTimeMillis()
                    )
                    onConfirm(appt)
                }
            ) {
                Text(tr(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = { confirmDismiss() }) {
                Text(tr(R.string.cancel))
            }
        }
    )

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(tr(R.string.discard_changes_title)) },
            text = { Text(tr(R.string.discard_changes_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDismiss()
                }) {
                    Text(tr(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(tr(R.string.cancel))
                }
            }
        )
    }
}
