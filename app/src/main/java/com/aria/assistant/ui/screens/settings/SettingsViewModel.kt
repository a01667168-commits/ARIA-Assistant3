package com.aria.assistant.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aria.assistant.ai.AiProvider
import com.aria.assistant.settings.AiProviderIds
import com.aria.assistant.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val providers: Map<String, @JvmSuppressWildcards AiProvider>
) : ViewModel() {

    val availableProviders: List<AiProvider> =
        providers.values.sortedBy { it.id != AiProviderIds.LOCAL }

    val selectedProviderId: StateFlow<String> = settingsRepository.selectedProviderId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiProviderIds.LOCAL)

    val userName: StateFlow<String> = settingsRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val ariaName: StateFlow<String> = settingsRepository.ariaName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "A.R.I.A.")

    val personality: StateFlow<String> = settingsRepository.personality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "equilibrada")

    val voiceSpeed: StateFlow<Float> = settingsRepository.voiceSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1.0f)

    val darkTheme: StateFlow<Boolean> = settingsRepository.darkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val memoryEnabled: StateFlow<Boolean> = settingsRepository.memoryEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun selectProvider(providerId: String) {
        viewModelScope.launch { settingsRepository.setSelectedProvider(providerId) }
    }

    suspend fun currentApiKey(providerId: String): String =
        settingsRepository.apiKeyFor(providerId).first()

    fun saveApiKey(providerId: String, key: String) {
        viewModelScope.launch { settingsRepository.setApiKey(providerId, key) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { settingsRepository.setUserName(name) }
    }

    fun setAriaName(name: String) {
        viewModelScope.launch { settingsRepository.setAriaName(name) }
    }

    fun setPersonality(personality: String) {
        viewModelScope.launch { settingsRepository.setPersonality(personality) }
    }

    fun setVoiceSpeed(speed: Float) {
        viewModelScope.launch { settingsRepository.setVoiceSpeed(speed) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkTheme(enabled) }
    }

    fun setMemoryEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMemoryEnabled(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationsEnabled(enabled) }
    }
}
