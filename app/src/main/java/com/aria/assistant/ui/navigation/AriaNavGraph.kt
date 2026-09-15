package com.aria.assistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aria.assistant.ui.screens.calendar.CalendarScreen
import com.aria.assistant.ui.screens.dashboard.DashboardScreen
import com.aria.assistant.ui.screens.home.HomeScreen
import com.aria.assistant.ui.screens.memory.MemoryScreen
import com.aria.assistant.ui.screens.music.MusicScreen
import com.aria.assistant.ui.screens.notes.NotesScreen
import com.aria.assistant.ui.screens.reminders.RemindersScreen
import com.aria.assistant.ui.screens.settings.SettingsScreen
import com.aria.assistant.ui.screens.tasks.TasksScreen
import com.aria.assistant.ui.screens.weather.WeatherScreen

@Composable
fun AriaNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AriaDestination.Dashboard.route) {

        composable(AriaDestination.Dashboard.route) {
            DashboardScreen(
                onNavigate = { destination -> navController.navigate(destination.route) }
            )
        }
        composable(AriaDestination.Conversation.route) {
            HomeScreen(onBack = { navController.popBackStack() })
        }
        composable(AriaDestination.Notes.route) {
            NotesScreen(onBack = { navController.popBackStack() })
        }
        composable(AriaDestination.Tasks.route) {
            TasksScreen(onBack = { navController.popBackStack() })
        }
        composable(AriaDestination.Reminders.route) {
            RemindersScreen(onBack = { navController.popBackStack() })
        }
        composable(AriaDestination.Calendar.route) {
            CalendarScreen(onBack = { navController.popBackStack() })
        }
        composable(AriaDestination.Music.route) {
            MusicScreen(onBack = { navController.popBackStack() })
        }
        composable(AriaDestination.Weather.route) {
            WeatherScreen(onBack = { navController.popBackStack() })
        }
        composable(AriaDestination.Memory.route) {
            MemoryScreen(onBack = { navController.popBackStack() })
        }
        composable(AriaDestination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
