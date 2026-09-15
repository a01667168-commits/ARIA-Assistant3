package com.aria.assistant.commands.handlers

import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import com.aria.assistant.notes.NoteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Notes Commands (sección 6 y 9). Fase 7: ya con persistencia real en Room. */
class NotesCommandHandler @Inject constructor(
    private val noteRepository: NoteRepository
) : CommandHandler {
    override val domain = "notes"

    private val createTriggers = listOf("toma nota:", "toma nota", "crea una nota:", "crea una nota", "anota que", "anota:", "anota")
    private val deleteTriggers = listOf("elimina la nota sobre ", "borra la nota sobre ", "elimina la nota ", "borra la nota ")
    private val listPhrases = listOf("lee mis notas", "qué notas tengo", "que notas tengo", "mis notas")

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return createTriggers.any { normalized.startsWith(it) } ||
            deleteTriggers.any { normalized.startsWith(it) } ||
            listPhrases.any { normalized.contains(it) }
    }

    override suspend fun execute(input: String): CommandResult {
        val normalized = input.trim().lowercase()

        if (listPhrases.any { normalized.contains(it) }) {
            val notes = noteRepository.allNotes.first()
            return if (notes.isEmpty()) {
                CommandResult.Success("No tienes notas guardadas todavía.")
            } else {
                CommandResult.Success("Tienes ${notes.size} notas. La más reciente dice: \"${notes.first().content}\".")
            }
        }

        val deleteTrigger = deleteTriggers.firstOrNull { normalized.startsWith(it) }
        if (deleteTrigger != null) {
            val query = input.trim().substring(deleteTrigger.length).trim()
            return if (noteRepository.deleteMatching(query)) {
                CommandResult.Success("Eliminé la nota sobre \"$query\".")
            } else {
                CommandResult.Failure("No encontré ninguna nota relacionada con \"$query\".")
            }
        }

        val createTrigger = createTriggers.firstOrNull { normalized.startsWith(it) }
        if (createTrigger != null) {
            val content = input.trim().substring(createTrigger.length).trim().removePrefix(":").trim()
            if (content.isBlank()) return CommandResult.Failure("¿Qué quieres que anote en la nota?")
            noteRepository.create(content)
            return CommandResult.Success("Nota guardada: $content")
        }

        return CommandResult.Failure("No entendí bien qué querías hacer con tus notas.")
    }
}
