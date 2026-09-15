package com.aria.assistant.ai

import com.aria.assistant.settings.AiProviderIds
import com.aria.assistant.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Capa AI Engine (sección 4 del proyecto). Ningún otro módulo habla
 * directamente con un proveedor concreto: todos pasan por aquí, que:
 *  1. Lee el proveedor elegido por el usuario en Configuración.
 *  2. Si requiere API key y no está configurada, avisa claramente en vez
 *     de fallar en silencio (sección 4: "informar cuando una función
 *     necesita Internet o una API").
 *  3. Si el proveedor externo falla (sin red, error del servidor), cae
 *     automáticamente al proveedor local para no dejar a A.R.I.A. muda.
 */
@Singleton
class AiEngine @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val providers: Map<String, @JvmSuppressWildcards AiProvider>
) {
    suspend fun generateResponse(
        prompt: String,
        conversationContext: List<String> = emptyList()
    ): AiEngineResult {
        val selectedId = settingsRepository.selectedProviderId.first()
        val provider = providers[selectedId] ?: providers.getValue(AiProviderIds.LOCAL)

        return when (val result = provider.generateResponse(prompt, conversationContext)) {
            is AiResult.Success -> AiEngineResult(
                text = result.text,
                usedProviderId = provider.id,
                wasFallback = false
            )

            is AiResult.RequiresConfiguration -> AiEngineResult(
                text = "Todavía no configuraste una clave para ${provider.displayName}. " +
                    "Ve a Configuración > Proveedor de IA para agregarla, o elige el proveedor local.",
                usedProviderId = AiProviderIds.LOCAL,
                wasFallback = true
            )

            is AiResult.Error -> {
                val fallback = providers.getValue(AiProviderIds.LOCAL)
                    .generateResponse(prompt, conversationContext)
                val fallbackText = (fallback as? AiResult.Success)?.text.orEmpty()
                AiEngineResult(
                    text = "${result.reason} $fallbackText".trim(),
                    usedProviderId = AiProviderIds.LOCAL,
                    wasFallback = true
                )
            }
        }
    }
}

data class AiEngineResult(
    val text: String,
    val usedProviderId: String,
    val wasFallback: Boolean
)
