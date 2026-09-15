package com.aria.assistant.ui.screens.home

import com.aria.assistant.voice.VoiceState

/**
 * Estados posibles del "núcleo" visual de A.R.I.A.
 * Se derivan directamente del VoiceEngine real (Fase 3).
 */
enum class AriaCoreState {
    IDLE,
    LISTENING,
    PROCESSING,
    RESPONDING
}

fun VoiceState.toCoreState(): AriaCoreState = when (this) {
    VoiceState.IDLE -> AriaCoreState.IDLE
    VoiceState.LISTENING -> AriaCoreState.LISTENING
    VoiceState.PROCESSING -> AriaCoreState.PROCESSING
    VoiceState.SPEAKING -> AriaCoreState.RESPONDING
    VoiceState.ERROR -> AriaCoreState.IDLE
}

data class HomeUiState(
    val statusText: String = "En línea",
    val coreState: AriaCoreState = AriaCoreState.IDLE,
    val conversation: List<ConversationEntry> = emptyList()
)

data class ConversationEntry(
    val isUser: Boolean,
    val text: String
)
