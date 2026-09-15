package com.aria.assistant.ui.screens.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aria.assistant.reminders.ReminderEntity
import com.aria.assistant.ui.components.AriaScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Recordatorios (sección 11). Fase 8: AlarmManager + notificación real. */
@Composable
fun RemindersScreen(onBack: (() -> Unit)? = null, viewModel: RemindersViewModel = hiltViewModel()) {
    val reminders by viewModel.reminders.collectAsState()
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotificationPermission = granted }

    var message by remember { mutableStateOf("") }
    var hourText by remember { mutableStateOf("") }
    var minuteText by remember { mutableStateOf("") }

    AriaScaffold(title = "Recordatorios", onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {

            if (!hasNotificationPermission) {
                PermissionBanner(
                    onRequest = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                )
            }

            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Mensaje del recordatorio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Hora (0-23)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
                        label = { Text("Minuto") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        val hour = hourText.toIntOrNull()
                        val minute = minuteText.toIntOrNull() ?: 0
                        if (message.isNotBlank() && hour != null && hour in 0..23 && minute in 0..59) {
                            viewModel.addReminder(message, hour, minute)
                            message = ""; hourText = ""; minuteText = ""
                        }
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Programar recordatorio", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (reminders.isEmpty()) {
                EmptyRemindersState(Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderRow(reminder = reminder, onCancel = { viewModel.cancel(reminder) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            "Sin permiso de notificaciones, tus recordatorios no podrán avisarte.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.TextButton(onClick = onRequest) {
            Text("Otorgar permiso")
        }
    }
}

@Composable
private fun EmptyRemindersState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(
            "No tienes recordatorios programados. Agrega uno arriba o dile a A.R.I.A. " +
                "\"recuérdame ... a las ...\".",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReminderRow(reminder: ReminderEntity, onCancel: () -> Unit) {
    val formatter = remember { SimpleDateFormat("EEEE d 'a las' HH:mm", Locale("es", "ES")) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(reminder.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                formatter.format(Date(reminder.triggerAtMillis)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onCancel) {
            Icon(Icons.Filled.Delete, contentDescription = "Cancelar recordatorio", tint = MaterialTheme.colorScheme.error)
        }
    }
}
