package com.aria.assistant.commands.handlers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.aria.assistant.calendar.CalendarRepository
import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** Calendar Commands (sección 6 y 12). Fase 8: ya conectado a CalendarContract real. */
class CalendarCommandHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calendarRepository: CalendarRepository
) : CommandHandler {
    override val domain = "calendar"

    private val listTriggers = listOf(
        "mis eventos", "próximos eventos", "proximos eventos",
        "qué tengo en el calendario", "que tengo en el calendario"
    )
    private val createTriggers = listOf("crea un evento:", "crea un evento", "agenda un evento:", "agenda un evento")

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return listTriggers.any { normalized.contains(it) } || createTriggers.any { normalized.startsWith(it) }
    }

    override suspend fun execute(input: String): CommandResult {
        val normalized = input.trim().lowercase()

        if (listTriggers.any { normalized.contains(it) }) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                return CommandResult.NeedsPermission(Manifest.permission.READ_CALENDAR)
            }

            val events = calendarRepository.upcomingEvents(limit = 5)
            return if (events.isEmpty()) {
                CommandResult.Success("No tienes eventos próximos en tu calendario.")
            } else {
                val formatter = SimpleDateFormat("EEEE d 'a las' HH:mm", Locale("es", "ES"))
                val list = events.joinToString("; ") { "${it.title} el ${formatter.format(Date(it.startMillis))}" }
                CommandResult.Success("Tus próximos eventos: $list.")
            }
        }

        val createTrigger = createTriggers.firstOrNull { normalized.startsWith(it) }
        if (createTrigger != null) {
            val title = input.trim().substring(createTrigger.length).trim().removePrefix(":").trim()
            if (title.isBlank()) return CommandResult.Failure("¿Qué título quieres para el evento?")
            return try {
                context.startActivity(calendarRepository.createEventIntent(title))
                CommandResult.Success("Abriendo el Calendario para crear \"$title\". Termina de completarlo ahí.")
            } catch (e: Exception) {
                CommandResult.Failure("No pude abrir la app de Calendario.")
            }
        }

        return CommandResult.Failure("No entendí bien qué querías hacer con el calendario.")
    }
}
