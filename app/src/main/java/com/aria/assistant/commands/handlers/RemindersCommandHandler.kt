package com.aria.assistant.commands.handlers

import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import com.aria.assistant.reminders.ReminderRepository
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Reminder Commands (sección 6 y 11). Fase 8: AlarmManager + notificación real.
 * Fase 14: el parseo de la hora vive en [ReminderTimeParser] (lógica pura, testeada).
 */
class RemindersCommandHandler @Inject constructor(
    private val reminderRepository: ReminderRepository
) : CommandHandler {
    override val domain = "reminders"

    private val triggers = listOf("recuérdame ", "recuerdame ")

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return triggers.any { normalized.startsWith(it) }
    }

    override suspend fun execute(input: String): CommandResult {
        val normalized = input.trim().lowercase()
        val trigger = triggers.first { normalized.startsWith(it) }
        val rest = input.trim().substring(trigger.length).trim()

        val parsed = ReminderTimeParser.parse(rest)
            ?: return CommandResult.Failure(
                "Dime a qué hora, por ejemplo: \"recuérdame estudiar a las 8\"."
            )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parsed.hour)
            set(Calendar.MINUTE, parsed.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val exact = reminderRepository.create(parsed.message, calendar.timeInMillis)
        val timeText = String.format(Locale("es", "ES"), "%02d:%02d", parsed.hour, parsed.minute)

        return if (exact) {
            CommandResult.Success("Listo, te recordaré \"${parsed.message}\" a las $timeText.")
        } else {
            CommandResult.Success(
                "Programé el recordatorio \"${parsed.message}\" para las $timeText, pero como no " +
                    "tengo el permiso de alarmas exactas, podría sonar con algunos minutos de diferencia."
            )
        }
    }
}
