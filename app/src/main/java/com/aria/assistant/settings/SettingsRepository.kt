package com.aria.assistant.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "aria_settings")

object AiProviderIds {
    const val CLAUDE = "claude"
    const val OPENAI = "openai"
    const val GEMINI = "gemini"
    const val LOCAL = "local"
}

/** Personalidades disponibles (sección 2 y 17): ajustan el tono del system prompt de la IA. */
object AriaPersonality {
    const val EQUILIBRADA = "equilibrada"
    const val PROFESIONAL = "profesional"
    const val CASUAL = "casual"

    val all = listOf(EQUILIBRADA, PROFESIONAL, CASUAL)

    fun describe(personality: String): String = when (personality) {
        PROFESIONAL -> "Mantén siempre un tono formal, preciso y profesional."
        CASUAL -> "Usa un tono relajado, cercano y coloquial, como hablando con un amigo."
        else -> "Sé natural: profesional cuando la situación lo pida, casual el resto del tiempo, con humor moderado."
    }
}

/**
 * Configuración persistida localmente (principio "local first", sección 18).
 * Fase 12: ya cubre todo lo pedido en la sección 17 — nombre de usuario,
 * nombre de A.R.I.A., velocidad de voz, personalidad, tema, proveedor de
 * IA, API keys, memoria activa y notificaciones.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SELECTED_PROVIDER = stringPreferencesKey("selected_ai_provider")
        fun apiKeyFor(providerId: String) = stringPreferencesKey("api_key_$providerId")
        val USER_NAME = stringPreferencesKey("user_name")
        val ARIA_NAME = stringPreferencesKey("aria_name")
        val VOICE_SPEED = floatPreferencesKey("voice_speed")
        val PERSONALITY = stringPreferencesKey("personality")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val MEMORY_ENABLED = booleanPreferencesKey("memory_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val selectedProviderId: Flow<String> =
        context.dataStore.data.map { it[Keys.SELECTED_PROVIDER] ?: AiProviderIds.LOCAL }

    fun apiKeyFor(providerId: String): Flow<String> =
        context.dataStore.data.map { it[Keys.apiKeyFor(providerId)] ?: "" }

    val userName: Flow<String> = context.dataStore.data.map { it[Keys.USER_NAME] ?: "" }
    val ariaName: Flow<String> = context.dataStore.data.map { it[Keys.ARIA_NAME] ?: "A.R.I.A." }
    val voiceSpeed: Flow<Float> = context.dataStore.data.map { it[Keys.VOICE_SPEED] ?: 1.0f }
    val personality: Flow<String> = context.dataStore.data.map { it[Keys.PERSONALITY] ?: AriaPersonality.EQUILIBRADA }
    val darkTheme: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_THEME] ?: true }
    val memoryEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.MEMORY_ENABLED] ?: true }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setSelectedProvider(providerId: String) {
        context.dataStore.edit { it[Keys.SELECTED_PROVIDER] = providerId }
    }

    suspend fun setApiKey(providerId: String, key: String) {
        context.dataStore.edit { it[Keys.apiKeyFor(providerId)] = key }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun setAriaName(name: String) {
        context.dataStore.edit { it[Keys.ARIA_NAME] = name.ifBlank { "A.R.I.A." } }
    }

    suspend fun setVoiceSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.VOICE_SPEED] = speed.coerceIn(0.5f, 2.0f) }
    }

    suspend fun setPersonality(personality: String) {
        context.dataStore.edit { it[Keys.PERSONALITY] = personality }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setMemoryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MEMORY_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
