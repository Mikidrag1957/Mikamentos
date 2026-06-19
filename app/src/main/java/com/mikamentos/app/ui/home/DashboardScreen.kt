package com.mikamentos.app.ui.home
import com.mikamentos.app.ui.tr

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikamentos.app.R
import java.io.File


@Composable
fun DashboardScreen(
    onPlanificar: () -> Unit,
    onInstrucciones: () -> Unit,
    onCatalogo: () -> Unit,
    onPerfil: () -> Unit,
    onHistorial: () -> Unit,
    onAjustes: () -> Unit,
    onAgenda: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("mikamentos_prefs", 0)

    var showPinDialog by remember { mutableStateOf(false) }
    var showDevDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var donateUrlInput by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var versionTapCount by remember { mutableIntStateOf(0) }
    val versionLastTap = remember { mutableStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CatalanFlagButton(onClick = {})
                    FlagButton(flag = "\uD83C\uDDF8\uD83C\uDDF0", onClick = {})
                    FlagButton(flag = "\uD83C\uDDEA\uD83C\uDDF8", onClick = {})
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.pharmacy),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FlagButton(flag = "\uD83C\uDDEB\uD83C\uDDF7", onClick = {})
                    FlagButton(flag = "\uD83C\uDDEC\uD83C\uDDE7", onClick = {})
                    FlagButton(flag = "\uD83C\uDDEE\uD83C\uDDF9", onClick = {})
                }
            }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = tr(R.string.app_name),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Row 1: Usuario | Ajustes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GridButton(
                icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(28.dp)) },
                label = tr(R.string.menu_profile),
                onClick = onPerfil,
                modifier = Modifier.weight(1f)
            )
            GridButton(
                icon = { Icon(painter = painterResource(android.R.drawable.ic_menu_manage), contentDescription = null, modifier = Modifier.size(28.dp)) },
                label = tr(R.string.settings),
                onClick = onAjustes,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Medicamentos | Planificar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GridButton(
                icon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(28.dp)) },
                label = tr(R.string.menu_catalog),
                onClick = onCatalogo,
                modifier = Modifier.weight(1f)
            )
            GridButton(
                icon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(28.dp)) },
                label = tr(R.string.planificar),
                onClick = onPlanificar,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: Historial | Información
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GridButton(
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(28.dp)) },
                label = tr(R.string.menu_history),
                onClick = onHistorial,
                modifier = Modifier.weight(1f)
            )
            GridButton(
                icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(28.dp)) },
                label = tr(R.string.menu_info),
                onClick = onInstrucciones,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 4: Agenda | Compartir app
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GridButton(
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(28.dp)) },
                label = tr(R.string.agenda),
                onClick = onAgenda,
                labelColor = androidx.compose.ui.graphics.Color(0xFF00C853),
                modifier = Modifier.weight(1f)
            )
            GridButton(
                icon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(28.dp)) },
                label = tr(R.string.share_app),
                onClick = {
                    val downloadLink = "https://github.com/Mikidrag1957/Mikamentos/releases/latest/download/app-release.apk"
                    (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).let { clipboard ->
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Mikamentos APK", downloadLink))
                    }
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Te recomiendo Mikamentos, una app para recordar medicamentos: $downloadLink")
                    }.let { intent ->
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_app)))
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "v1.0.0",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val now = System.currentTimeMillis()
                    if (now - versionLastTap.value > 2000) {
                        versionTapCount = 1
                    } else {
                        versionTapCount++
                    }
                    versionLastTap.value = now
                    if (versionTapCount >= 4) {
                        versionTapCount = 0
                        pinInput = ""
                        pinError = false
                        showPinDialog = true
                    }
                }
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Developer PIN dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Access") },
            text = {
                Column {
                    Text(
                        text = "Enter PIN",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                pinInput = input
                                pinError = false
                            }
                        },
                        label = { Text("PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        supportingText = if (pinError) {{ Text("Wrong PIN") }} else null,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = pinInput.length == 4, onClick = {
                    val storedPin = prefs.getString("developer_pin", "0000") ?: "0000"
                    if (pinInput == storedPin) {
                        showPinDialog = false
                        donateUrlInput = prefs.getString("donate_url", "") ?: ""
                        newPin = storedPin
                        showDevDialog = true
                    } else {
                        pinError = true
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(tr(R.string.cancel))
                }
            }
        )
    }

    // Developer settings dialog
    if (showDevDialog) {
        AlertDialog(
            onDismissRequest = { showDevDialog = false },
            title = { Text(tr(R.string.paypal_title)) },
            text = {
                Column {
                    Text(
                        text = tr(R.string.paypal_hint),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = donateUrlInput,
                        onValueChange = { donateUrlInput = it },
                        label = { Text(tr(R.string.paypal_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Change PIN (4 digits)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                newPin = input
                            }
                        },
                        label = { Text("New PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val url = donateUrlInput.trim()
                    val editor = prefs.edit()
                    if (url.isNotBlank()) {
                        val fullUrl = if (url.startsWith("http")) {
                            url
                        } else if (url.contains("@")) {
                            "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=$url&currency_code=EUR"
                        } else {
                            "https://www.paypal.com/paypalme/$url"
                        }
                        editor.putString("donate_url", fullUrl)
                    }
                    if (newPin.length == 4) {
                        editor.putString("developer_pin", newPin)
                    }
                    editor.apply()
                    showDevDialog = false
                }) {
                    Text(tr(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDevDialog = false }) {
                    Text(tr(R.string.cancel))
                }
            }
        )
    }

}

@Composable
fun FlagButton(flag: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFE0E0E0))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = flag,
            fontSize = 20.sp
        )
    }
}

@Composable
fun CatalanFlagButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFFFCC00))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val stripeHeight = 36f / 9
            for (i in 0 until 9) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((stripeHeight).dp)
                        .background(if (i % 2 == 0) Color(0xFFFFCC00) else Color(0xFFDA121A))
                )
            }
        }
    }
}

@Composable
fun GridButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (labelColor != androidx.compose.ui.graphics.Color.Unspecified) labelColor else MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

