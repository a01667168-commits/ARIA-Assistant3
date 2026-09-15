package com.aria.assistant.commands.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Search Commands (sección 6): "ARIA busca información sobre...". */
class SearchCommandHandler @Inject constructor(
    @ApplicationContext private val context: Context
) : CommandHandler {
    override val domain = "search"

    private val triggers = listOf("busca información sobre ", "busca ", "buscar información sobre ", "buscar ")

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return triggers.any { normalized.startsWith(it) }
    }

    override suspend fun execute(input: String): CommandResult {
        val normalized = input.trim().lowercase()
        val trigger = triggers.first { normalized.startsWith(it) }
        val query = input.trim().substring(trigger.length)

        return try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            CommandResult.Success("Buscando \"$query\" en el navegador.")
        } catch (e: Exception) {
            CommandResult.Failure("No pude abrir el navegador para buscar eso.")
        }
    }
}
