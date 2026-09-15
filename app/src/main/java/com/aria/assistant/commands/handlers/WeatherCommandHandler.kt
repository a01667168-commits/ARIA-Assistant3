package com.aria.assistant.commands.handlers

import android.Manifest
import com.aria.assistant.commands.CommandHandler
import com.aria.assistant.commands.CommandResult
import com.aria.assistant.weather.WeatherRepository
import com.aria.assistant.weather.WeatherResult
import javax.inject.Inject

/** Weather Commands (sección 6 y 13). Fase 9: ya conectado a Open-Meteo real. */
class WeatherCommandHandler @Inject constructor(
    private val weatherRepository: WeatherRepository
) : CommandHandler {
    override val domain = "weather"
    private val triggers = listOf("clima", "temperatura", "pronóstico", "pronostico", "va a llover")

    override fun canHandle(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return triggers.any { normalized.contains(it) }
    }

    override suspend fun execute(input: String): CommandResult {
        return when (val result = weatherRepository.currentWeather()) {
            is WeatherResult.Success -> {
                val info = result.info
                val humidityText = info.humidityPercent?.let { ", humedad del $it%" } ?: ""
                CommandResult.Success(
                    "Ahora mismo hay ${info.condition}, con ${info.temperatureC.toInt()} grados$humidityText."
                )
            }
            WeatherResult.NoPermission -> CommandResult.NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            WeatherResult.NoLocation -> CommandResult.Failure(
                "No pude obtener tu ubicación todavía. Abre Maps o espera a que el GPS tenga señal e intenta de nuevo."
            )
            is WeatherResult.Error -> CommandResult.Failure(result.message)
        }
    }
}
