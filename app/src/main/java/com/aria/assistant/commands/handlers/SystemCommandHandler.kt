package com.aria.assistant.commands.handlers

import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** System Commands (sección 6): consultas locales que no requieren nada externo. */
class SystemCommandHandler @Inject constructor() : CommandHandler {
    override val domain = "system"

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return normalized.contains("qué hora es") || normalized.contains("que hora es") ||
            normalized.contains("qué día es") || normalized.contains("que dia es")
    }

    override suspend fun execute(input: String): CommandResult {
        val normalized = input.trim().lowercase()
        return if (normalized.contains("hora")) {
            val time = SimpleDateFormat("HH:mm", Locale("es", "ES")).format(Date())
            CommandResult.Success("Son las $time.")
        } else {
            val date = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES")).format(Date())
            CommandResult.Success("Hoy es $date.")
        }
    }
}
