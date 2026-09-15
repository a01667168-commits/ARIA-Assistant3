package com.aria.assistant.commands.handlers

import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import com.aria.assistant.tasks.TaskRepository
import javax.inject.Inject

/**
 * Tasks Commands (sección 6 y 10). Fase 7: persistencia real en Room.
 * Fase 14: el parseo de prioridad vive en [TaskCommandParser] (lógica pura, testeada).
 */
class TasksCommandHandler @Inject constructor(
    private val taskRepository: TaskRepository
) : CommandHandler {
    override val domain = "tasks"

    private val createTriggers = listOf("agrega ", "crea una tarea:", "crea una tarea")
    private val completeTriggers = listOf("completa la tarea ", "completa ", "marca como hecha ")
    private val deleteTriggers = listOf("elimina la tarea ", "borra la tarea ")
    private val pendingPhrases = listOf("qué tengo pendiente", "que tengo pendiente", "mis tareas")

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return createTriggers.any { normalized.startsWith(it) } ||
            completeTriggers.any { normalized.startsWith(it) } ||
            deleteTriggers.any { normalized.startsWith(it) } ||
            pendingPhrases.any { normalized.contains(it) }
    }

    override suspend fun execute(input: String): CommandResult {
        val normalized = input.trim().lowercase()

        if (pendingPhrases.any { normalized.contains(it) }) {
            val pending = taskRepository.pendingTasks()
            return if (pending.isEmpty()) {
                CommandResult.Success("No tienes ninguna tarea pendiente. ¡Buen trabajo!")
            } else {
                val list = pending.take(5).joinToString(separator = ", ") { it.title }
                CommandResult.Success("Tienes ${pending.size} pendientes. Las próximas son: $list.")
            }
        }

        val deleteTrigger = deleteTriggers.firstOrNull { normalized.startsWith(it) }
        if (deleteTrigger != null) {
            val query = input.trim().substring(deleteTrigger.length).trim()
            val pending = taskRepository.pendingTasks()
            val match = pending.firstOrNull { it.title.lowercase().contains(query.lowercase()) }
            return if (match != null) {
                taskRepository.delete(match)
                CommandResult.Success("Eliminé la tarea \"$query\".")
            } else {
                CommandResult.Failure("No encontré ninguna tarea relacionada con \"$query\".")
            }
        }

        val completeTrigger = completeTriggers.firstOrNull { normalized.startsWith(it) }
        if (completeTrigger != null) {
            val query = input.trim().substring(completeTrigger.length).trim()
            return if (taskRepository.completeMatching(query)) {
                CommandResult.Success("Marqué \"$query\" como completada.")
            } else {
                CommandResult.Failure("No encontré ninguna tarea pendiente relacionada con \"$query\".")
            }
        }

        val createTrigger = createTriggers.firstOrNull { normalized.startsWith(it) }
        if (createTrigger != null) {
            val rest = input.trim().substring(createTrigger.length).trim().removePrefix(":").trim()
            val parsed = TaskCommandParser.parse(rest)
                ?: return CommandResult.Failure("¿Qué tarea quieres que agregue?")

            taskRepository.create(title = parsed.title, priority = parsed.priority)
            return CommandResult.Success(
                "Tarea agregada: ${parsed.title} (prioridad ${parsed.priority.name.lowercase()})."
            )
        }

        return CommandResult.Failure("No entendí bien qué querías hacer con tus tareas.")
    }
}
