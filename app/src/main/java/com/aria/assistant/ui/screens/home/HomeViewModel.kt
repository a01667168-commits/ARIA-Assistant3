package com.aria.assistant.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aria.assistant.commands.CommandEngine
import com.aria.assistant.commands.CommandResult
import com.aria.assistant.voice.VoiceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla de Conversación.
 *
 * Fase 3: conectado al VoiceEngine real (escucha, habla, refleja su estado).
 * Fase 4: conectado al AiEngine real (a través del CommandEngine).
 * Fase 5: ya no llama directo al AiEngine — todo pasa por el CommandEngine,
 * que primero intenta interpretar comandos específicos (abrir apps, buscar,
 * hora/fecha, notas/tareas/recordatorios/calendario/clima/música) y solo si
 * ninguno coincide, delega a la IA como conversación libre.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val voiceEngine: VoiceEngine,
    private val commandEngine: CommandEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            conversation = listOf(
                ConversationEntry(isUser = false, text = "Hola. Soy A.R.I.A. Estoy lista para ayudarte.")
            )
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** true mientras se procesa el comando/la IA (no es un estado del VoiceEngine). */
    private val _isThinking = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            combine(voiceEngine.state, _isThinking) { voiceState, thinking ->
                if (thinking) AriaCoreState.PROCESSING else voiceState.toCoreState()
            }.collect { core -> _uiState.update { it.copy(coreState = core) } }
        }
    }

    fun onMicClick() {
        voiceEngine.startListening(
            onResult = { recognizedText -> handleUserSpeech(recognizedText) },
            onError = { message -> appendAriaMessage(message) }
        )
    }

    private fun handleUserSpeech(text: String) {
        appendUserMessage(text)
        _isThinking.value = true

        viewModelScope.launch {
            val replyText = when (val result = commandEngine.process(text)) {
                is CommandResult.Success -> result.message
                is CommandResult.Failure -> result.reason
                is CommandResult.NeedsPermission -> "Necesito el permiso de ${result.permission} para hacer eso."
            }
            _isThinking.value = false
            appendAriaMessage(replyText)
            voiceEngine.speak(replyText)
        }
    }

    private fun appendUserMessage(text: String) {
        _uiState.update { it.copy(conversation = it.conversation + ConversationEntry(isUser = true, text = text)) }
    }

    private fun appendAriaMessage(text: String) {
        _uiState.update { it.copy(conversation = it.conversation + ConversationEntry(isUser = false, text = text)) }
    }

    override fun onCleared() {
        voiceEngine.stopListening()
        super.onCleared()
    }
}
