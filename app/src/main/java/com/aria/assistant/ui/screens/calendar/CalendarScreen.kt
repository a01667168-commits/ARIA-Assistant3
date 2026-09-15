package com.aria.assistant.ui.screens.calendar

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import com.aria.assistant.calendar.CalendarEvent
import com.aria.assistant.ui.components.AriaScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Calendario (sección 12). Fase 8: eventos reales vía CalendarContract. */
@Composable
fun CalendarScreen(onBack: (() -> Unit)? = null, viewModel: CalendarViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val events by viewModel.events.collectAsState()
    var newEventTitle by remember { mutableStateOf("") }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.loadEvents()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.loadEvents()
    }

    AriaScaffold(title = "Calendario", onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                OutlinedTextField(
                    value = newEventTitle,
                    onValueChange = { newEventTitle = it },
                    label = { Text("Título del nuevo evento") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.IconButton(onClick = {
                    if (newEventTitle.isNotBlank()) {
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_INSERT,
                                android.provider.CalendarContract.Events.CONTENT_URI
                            ).apply {
                                putExtra(android.provider.CalendarContract.Events.TITLE, newEventTitle)
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                        newEventTitle = ""
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Crear evento", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (!hasPermission) {
                PermissionState(onRequest = { permissionLauncher.launch(Manifest.permission.READ_CALENDAR) })
            } else if (events.isEmpty()) {
                EmptyEventsState(Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(events, key = { it.id }) { event -> EventRow(event) }
                }
            }
        }
    }
}

@Composable
private fun PermissionState(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(
            "Necesito permiso para leer tu calendario y mostrarte tus próximos eventos.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRequest) { Text("Otorgar permiso") }
    }
}

@Composable
private fun EmptyEventsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(
            "No tienes eventos próximos en los siguientes 30 días.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EventRow(event: CalendarEvent) {
    val formatter = remember { SimpleDateFormat("EEEE d 'a las' HH:mm", Locale("es", "ES")) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(event.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            formatter.format(Date(event.startMillis)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
