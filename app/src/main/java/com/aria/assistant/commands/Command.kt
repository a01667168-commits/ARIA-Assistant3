package com.aria.assistant.commands

/**
 * Contrato base para cualquier comando interpretable por A.R.I.A.
 * Cada dominio (apps, música, notas, tareas, recordatorios, calendario,
 * clima, búsqueda, sistema, IA) implementa su propio CommandHandler.
 * El CommandEngine (Fase 5) enruta el texto reconocido hacia el primer
 * handler que declare poder manejarlo.
 */
interface CommandHandler {
    /** Nombre del dominio, solo para depuración/logs (ej. "apps", "clima"). */
    val domain: String

    fun canHandle(input: String): Boolean
    suspend fun execute(input: String): CommandResult
}

sealed class CommandResult {
    data class Success(val message: String) : CommandResult()
    data class Failure(val reason: String) : CommandResult()
    data class NeedsPermission(val permission: String) : CommandResult()
}
