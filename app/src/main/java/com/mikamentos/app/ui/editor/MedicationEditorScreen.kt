package com.mikamentos.app.ui.editor
import com.mikamentos.app.ui.tr

import android.app.TimePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikamentos.app.R
import com.mikamentos.app.data.model.CatalogMedication
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationEditorScreen(
    medicationId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: MedicationEditorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val catalogMedications by viewModel.catalogMedications.collectAsState()
    val selectedCatalogMed by viewModel.selectedCatalogMed.collectAsState()
    val hour by viewModel.hour.collectAsState()
    val minute by viewModel.minute.collectAsState()
    val daysOfWeek by viewModel.daysOfWeek.collectAsState()
    val repetitions by viewModel.repetitions.collectAsState()
    val repetitionMinutes by viewModel.repetitionMinutes.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val photoPath by viewModel.photoPath.collectAsState()
    val profileError by viewModel.profileError.collectAsState()

    var showDaysDialog by remember { mutableStateOf(false) }
    var showCatalogPicker by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var notesDraft by remember { mutableStateOf("") }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val profileErrorDialog = remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraUri != null) {
            val saved = copyImageToStorage(context, cameraUri!!)
            viewModel.updatePhotoPath(saved)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val saved = copyImageToStorage(context, uri)
            viewModel.updatePhotoPath(saved)
        }
    }

    LaunchedEffect(profileError) {
        if (profileError != null) {
            profileErrorDialog.value = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(tr(R.string.edit_schedule))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.save(
                                onSuccess = onNavigateBack,
                                onProfileRequired = { profileErrorDialog.value = true }
                            )
                        },
                        enabled = selectedCatalogMed != null
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = tr(R.string.save),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (medicationId != null && selectedCatalogMed != null) {
                Text(
                    text = selectedCatalogMed!!.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedButton(
                    onClick = { showCatalogPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = selectedCatalogMed?.title ?: tr(R.string.select_from_catalog),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Foto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = {
                        val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                        file.parentFile?.mkdirs()
                        cameraUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        cameraLauncher.launch(cameraUri!!)
                    },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Cámara", maxLines = 1, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Galería", maxLines = 1, fontSize = 12.sp)
                }
            }

            if (photoPath != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val bitmap = remember(photoPath) { BitmapFactory.decodeFile(photoPath) }
                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .width(190.dp)
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .align(Alignment.End)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr(R.string.time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = String.format("%02d:%02d", hour, minute),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                viewModel.updateHour(h)
                                viewModel.updateMinute(m)
                            },
                            hour,
                            minute,
                            true
                        ).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dia/Semana",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
                dayLabels.forEachIndexed { index, label ->
                    val isEnabled = daysOfWeek.getOrNull(index) == true
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isEnabled) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { showDaysDialog = true }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = tr(R.string.edit_days),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = tr(R.string.repetitions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tr(R.string.repetitions_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tr(R.string.times),
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "-",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { if (repetitions > 1) viewModel.updateRepetitions(repetitions - 1) }
                            .padding(12.dp)
                    )
                    Text(
                        text = "$repetitions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { if (repetitions < 10) viewModel.updateRepetitions(repetitions + 1) }
                            .padding(12.dp)
                    )
                }
            }

            if (repetitions > 1) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tr(R.string.minutes_between),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "-",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { if (repetitionMinutes > 1) viewModel.updateRepetitionMinutes(repetitionMinutes - 1) }
                                .padding(12.dp)
                        )
                        Text(
                            text = "$repetitionMinutes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text(
                            text = "+",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { if (repetitionMinutes < 30) viewModel.updateRepetitionMinutes(repetitionMinutes + 1) }
                                .padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        notesDraft = notes
                        showNotesDialog = true
                    }
            ) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { },
                    label = { Text(tr(R.string.notes_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    minLines = 3,
                    maxLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }

        }
    }

    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Notas") },
            text = {
                OutlinedTextField(
                    value = notesDraft,
                    onValueChange = { notesDraft = it },
                    label = { Text(tr(R.string.notes_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateNotes(notesDraft)
                    showNotesDialog = false
                }) {
                    Text(tr(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text(tr(R.string.cancel))
                }
            }
        )
    }

    if (showDaysDialog) {
        val dayNames = listOf(
            tr(R.string.monday),
            tr(R.string.tuesday),
            tr(R.string.wednesday),
            tr(R.string.thursday),
            tr(R.string.friday),
            tr(R.string.saturday),
            tr(R.string.sunday)
        )

        AlertDialog(
            onDismissRequest = { showDaysDialog = false },
            title = { Text(tr(R.string.edit_days)) },
            text = {
                Column {
                    dayNames.forEachIndexed { index, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateDay(index, !(daysOfWeek.getOrNull(index) ?: true))
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = daysOfWeek.getOrNull(index) == true,
                                onCheckedChange = { viewModel.updateDay(index, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDaysDialog = false }) {
                    Text(tr(R.string.close))
                }
            }
        )
    }

    if (showCatalogPicker) {
        AlertDialog(
            onDismissRequest = { showCatalogPicker = false },
            title = { Text(tr(R.string.select_from_catalog)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    if (catalogMedications.isEmpty()) {
                        Text(
                            text = tr(R.string.no_catalog_medications),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        catalogMedications.forEach { med ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectCatalogMedication(med)
                                        showCatalogPicker = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedCatalogMed?.id == med.id) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = med.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (med.description.isNotEmpty()) {
                                        Text(
                                            text = med.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCatalogPicker = false }) {
                    Text(tr(R.string.close))
                }
            }
        )
    }

    if (profileErrorDialog.value) {
        AlertDialog(
            onDismissRequest = {
                profileErrorDialog.value = false
                viewModel.clearProfileError()
            },
            title = { Text(tr(R.string.profile_required_title)) },
            text = { Text(tr(R.string.profile_required_message)) },
            confirmButton = {
                TextButton(onClick = {
                    profileErrorDialog.value = false
                    viewModel.clearProfileError()
                    onNavigateToSettings()
                }) {
                    Text(tr(R.string.settings))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    profileErrorDialog.value = false
                    viewModel.clearProfileError()
                }) {
                    Text(tr(R.string.close))
                }
            }
        )
    }
}

private fun copyImageToStorage(context: android.content.Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "photos")
        dir.mkdirs()
        val file = File(dir, "${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

