package com.aria.assistant.ai.providers

import com.aria.assistant.ai.AiProvider
import com.aria.assistant.ai.AiResult
import com.aria.assistant.settings.AiProviderIds
import com.aria.assistant.settings.SettingsRepository
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
 * Proveedor de IA basado en la API de Anthropic (Claude).
 * Requiere que el usuario ingrese su propia API key en Configuración —
 * nunca se incluye una clave real en el código (sección 3 y 4 del proyecto).
 * Fase 12: el system prompt incorpora el nombre de A.R.I.A., su
 * personalidad elegida y el nombre del usuario, todos desde Configuración.
 */
@Singleton
class ClaudeProvider @Inject constructor(
    private val settingsRepository: SettingsRepository
) : AiProvider {
    override val id = AiProviderIds.CLAUDE
    override val displayName = "Claude (Anthropic)"
    override val requiresApiKey = true

    override suspend fun generateResponse(prompt: String, conversationContext: List<String>): AiResult {
        val apiKey = settingsRepository.apiKeyFor(id).first()
        if (apiKey.isBlank()) return AiResult.RequiresConfiguration

        return withContext(Dispatchers.IO) {
            try {
                val connection = (URL("https://api.anthropic.com/v1/messages")
                    .openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("content-type", "application/json")
                    setRequestProperty("x-api-key", apiKey)
                    setRequestProperty("anthropic-version", "2023-06-01")
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 30_000
                }

                val body = JSONObject().apply {
                    put("model", "claude-sonnet-4-6")
                    put("max_tokens", 1024)
                    put("system", buildSystemPrompt())
                    put(
                        "messages",
                        JSONArray().put(JSONObject().put("role", "user").put("content", prompt))
                    )
                }

                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

                if (code !in 200..299) {
                    return@withContext AiResult.Error("Claude respondió con un error ($code).")
                }

                val json = JSONObject(responseText)
                val contentArray = json.optJSONArray("content")
                val text = (0 until (contentArray?.length() ?: 0))
                    .map { contentArray!!.getJSONObject(it) }
                    .firstOrNull { it.optString("type") == "text" }
                    ?.optString("text")
                    .orEmpty()

                if (text.isBlank()) AiResult.Error("Claude no devolvió texto en la respuesta.")
                else AiResult.Success(text)
            } catch (e: Exception) {
                AiResult.Error("No se pudo conectar con Claude: ${e.message}")
            }
        }
    }

    private suspend fun buildSystemPrompt(): String {
        val ariaName = settingsRepository.ariaName.first()
        val personality = settingsRepository.personality.first()
        val userName = settingsRepository.userName.first()
        val userLine = if (userName.isNotBlank()) " El usuario se llama $userName." else ""
        return "Eres $ariaName (Advanced Responsive Intelligent Assistant), un asistente personal " +
            "inteligente y cercano.$userLine ${AriaPersonality.describe(personality)} " +
            "Responde siempre en español, de forma breve y conversacional."
    }
}
