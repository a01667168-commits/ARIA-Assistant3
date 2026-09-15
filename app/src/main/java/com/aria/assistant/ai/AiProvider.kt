package com.aria.assistant.ai

/**
 * Contrato que debe cumplir cualquier proveedor de IA conectado a A.R.I.A.
 * (Claude, OpenAI, Gemini, un modelo local, o una API personalizada).
 *
 * Se implementa en la Fase 4 (AI Engine). Declarado aquí desde la Fase 1
 * para fijar la arquitectura: el resto de la app depende de esta interfaz,
 * nunca de un proveedor concreto.
 */
interface AiProvider {
    val id: String
    val displayName: String
    val requiresApiKey: Boolean

    suspend fun generateResponse(prompt: String, conversationContext: List<String>): AiResult
}

sealed class AiResult {
    data class Success(val text: String) : AiResult()
    data class Error(val reason: String) : AiResult()
    object RequiresConfiguration : AiResult()
}
