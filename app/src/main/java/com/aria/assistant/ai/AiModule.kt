package com.aria.assistant.ai

import com.aria.assistant.ai.providers.ClaudeProvider
import com.aria.assistant.ai.providers.GeminiProvider
import com.aria.assistant.ai.providers.LocalFallbackProvider
import com.aria.assistant.ai.providers.OpenAiProvider
import com.aria.assistant.settings.AiProviderIds
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * Registra cada proveedor en un Map<String, AiProvider> inyectable
 * (ver AiEngine). Agregar un proveedor nuevo en el futuro (ej. un modelo
 * local on-device) es añadir una clase + un @Binds aquí, sin tocar nada más.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @IntoMap
    @StringKey(AiProviderIds.CLAUDE)
    abstract fun bindClaudeProvider(provider: ClaudeProvider): AiProvider

    @Binds
    @IntoMap
    @StringKey(AiProviderIds.OPENAI)
    abstract fun bindOpenAiProvider(provider: OpenAiProvider): AiProvider

    @Binds
    @IntoMap
    @StringKey(AiProviderIds.GEMINI)
    abstract fun bindGeminiProvider(provider: GeminiProvider): AiProvider

    @Binds
    @IntoMap
    @StringKey(AiProviderIds.LOCAL)
    abstract fun bindLocalProvider(provider: LocalFallbackProvider): AiProvider
}
