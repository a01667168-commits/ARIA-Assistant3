package com.aria.assistant.commands

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enruta el texto reconocido por voz (o escrito) hacia el primer
 * CommandHandler que declare poder manejarlo (sección 6 del proyecto).
 * El AiFallbackHandler ("ai") siempre puede manejar cualquier texto, así
 * que se evalúa al final, nunca antes que un comando específico.
 */
@Singleton
class CommandEngine @Inject constructor(
    handlers: Set<@JvmSuppressWildcards CommandHandler>
) {
    private val orderedHandlers: List<CommandHandler> =
        handlers.sortedBy { it.domain == "ai" } // false antes que true: específicos primero

    suspend fun process(input: String): CommandResult {
        val handler = orderedHandlers.firstOrNull { it.canHandle(input) }
            ?: return CommandResult.Failure("No reconocí ningún comando ni tengo forma de responder a eso.")
        return handler.execute(input)
    }
}
