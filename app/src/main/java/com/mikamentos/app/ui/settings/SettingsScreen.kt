package com.mikamentos.app.ui.settings
import com.mikamentos.app.ui.tr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikamentos.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun getLanguageName(code: String): String {
    return when (code) {
        "es" -> "Español"
        "en" -> "English"
        "sk" -> "Slovenčina"
        "ca" -> "Català"
        "fr" -> "Français"
        "it" -> "Italiano"
        else -> code
    }
}

private fun restartActivity(context: Context) {
    (context as? Activity)?.recreate()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }

    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.getParcelableExtra<android.net.Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        if (uri != null) {
            val name = RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: uri.toString()
            viewModel.updateAlarmRingtone(uri.toString(), name)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(R.string.settings)) },
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
                text = tr(R.string.select_language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showLanguageDialog = true },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getLanguageName(settings.language),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = tr(R.string.settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val themeOptions = listOf(0 to tr(R.string.theme_system), 1 to tr(R.string.theme_light), 2 to tr(R.string.theme_dark))
            themeOptions.forEach { (mode, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        viewModel.updateThemeMode(mode)
                        restartActivity(context)
                    }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    if (settings.themeMode == mode) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = tr(R.string.alarm_sound),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val ringtoneTitle = tr(R.string.select_ringtone)
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, ringtoneTitle)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                            if (settings.alarmRingtoneUri.isNotBlank()) android.net.Uri.parse(settings.alarmRingtoneUri)
                            else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    }
                    ringtoneLauncher.launch(intent)
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (settings.alarmRingtoneName.isNotBlank()) settings.alarmRingtoneName else tr(R.string.default_ringtone),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = tr(R.string.data_management),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val scope = rememberCoroutineScope()
            var showImportDialog by remember { mutableStateOf(false) }

            val importLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    scope.launch {
                        val importSuccessText = context.getString(R.string.import_success)
                        val importErrorText = context.getString(R.string.import_error)
                        try {
                            val json = withContext(Dispatchers.IO) {
                                context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                            }
                            val success = viewModel.importAllData(json)
                            if (success) {
                                Toast.makeText(context, importSuccessText, Toast.LENGTH_LONG).show()
                                restartActivity(context)
                            } else {
                                Toast.makeText(context, importErrorText, Toast.LENGTH_LONG).show()
                            }
                        } catch (_: Exception) {
                            Toast.makeText(context, importErrorText, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            val exportLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                if (uri != null) {
                    scope.launch {
                        val exportSuccessText = context.getString(R.string.export_success)
                        val importErrorText = context.getString(R.string.import_error)
                        try {
                            val json = viewModel.exportAllData()
                            withContext(Dispatchers.IO) {
                                context.contentResolver.openOutputStream(uri)?.use {
                                    it.write(json.toByteArray())
                                }
                            }
                            Toast.makeText(context, exportSuccessText, Toast.LENGTH_LONG).show()
                        } catch (_: Exception) {
                            Toast.makeText(context, importErrorText, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            Button(
                onClick = { exportLauncher.launch("mikamentos_backup.json") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(tr(R.string.export_data))
            }

            Button(
                onClick = { showImportDialog = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(tr(R.string.import_data))
            }

            if (showImportDialog) {
                AlertDialog(
                    onDismissRequest = { showImportDialog = false },
                    title = { Text(tr(R.string.confirm_import_title)) },
                    text = { Text(tr(R.string.confirm_import_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showImportDialog = false
                            importLauncher.launch("application/json")
                        }) {
                            Text(tr(R.string.save))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text(tr(R.string.cancel))
                        }
                    }
                )
            }

        }
    }

    if (showLanguageDialog) {
        val languages = listOf("es", "en", "sk", "ca", "fr", "it")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(tr(R.string.select_language)) },
            text = {
                Column {
                    languages.forEach { code ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                viewModel.updateLanguage(code)
                                showLanguageDialog = false
                                restartActivity(context)
                            }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = getLanguageName(code), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            if (code == settings.language) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(tr(R.string.close))
                }
            }
        )
    }
}

