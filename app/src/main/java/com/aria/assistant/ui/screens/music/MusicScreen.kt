package com.aria.assistant.ui.screens.music

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aria.assistant.ui.components.AriaScaffold

/** Música (sección 8). Fase 10: control real de reproducción + búsqueda/apertura. */
@Composable
fun MusicScreen(onBack: (() -> Unit)? = null, viewModel: MusicViewModel = hiltViewModel()) {
    var query by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf<String?>(null) }

    AriaScaffold(title = "Música", onBack = onBack) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.LibraryMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(
                "Estos controles hablan con la app de música que tengas abierta (Spotify, YouTube " +
                    "Music, etc.) mediante eventos de medios del sistema.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            Row(horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.previous() }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Anterior", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = { viewModel.playPause() }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Reproducir / pausar", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = { viewModel.next() }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Siguiente", tint = MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar canción o artista") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Button(
                onClick = {
                    if (query.isNotBlank()) {
                        val result = viewModel.search(query)
                        statusText = when (result) {
                            is com.aria.assistant.music.MusicOpenResult.OpenedIn -> "Abriendo en ${result.appName}."
                            com.aria.assistant.music.MusicOpenResult.Failed -> "No pude abrir ninguna app de música."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buscar y abrir")
            }

            statusText?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
