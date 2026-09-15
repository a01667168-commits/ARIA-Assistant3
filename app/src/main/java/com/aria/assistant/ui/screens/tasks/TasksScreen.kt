package com.aria.assistant.ui.screens.tasks

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aria.assistant.tasks.TaskEntity
import com.aria.assistant.tasks.TaskPriority
import com.aria.assistant.ui.components.AriaScaffold
import com.aria.assistant.ui.theme.AriaError
import com.aria.assistant.ui.theme.AriaSuccess

/** Tareas (sección 10). Fase 7: crear (con prioridad), completar y eliminar ya son reales (Room). */
@Composable
fun TasksScreen(onBack: (() -> Unit)? = null, viewModel: TasksViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    var newTaskText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIA) }
    var menuExpanded by remember { mutableStateOf(false) }

    AriaScaffold(title = "Tareas", onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    label = { Text("Nueva tarea") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    TextButton(onClick = { menuExpanded = true }) {
                        Text(selectedPriority.name.lowercase())
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        TaskPriority.entries.forEach { priority ->
                            DropdownMenuItem(
                                text = { Text(priority.name.lowercase()) },
                                onClick = { selectedPriority = priority; menuExpanded = false }
                            )
                        }
                    }
                }
                IconButton(onClick = {
                    viewModel.addTask(newTaskText, selectedPriority)
                    newTaskText = ""
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar tarea", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (tasks.isEmpty()) {
                EmptyTasksState(Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(task = task, onToggle = { viewModel.toggleDone(task) }, onDelete = { viewModel.delete(task) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.TaskAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No tienes tareas todavía. Agrega una arriba o dile a A.R.I.A. \"agrega ... como prioridad alta\".",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TaskRow(task: TaskEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    val priorityColor = when (task.priority) {
        TaskPriority.ALTA -> AriaError
        TaskPriority.MEDIA -> MaterialTheme.colorScheme.primary
        TaskPriority.BAJA -> AriaSuccess
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Prioridad ${task.priority.name.lowercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = priorityColor
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar tarea", tint = MaterialTheme.colorScheme.error)
        }
    }
}
