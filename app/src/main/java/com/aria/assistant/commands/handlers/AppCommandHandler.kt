package com.aria.assistant.commands.handlers

import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import com.aria.assistant.system.AppLauncher
import com.aria.assistant.system.LaunchResult
import javax.inject.Inject

/** App Commands (sección 6 y 7): "ARIA abre Spotify", "ARIA abre configuración", etc. */
class AppCommandHandler @Inject constructor(
    private val appLauncher: AppLauncher
) : CommandHandler {
    override val domain = "apps"

    private val triggers = listOf("abre ", "abrir ", "ábreme ", "abreme ")

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return triggers.any { normalized.startsWith(it) }
    }

    override suspend fun execute(input: String): CommandResult {
        val normalized = input.trim().lowercase()
        val trigger = triggers.first { normalized.startsWith(it) }
        val appName = input.trim().substring(trigger.length)

        return when (val result = appLauncher.launch(appName)) {
            is LaunchResult.Success -> CommandResult.Success(result.message)
            is LaunchResult.NotInstalled -> CommandResult.Failure(
                "No encontré ${result.appName} instalada en tu teléfono."
            )
            is LaunchResult.NotLaunchable -> CommandResult.Failure(
                "Encontré ${result.appName} instalada, pero Android no me deja abrirla directamente " +
                    "(no tiene una pantalla propia, es un componente en segundo plano). Puedes abrirla " +
                    "manualmente desde el cajón de aplicaciones."
            )
            is LaunchResult.Unknown -> CommandResult.Failure(
                "No encontré ninguna app llamada \"${result.appName}\" en tu teléfono."
            )
        }
    }
}
