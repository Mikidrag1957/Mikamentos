package com.mikamentos.app.ui.catalog

import com.mikamentos.app.ui.tr

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikamentos.app.R
import com.mikamentos.app.data.model.CatalogMedication

data class CatalogItem(val medication: CatalogMedication, val displayDescription: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationCatalogScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicationCatalogViewModel = hiltViewModel()
) {
    val catalogMedications by viewModel.catalogMedications.collectAsState()
    val deleteError by viewModel.showDeleteError.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val translatedDescriptions by viewModel.translatedDescriptions.collectAsState()
    val translationVersion by viewModel.translationVersion.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val catalogItems = remember(catalogMedications, translatedDescriptions, translationVersion) {
        catalogMedications.map { med ->
            CatalogItem(
                medication = med,
                displayDescription = translatedDescriptions[med.id] ?: med.description
            )
        }
    }

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingMed by remember { mutableStateOf<CatalogMedication?>(null) }
    var dialogTitle by rememberSaveable { mutableStateOf("") }
    var dialogDescription by rememberSaveable { mutableStateOf("") }
    var medicationToDelete by remember { mutableStateOf<CatalogMedication?>(null) }
    var showFullDescription by remember { mutableStateOf<CatalogItem?>(null) }

    val deleteErrorDialog = remember { mutableStateOf(false) }

    LaunchedEffect(deleteError) {
        if (deleteError != null) deleteErrorDialog.value = true
    }

    if (showFullDescription != null) {
        MedicationFullDescription(
            item = showFullDescription!!,
            onClose = { showFullDescription = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(R.string.catalog)) },
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
                        onClick = { viewModel.loadTranslations() },
                        enabled = !isTranslating
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = tr(R.string.refresh), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingMed = null
                    dialogTitle = ""
                    dialogDescription = ""
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = tr(R.string.add_medication))
            }
        }
    ) { paddingValues ->
        if (catalogItems.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tr(R.string.no_catalog_medications),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(catalogItems, key = { it.medication.id }) { item ->
                    CatalogMedCard(
                        medication = item.medication,
                        displayDescription = item.displayDescription,
                        onRead = { showFullDescription = item },
                        onEdit = {
                            editingMed = item.medication
                            dialogTitle = item.medication.title
                            dialogDescription = item.displayDescription
                            showDialog = true
                        },
                        onDelete = { medicationToDelete = item.medication }
                    )
                }
            }
        }
    }

    if (showDialog) {
        CatalogMedDialog(
            title = dialogTitle,
            onTitleChange = { dialogTitle = it },
            description = dialogDescription,
            onDescriptionChange = { dialogDescription = it },
            isEditing = editingMed != null,
            isSearching = isSearching,
            onDescriptionListen = {
                val lang = settings.language
                viewModel.speakDescription(dialogDescription, lang)
            },
            onSearchDescription = {
                viewModel.searchDrugDescription(dialogTitle) { description ->
                    dialogDescription = description
                }
            },
            onDismiss = {
                viewModel.stopSpeaking()
                showDialog = false
            },
            onSave = {
                val name = dialogTitle.trim()
                if (name.isBlank()) return@CatalogMedDialog
                if (editingMed != null) {
                    viewModel.updateCatalogMedication(editingMed!!, dialogTitle, dialogDescription)
                } else {
                    viewModel.addCatalogMedication(dialogTitle, dialogDescription)
                }
                showDialog = false
            }
        )
    }

    if (deleteErrorDialog.value) {
        AlertDialog(
            onDismissRequest = {
                deleteErrorDialog.value = false
                viewModel.clearDeleteError()
            },
            title = { Text(tr(R.string.delete)) },
            text = { Text(tr(R.string.cannot_delete_scheduled)) },
            confirmButton = {
                TextButton(onClick = {
                    deleteErrorDialog.value = false
                    viewModel.clearDeleteError()
                }) {
                    Text(tr(R.string.close))
                }
            }
        )
    }

    if (medicationToDelete != null) {
        AlertDialog(
            onDismissRequest = { medicationToDelete = null },
            title = { Text(tr(R.string.confirm_delete_title)) },
            text = { Text(String.format(tr(R.string.confirm_delete_message), medicationToDelete!!.title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCatalogMedication(medicationToDelete!!)
                    medicationToDelete = null
                }) {
                    Text(tr(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicationToDelete = null }) {
                    Text(tr(R.string.cancel))
                }
            }
        )
    }

    if (searchError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSearchError() },
            title = { Text(tr(R.string.search_error_title)) },
            text = { Text(searchError ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSearchError() }) {
                    Text(tr(R.string.close))
                }
            }
        )
    }
}

@Composable
fun CatalogMedCard(
    medication: CatalogMedication,
    displayDescription: String = "",
    onRead: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val principioActivo = displayDescription.split("\n").firstOrNull { it.startsWith("Principio activo:") } ?: ""
    val dosis = displayDescription.split("\n").firstOrNull { it.startsWith("Dosis:") } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (displayDescription.isNotEmpty()) onRead()
                }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Search, contentDescription = tr(R.string.read), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = tr(R.string.edit), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = tr(R.string.delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = medication.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (principioActivo.isNotEmpty() || dosis.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                if (principioActivo.isNotEmpty()) {
                    Text(
                        text = principioActivo,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                if (dosis.isNotEmpty()) {
                    Text(
                        text = dosis,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationFullDescription(
    item: CatalogItem,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.medication.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr(R.string.back))
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = item.displayDescription.ifEmpty { tr(R.string.no_description) },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun CatalogMedDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isEditing: Boolean,
    isSearching: Boolean,
    onDescriptionListen: () -> Unit,
    onSearchDescription: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val titleFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditing) tr(R.string.edit_catalog_medication)
                else tr(R.string.new_catalog_medication)
            )
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text(tr(R.string.catalog_name_hint)) },
                        modifier = Modifier.weight(1f).focusRequester(titleFocusRequester),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSearchDescription,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() && !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (isSearching) tr(R.string.searching)
                               else tr(R.string.search_description)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = { Text(tr(R.string.catalog_description_hint)) },
                        modifier = Modifier.weight(1f),
                        minLines = 4,
                        maxLines = 10,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(onClick = onDescriptionListen) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = tr(R.string.listen), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = title.isNotBlank()
            ) {
                Text(tr(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr(R.string.cancel))
            }
        }
    )
}
