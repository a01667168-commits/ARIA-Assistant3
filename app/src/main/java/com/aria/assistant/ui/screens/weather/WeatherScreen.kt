package com.aria.assistant.ui.screens.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aria.assistant.ui.components.AriaScaffold
import com.aria.assistant.weather.WeatherResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

/** Clima (sección 13). Fase 9: temperatura, condición y humedad reales (Open-Meteo). */
@Composable
fun WeatherScreen(onBack: (() -> Unit)? = null, viewModel: WeatherViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val result by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted; if (granted) viewModel.refresh() }

    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.refresh()
    }

    AriaScaffold(title = "Clima", onBack = onBack) { padding ->
        WeatherContent(
            padding = padding,
            hasPermission = hasPermission,
            isLoading = isLoading,
            result = result,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
            onRetry = { viewModel.refresh(forceRefresh = true) }
        )
    }
}

@Composable
private fun WeatherContent(
    padding: PaddingValues,
    hasPermission: Boolean,
    isLoading: Boolean,
    result: WeatherResult?,
    onRequestPermission: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            !hasPermission -> {
                InfoState(
                    icon = Icons.Filled.Cloud,
                    message = "Necesito tu ubicación aproximada para darte el clima de tu zona."
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRequestPermission) { Text("Otorgar permiso") }
            }
            isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            result is WeatherResult.Success -> {
                val info = result.info
                Text("${info.temperatureC.toInt()}°C", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Text(info.condition.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                info.humidityPercent?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Humedad: $it%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Actualizar") }
            }
            result is WeatherResult.NoLocation -> {
                InfoState(icon = Icons.Filled.Cloud, message = "No pude obtener tu ubicación todavía. Abre Maps o espera señal de GPS.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRetry) { Text("Reintentar") }
            }
            result is WeatherResult.Error -> {
                InfoState(icon = Icons.Filled.Cloud, message = result.message)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRetry) { Text("Reintentar") }
            }
            else -> InfoState(icon = Icons.Filled.Cloud, message = "Cargando el clima…")
        }
    }
}

@Composable
private fun InfoState(icon: ImageVector, message: String) {
    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(8.dp))
    Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
}
