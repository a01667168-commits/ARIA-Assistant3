package com.aria.assistant.system

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsula la apertura de apps instaladas usando Intents estándar de
 * Android (sección 7 y Fase 11). Nunca intenta controlar apps de terceros
 * más allá de abrirlas — si Android no lo permite, se informa la
 * limitación en vez de inventar una solución (regla 26).
 *
 * Fase 11: en vez de depender solo de una lista fija de paquetes conocidos,
 * busca dinámicamente entre TODAS las apps instaladas por su nombre visible
 * (label), así A.R.I.A. puede abrir cualquier app del teléfono del usuario,
 * no solo las que anticipamos.
 */
@Singleton
class AppLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Alias rápidos para apps muy comunes cuyo nombre visible no siempre
    // coincide con lo que la gente dice en voz alta.
    private val knownAliases = mapOf(
        "spotify" to "com.spotify.music",
        "youtube" to "com.google.android.youtube",
        "whatsapp" to "com.whatsapp",
        "telegram" to "org.telegram.messenger",
        "chrome" to "com.android.chrome",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "camara" to "com.android.camera",
        "cámara" to "com.android.camera",
        "galeria" to "com.google.android.apps.photos",
        "galería" to "com.google.android.apps.photos",
        "crunchyroll" to "com.crunchyroll.crunchyroid"
    )

    fun launch(appNameRaw: String): LaunchResult {
        val appName = appNameRaw.trim().lowercase()

        if (appName.contains("configuracion") || appName.contains("configuración") || appName.contains("ajustes")) {
            val intent = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return LaunchResult.Success("Abriendo la configuración de Android.")
        }

        // 1) Alias rápido conocido.
        knownAliases[appName]?.let { packageName ->
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return LaunchResult.Success("Abriendo $appNameRaw.")
            }
            return LaunchResult.NotInstalled(appNameRaw)
        }

        // 2) Búsqueda dinámica entre todas las apps instaladas por su nombre visible.
        val match = findInstalledAppByLabel(appName)
        if (match != null) {
            val intent = context.packageManager.getLaunchIntentForPackage(match.packageName)
            return if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                LaunchResult.Success("Abriendo ${match.label}.")
            } else {
                // La app existe pero no expone una pantalla propia para abrir directamente
                // (ej. un servicio en segundo plano) — Android no permite forzarla.
                LaunchResult.NotLaunchable(match.label)
            }
        }

        return LaunchResult.Unknown(appNameRaw)
    }

    private fun findInstalledAppByLabel(query: String): InstalledApp? {
        val packageManager = context.packageManager
        val launchableApps = packageManager.getInstalledApplications(0)
            .asSequence()
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .mapNotNull { info ->
                val label = packageManager.getApplicationLabel(info).toString()
                InstalledApp(packageName = info.packageName, label = label)
            }
            .toList()

        return launchableApps.firstOrNull { it.label.lowercase() == query } // coincidencia exacta primero
            ?: launchableApps.firstOrNull { it.label.lowercase().contains(query) } // luego parcial
    }

    private data class InstalledApp(val packageName: String, val label: String)
}

sealed class LaunchResult {
    data class Success(val message: String) : LaunchResult()
    data class NotInstalled(val appName: String) : LaunchResult()
    data class NotLaunchable(val appName: String) : LaunchResult()
    data class Unknown(val appName: String) : LaunchResult()
}
