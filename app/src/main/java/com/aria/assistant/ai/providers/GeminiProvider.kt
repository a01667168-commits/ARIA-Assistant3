package com.aria.assistant.ai.providers

import com.aria.assistant.ai.AiProvider
import com.aria.assistant.ai.AiResult
import com.aria.assistant.settings.AiProviderIds
import com.aria.assistant.settings.AriaPersonality
import com.aria.assistant.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proveedor de IA basado en la API de Google Gemini (generateContent).
 * Requiere que el usuario ingrese su propia API key en Configuración.
 */
@Singleton
class GeminiProvider @Inject constructor(
    private val settingsRepository: SettingsRepository
) : AiProvider {
    override val id = AiProviderIds.GEMINI
    override val displayName = "Gemini (Google)"
    override val requiresApiKey = true

    override suspend fun generateResponse(prompt: String, conversationContext: List<String>): AiResult {
        val apiKey = settingsRepository.apiKeyFor(id).first()
        if (apiKey.isBlank()) return AiResult.RequiresConfiguration

        return withContext(Dispatchers.IO) {
            try {
                val url = URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/" +
                        "gemini-1.5-flash:generateContent?key=$apiKey"
                )
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 30_000
                }

                val body = JSONObject().apply {
                    put(
                        "system_instruction",
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", buildSystemPrompt()))
                        )
                    )
                    put(
                        "contents",
                        JSONArray().put(
                            JSONObject().put(
                                "parts",
                                JSONArray().put(JSONObject().put("text", prompt))
                            )
                        )
                    )
                }

                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

                if (code !in 200..299) {
                    return@withContext AiResult.Error("Gemini respondió con un error ($code).")
                }

                val json = JSONObject(responseText)
                val text = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    .orEmpty()

                if (text.isBlank()) AiResult.Error("Gemini no devolvió texto en la respuesta.")
                else AiResult.Success(text)
            } catch (e: Exception) {
                AiResult.Error("No se pudo conectar con Gemini: ${e.message}")
            }
        }
    }

    private suspend fun buildSystemPrompt(): String {
        val ariaName = settingsRepository.ariaName.first()
        val personality = settingsRepository.personality.first()
        val userName = settingsRepository.userName.first()
        val userLine = if (userName.isNotBlank()) " El usuario se llama $userName." else ""
        return "Eres $ariaName, un asistente personal inteligente y cercano.$userLine " +
            "${AriaPersonality.describe(personality)} Responde en español, breve."
    }
}
