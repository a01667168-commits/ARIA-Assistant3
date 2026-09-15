package com.aria.assistant.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Todas las rutas de navegación de A.R.I.A. Un único lugar como fuente
 * de verdad, usado tanto por el NavHost como por el Dashboard (accesos).
 */
sealed class AriaDestination(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : AriaDestination("dashboard", "Panel", Icons.Filled.Dashboard)
    object Conversation : AriaDestination("conversation", "Conversación", Icons.Filled.Chat)
    object Notes : AriaDestination("notes", "Notas", Icons.Filled.EditNote)
    object Tasks : AriaDestination("tasks", "Tareas", Icons.Filled.TaskAlt)
    object Reminders : AriaDestination("reminders", "Recordatorios", Icons.Filled.Notifications)
    object Calendar : AriaDestination("calendar", "Calendario", Icons.Filled.CalendarMonth)
    object Music : AriaDestination("music", "Música", Icons.Filled.LibraryMusic)
    object Weather : AriaDestination("weather", "Clima", Icons.Filled.Cloud)
    object Memory : AriaDestination("memory", "Memoria", Icons.Filled.Memory)
    object Settings : AriaDestination("settings", "Configuración", Icons.Filled.Settings)

    companion object {
        val dashboardShortcuts = listOf(
            Conversation, Notes, Tasks, Reminders, Calendar, Music, Weather, Memory, Settings
        )
    }
}
