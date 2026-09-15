package com.aria.assistant.voice

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Une la interfaz VoiceEngine a la implementación local (AndroidVoiceEngine).
 * Cambiar de proveedor de voz en el futuro significa cambiar solo este binding.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {

    @Binds
    @Singleton
    abstract fun bindVoiceEngine(impl: AndroidVoiceEngine): VoiceEngine
}
