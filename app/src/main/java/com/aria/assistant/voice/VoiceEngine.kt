package com.aria.assistant.voice

import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato del motor de voz (Speech-to-Text + Text-to-Speech).
 * Implementación real en la Fase 3 (AndroidVoiceEngine), usando Android
 * SpeechRecognizer/TextToSpeech como base local, con posibilidad de
 * conectar proveedores externos después (ver sección 3 del proyecto).
 */
interface VoiceEngine {
    val state: StateFlow<VoiceState>

    /**
     * Inicia el reconocimiento de voz. Requiere el permiso RECORD_AUDIO
     * ya concedido — la UI es responsable de solicitarlo antes de llamar.
     * @param onResult texto reconocido cuando el usuario termina de hablar.
     * @param onError mensaje legible cuando algo falla (sin conexión, sin
     *   coincidencia, permiso faltante, etc).
     */
    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit = {})

    fun stopListening()

    fun speak(text: String)
}

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}
