package com.aria.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.aria.assistant.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación local del motor de voz usando las APIs nativas de Android
 * (SpeechRecognizer para STT, TextToSpeech para TTS). No requiere ninguna
 * API externa ni clave — cumple el requisito de funcionar sin conexión a
 * un proveedor de pago (sección 4 del proyecto).
 *
 * Fase 12: la velocidad de habla se lee de Configuración (SettingsRepository)
 * y se aplica antes de cada respuesta hablada.
 *
 * Preparado para ser reemplazado o combinado con un proveedor externo más
 * adelante sin tocar el resto de la app, ya que todo pasa por [VoiceEngine].
 */
@Singleton
class AndroidVoiceEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : VoiceEngine {

    private val engineScope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(VoiceState.IDLE)
    override val state: StateFlow<VoiceState> = _state

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("es", "ES")
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _state.value = VoiceState.SPEAKING
                    }
                    override fun onDone(utteranceId: String?) {
                        _state.value = VoiceState.IDLE
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _state.value = VoiceState.IDLE
                    }
                })
            }
        }
    }

    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("El reconocimiento de voz no está disponible en este dispositivo.")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = VoiceState.LISTENING
                }

                override fun onBeginningOfSpeech() {
                    _state.value = VoiceState.LISTENING
                }

                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    _state.value = VoiceState.PROCESSING
                }

                override fun onError(error: Int) {
                    _state.value = VoiceState.IDLE
                    onError(mapSpeechError(error))
                }

                override fun onResults(results: Bundle?) {
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    _state.value = VoiceState.IDLE
                    if (text.isNotBlank()) onResult(text) else onError("No pude entender lo que dijiste.")
                }

                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            startListening(intent)
        }
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _state.value = VoiceState.IDLE
    }

    override fun speak(text: String) {
        engineScope.launch {
            val speed = settingsRepository.voiceSpeed.first()
            textToSpeech?.setSpeechRate(speed)
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        }
    }

    private fun mapSpeechError(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH -> "No entendí lo que dijiste."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No escuché nada, intenta de nuevo."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Falta el permiso de micrófono."
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "Problema de red al reconocer la voz."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocimiento de voz está ocupado, intenta de nuevo."
        else -> "Ocurrió un error al escuchar."
    }
}
