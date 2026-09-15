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
 * Proveedor de IA basado en la API de OpenAI (Chat Completions).
 * Requiere que el usuario ingrese su propia API key en Configuración.
 */
@Singleton
class OpenAiProvider @Inject constructor(
    private val settingsRepository: SettingsRepository
) : AiProvider {
    override val id = AiProviderIds.OPENAI
    override val displayName = "OpenAI (ChatGPT)"
    override val requiresApiKey = true

    override suspend fun generateResponse(prompt: String, conversationContext: List<String>): AiResult {
        val apiKey = settingsRepository.apiKeyFor(id).first()
        if (apiKey.isBlank()) return AiResult.RequiresConfiguration

        return withContext(Dispatchers.IO) {
            try {
                val connection = (URL("https://api.openai.com/v1/chat/completions")
                    .openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 30_000
                }

                val messages = JSONArray().apply {
                    put(
                        JSONObject()
                            .put("role", "system")
                            .put("content", buildSystemPrompt())
                    )
                    put(JSONObject().put("role", "user").put("content", prompt))
                }

                val body = JSONObject().apply {
                    put("model", "gpt-4o-mini")
                    put("messages", messages)
                }

                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

                if (code !in 200..299) {
                    return@withContext AiResult.Error("OpenAI respondió con un error ($code).")
                }

                val json = JSONObject(responseText)
                val text = json.optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")
                    .orEmpty()

                if (text.isBlank()) AiResult.Error("OpenAI no devolvió texto en la respuesta.")
                else AiResult.Success(text)
            } catch (e: Exception) {
                AiResult.Error("No se pudo conectar con OpenAI: ${e.message}")
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
