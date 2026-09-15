package com.aria.assistant.commands.handlers

import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import com.aria.assistant.memory.MemoryRepository
import com.aria.assistant.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Memory Commands (sección 5). Fase 12: respeta el interruptor de privacidad "Memoria activa". */
class MemoryCommandHandler @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val settingsRepository: SettingsRepository
) : CommandHandler {
    override val domain = "memory"

    private val rememberTriggers = listOf("recuerda que ", "recuerda ")
    private val forgetTriggers = listOf("olvida lo que te dije sobre ", "olvida que ", "olvida ")
    private val recallPhrases = listOf("qué recuerdas de mí", "que recuerdas de mi", "qué recuerdas de mi")
    private val forgetAllPhrases = listOf("olvida todo", "borra todos mis recuerdos", "borra toda tu memoria")

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return rememberTriggers.any { normalized.startsWith(it) } ||
            forgetTriggers.any { normalized.startsWith(it) } ||
            recallPhrases.any { normalized.contains(it) } ||
            forgetAllPhrases.any { normalized.contains(it) }
    }

    override suspend fun execute(input: String): CommandResult {
        val normalized = input.trim().lowercase()

        if (forgetAllPhrases.any { normalized.contains(it) }) {
            memoryRepository.forgetAll()
            return CommandResult.Success("Borré todo lo que recordaba de ti.")
        }

        if (recallPhrases.any { normalized.contains(it) }) {
            val memories = memoryRepository.allMemories.first()
            return if (memories.isEmpty()) {
                CommandResult.Success("Todavía no tengo nada guardado sobre ti.")
            } else {
                val list = memories.joinToString(separator = ". ") { it.content }
                CommandResult.Success("Esto es lo que recuerdo de ti: $list.")
            }
        }

        val rememberTrigger = rememberTriggers.firstOrNull { normalized.startsWith(it) }
        if (rememberTrigger != null) {
            if (!settingsRepository.memoryEnabled.first()) {
                return CommandResult.Failure(
                    "Tienes la memoria desactivada en Configuración, así que no puedo guardar eso. " +
                        "Actívala en Configuración > Memoria activa si quieres que recuerde cosas."
                )
            }
            val content = input.trim().substring(rememberTrigger.length).trim()
            if (content.isBlank()) return CommandResult.Failure("¿Qué quieres que recuerde exactamente?")
            memoryRepository.remember(content)
            return CommandResult.Success("Listo, lo voy a recordar: $content")
        }

        val forgetTrigger = forgetTriggers.firstOrNull { normalized.startsWith(it) }
        if (forgetTrigger != null) {
            val query = input.trim().substring(forgetTrigger.length).trim()
            val removed = memoryRepository.forgetMatching(query)
            return if (removed > 0) {
                CommandResult.Success("Olvidé lo que tenía guardado sobre \"$query\".")
            } else {
                CommandResult.Failure("No encontré ningún recuerdo relacionado con \"$query\".")
            }
        }

        return CommandResult.Failure("No entendí bien qué querías que recordara u olvidara.")
    }
}
