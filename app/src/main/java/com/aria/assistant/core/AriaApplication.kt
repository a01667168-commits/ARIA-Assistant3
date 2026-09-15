package com.aria.assistant.core

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada de la aplicación A.R.I.A.
 * Anotada con @HiltAndroidApp para habilitar la inyección de dependencias
 * en toda la app (AI Engine, Voice Engine, Command Engine, repositorios, etc).
 */
@HiltAndroidApp
class AriaApplication : Application()
