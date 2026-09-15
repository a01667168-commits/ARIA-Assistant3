package com.aria.assistant.ai.providers

import com.aria.assistant.ai.AiProvider
import com.aria.assistant.ai.AiResult
import com.aria.assistant.settings.AiProviderIds
import com.aria.assistant.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proveedor local sin IA generativa real: no requiere API key ni Internet.
 * Cumple el requisito de la sección 4 ("la aplicación debe poder funcionar
 * parcialmente sin una API externa") y sirve como respaldo automático del
 * AiEngine cuando el proveedor externo elegido falla o no está configurado.
 */
@Singleton
class LocalFallbackProvider @Inject constructor(
    private val settingsRepository: SettingsRepository
) : AiProvider {
    override val id = AiProviderIds.LOCAL
    override val displayName = "Local (sin IA generativa)"
    override val requiresApiKey = false

    override suspend fun generateResponse(prompt: String, conversationContext: List<String>): AiResult {
        val ariaName = settingsRepository.ariaName.first()
        return AiResult.Success(
            "Todavía no tengo un proveedor de IA generativa configurado, así que no puedo dar una " +
                "respuesta elaborada. Puedes conectar Claude, OpenAI o Gemini en Configuración. " +
                "Por ahora, esto es lo que entendí ($ariaName): \"$prompt\"."
        )
    }
}
