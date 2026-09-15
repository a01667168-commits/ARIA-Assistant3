package com.aria.assistant.commands.handlers

import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import com.aria.assistant.music.MusicController
import com.aria.assistant.music.MusicOpenResult
import javax.inject.Inject

/** Music Commands (sección 6 y 8). Fase 10: play/pausa/siguiente reales; "reproduce X" abre Spotify/YouTube. */
class MusicCommandHandler @Inject constructor(
    private val musicController: MusicController
) : CommandHandler {
    override val domain = "music"

    private val playTriggers = listOf("reproduce ", "pon ")
    private val pausePhrases = listOf("pausa", "pausar", "para la música", "para la musica")
    private val nextPhrases = listOf("siguiente canción", "siguiente cancion", "siguiente")
    private val previousPhrases = listOf("canción anterior", "cancion anterior", "anterior")

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return playTriggers.any { normalized.startsWith(it) } ||
            pausePhrases.any { normalized.contains(it) } ||
            nextPhrases.any { normalized.contains(it) } ||
            previousPhrases.any { normalized.contains(it) }
    }

    override suspend fun execute(input: String): CommandResult {
        val normalized = input.trim().lowercase()

        val playTrigger = playTriggers.firstOrNull { normalized.startsWith(it) }
        if (playTrigger != null) {
            val query = input.trim().substring(playTrigger.length).trim()
                .removePrefix("música de ").removePrefix("musica de ")
            if (query.isBlank()) return CommandResult.Failure("¿Qué quieres que reproduzca?")
            return when (val result = musicController.searchAndOpen(query)) {
                is MusicOpenResult.OpenedIn -> CommandResult.Success("Abriendo \"$query\" en ${result.appName}.")
                MusicOpenResult.Failed -> CommandResult.Failure("No pude abrir ninguna app de música para reproducir eso.")
            }
        }

        if (pausePhrases.any { normalized.contains(it) }) {
            musicController.playPause()
            return CommandResult.Success("Listo, pausé o reanudé la reproducción.")
        }
        if (nextPhrases.any { normalized.contains(it) }) {
            musicController.next()
            return CommandResult.Success("Pasando a la siguiente canción.")
        }
        if (previousPhrases.any { normalized.contains(it) }) {
            musicController.previous()
            return CommandResult.Success("Volviendo a la canción anterior.")
        }

        return CommandResult.Failure("No entendí bien qué querías hacer con la música.")
    }
}
