package com.aria.assistant.ui.screens.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aria.assistant.memory.MemoryEntity
import com.aria.assistant.ui.components.AriaScaffold

/**
 * Memoria (sección 5): ver, editar (borrar y volver a crear, por ahora —
 * la edición in-place puede añadirse después sin cambiar la arquitectura)
 * y eliminar recuerdos. Ya conectado a Room desde la Fase 1/6.
 */
@Composable
fun MemoryScreen(onBack: (() -> Unit)? = null, viewModel: MemoryViewModel = hiltViewModel()) {
    val memories by viewModel.memories.collectAsState()

    AriaScaffold(title = "Memoria", onBack = onBack) { padding ->
        if (memories.isEmpty()) {
            EmptyMemoryState(padding)
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(memories, key = { it.id }) { memory ->
                        MemoryRow(memory = memory, onDelete = { viewModel.delete(memory) })
                    }
                }
                TextButton(
                    onClick = { viewModel.deleteAll() },
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                ) {
                    Text("Borrar todos los recuerdos", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun EmptyMemoryState(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Todavía no tengo nada guardado. Dile a A.R.I.A. \"recuerda que...\" y aparecerá aquí.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MemoryRow(memory: MemoryEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = memory.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar recuerdo", tint = MaterialTheme.colorScheme.error)
        }
    }
}
