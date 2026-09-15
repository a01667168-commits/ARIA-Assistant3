package com.aria.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AriaDarkColorScheme = darkColorScheme(
    primary = AriaAccentCyan,
    secondary = AriaAccentViolet,
    tertiary = AriaAccentSoft,
    background = AriaBackground,
    surface = AriaSurface,
    surfaceVariant = AriaSurfaceVariant,
    onPrimary = AriaBackground,
    onSecondary = AriaBackground,
    onBackground = AriaTextPrimary,
    onSurface = AriaTextPrimary,
    error = AriaError
)

private val AriaLightColorScheme = lightColorScheme(
    primary = AriaAccentCyan,
    secondary = AriaAccentViolet,
    background = AriaLightBackground,
    surface = AriaLightSurface,
    onBackground = AriaLightTextPrimary,
    onSurface = AriaLightTextPrimary,
    error = AriaError
)

/**
 * Tema principal de A.R.I.A. El modo oscuro es el modo por defecto/preferido
 * (ver requisito de "Dark Mode" principal); el modo claro queda disponible
 * como alternativa configurable en Settings (se conecta en fases posteriores).
 */
@Composable
fun AriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AriaDarkColorScheme else AriaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AriaTypography,
        content = content
    )
}
