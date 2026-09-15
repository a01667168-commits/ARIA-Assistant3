package com.aria.assistant.commands.handlers

import com.aria.assistant.ai.AiEngine
import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import javax.inject.Inject

/**
 * AI Commands (sección 6): último recurso del CommandEngine. Si ningún
 * comando específico coincide, se trata el texto como conversación normal
 * y se delega al AiEngine — así "ARIA, ¿qué opinas de...?" sigue funcionando
 * aunque no sea un comando reconocido explícitamente.
 */
class AiFallbackHandler @Inject constructor(
    private val aiEngine: AiEngine
) : CommandHandler {
    override val domain = "ai"

    override fun canHandle(input: String): Boolean = true

    override suspend fun execute(input: String): CommandResult {
        val result = aiEngine.generateResponse(input)
        return CommandResult.Success(result.text)
    }
}
