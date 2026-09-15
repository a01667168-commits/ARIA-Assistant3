package com.aria.assistant.commands.handlers

import com.aria.assistant.tasks.TaskPriority

/**
 * Lógica pura de parseo de "... como prioridad alta/media/baja", extraída
 * de TasksCommandHandler en la Fase 14 para poder testearla sin depender
 * de Room ni de ningún otro componente de Android.
 */
object TaskCommandParser {
    data class ParsedTask(val title: String, val priority: TaskPriority)

    /** Devuelve null si no queda ningún título después de quitar la frase de prioridad. */
    fun parse(rest: String): ParsedTask? {
        val priority = when {
            rest.contains("prioridad alta", ignoreCase = true) -> TaskPriority.ALTA
            rest.contains("prioridad baja", ignoreCase = true) -> TaskPriority.BAJA
            else -> TaskPriority.MEDIA
        }
        val title = rest
            .replace(Regex("(?i)como prioridad (alta|media|baja)"), "")
            .trim()
            .trimEnd('.')

        if (title.isBlank()) return null
        return ParsedTask(title, priority)
    }
}
