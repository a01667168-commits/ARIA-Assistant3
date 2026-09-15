package com.aria.assistant.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aria.assistant.ai.AiProvider
import com.aria.assistant.settings.AriaPersonality
import com.aria.assistant.ui.components.AriaScaffold

/**
 * Configuración (sección 17). Fase 12: todos los controles son reales y
 * persistentes (DataStore) — nombre de usuario, nombre de A.R.I.A.,
 * velocidad de voz, personalidad, tema, proveedor de IA + API keys,
 * memoria activa y notificaciones.
 */
@Composable
fun SettingsScreen(onBack: (() -> Unit)? = null, viewModel: SettingsViewModel = hiltViewModel()) {
    AriaScaffold(title = "Configuración", onBack = onBack) { padding ->
        SettingsContent(padding, viewModel)
    }
}

@Composable
private fun SettingsContent(padding: PaddingValues, viewModel: SettingsViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item { SectionTitle("General") }
        item { GeneralSection(viewModel) }

        item { SectionTitle("Voz y personalidad") }
        item { VoicePersonalitySection(viewModel) }

        item { SectionTitle("Proveedor de IA") }
        item { AiProviderSection(viewModel) }

        item { SectionTitle("Privacidad") }
        item { PrivacySection(viewModel) }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun GeneralSection(viewModel: SettingsViewModel) {
    val userName by viewModel.userName.collectAsState()
    val ariaName by viewModel.ariaName.collectAsState()
    val darkTheme by viewModel.darkTheme.collectAsState()

    var userNameField by remember(userName) { mutableStateOf(userName) }
    var ariaNameField by remember(ariaName) { mutableStateOf(ariaName) }

    Column {
        OutlinedTextField(
            value = userNameField,
            onValueChange = { userNameField = it },
            label = { Text("Tu nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = null
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.setUserName(userNameField) }) { Text("Guardar") }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = ariaNameField,
            onValueChange = { ariaNameField = it },
            label = { Text("Nombre de tu asistente") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { viewModel.setAriaName(ariaNameField) }) { Text("Guardar") }
        }

        Spacer(Modifier.height(12.dp))
        ToggleRow(
            label = "Modo oscuro",
            description = "Usar el tema oscuro en toda la app",
            checked = darkTheme,
            onCheckedChange = { viewModel.setDarkTheme(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun VoicePersonalitySection(viewModel: SettingsViewModel) {
    val personality by viewModel.personality.collectAsState()
    val voiceSpeed by viewModel.voiceSpeed.collectAsState()
    var sliderValue by remember(voiceSpeed) { mutableStateOf(voiceSpeed) }

    Column {
        Text("Personalidad", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        AriaPersonality.all.forEach { option ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.setPersonality(option) }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = option == personality, onClick = { viewModel.setPersonality(option) })
                Spacer(Modifier.width(8.dp))
                Text(option.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Velocidad de voz: ${"%.1f".format(sliderValue)}x", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { viewModel.setVoiceSpeed(sliderValue) },
            valueRange = 0.5f..2.0f
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun AiProviderSection(viewModel: SettingsViewModel) {
    val selectedId by viewModel.selectedProviderId.collectAsState()
    val selectedProvider = viewModel.availableProviders.firstOrNull { it.id == selectedId }
    var apiKeyText by remember(selectedId) { mutableStateOf("") }

    LaunchedEffect(selectedId) {
        apiKeyText = viewModel.currentApiKey(selectedId)
    }

    Column {
        viewModel.availableProviders.forEach { provider ->
            ProviderRow(
                provider = provider,
                selected = provider.id == selectedId,
                onSelect = { viewModel.selectProvider(provider.id) }
            )
        }

        if (selectedProvider?.requiresApiKey == true) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = apiKeyText,
                onValueChange = { apiKeyText = it },
                label = { Text("API key de ${selectedProvider.displayName}") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = { viewModel.saveApiKey(selectedId, apiKeyText) }) { Text("Guardar clave") }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tu clave se guarda solo en este dispositivo, nunca en el código de la app " +
                    "ni en un servidor de A.R.I.A.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun PrivacySection(viewModel: SettingsViewModel) {
    val memoryEnabled by viewModel.memoryEnabled.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    Column {
        ToggleRow(
            label = "Memoria activa",
            description = "Permitir que A.R.I.A. guarde lo que le pidas recordar",
            checked = memoryEnabled,
            onCheckedChange = { viewModel.setMemoryEnabled(it) }
        )
        ToggleRow(
            label = "Notificaciones",
            description = "Avisos de recordatorios",
            checked = notificationsEnabled,
            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Todos tus datos (memoria, notas, tareas, recordatorios) se guardan solo en " +
                "este dispositivo (principio \"local first\", sección 18).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProviderRow(provider: AiProvider, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(provider.displayName, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = if (provider.requiresApiKey) "Requiere API key propia" else "Funciona sin conexión ni clave",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
