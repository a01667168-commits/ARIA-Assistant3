package com.aria.assistant.commands.handlers

/**
 * Lógica pura de parseo de "... a las H" / "... a las H:MM", extraída de
 * RemindersCommandHandler en la Fase 14 para poder testearla sin depender
 * de Room, AlarmManager ni ningún otro componente de Android.
 */
object ReminderTimeParser {
    private val timeRegex = Regex("""a las (\d{1,2})(?::(\d{2}))?""")

    data class ParsedReminder(val message: String, val hour: Int, val minute: Int)

    /** Devuelve null si no hay un patrón de hora reconocible, la hora es inválida, o no queda mensaje. */
    fun parse(rest: String): ParsedReminder? {
        val match = timeRegex.find(rest) ?: return null
        val hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: 0
        if (hour !in 0..23 || minute !in 0..59) return null

        val message = rest.substring(0, match.range.first).trim().trimEnd('.')
        if (message.isBlank()) return null

        return ParsedReminder(message, hour, minute)
    }
}
