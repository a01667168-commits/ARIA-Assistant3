# A.R.I.A. — Checklist final (Fase 15)

Verificación manual hecha por Claude antes de entregar (sección 25 del proyecto). Como este entorno
no tiene SDK de Android ni red, esto es una revisión estática (grep/lectura de código), **no** una
compilación real — la confirmación definitiva es tuya, siguiendo los pasos de más abajo.

## ✅ Verificado en este entorno
- [x] Los 78 archivos `.kt` de producción y los 7 de test tienen `package com.aria.assistant...` correcto.
- [x] Las 10 pantallas del NavGraph (`AriaNavGraph.kt`) existen todas como archivos reales.
- [x] Los 10 ViewModels usados con `hiltViewModel()` existen y tienen `@Inject constructor`.
- [x] Los 11 `CommandHandler` están registrados en `CommandModule` (10 dominios + fallback de IA).
- [x] Las 4 entidades Room (`Memory`, `Note`, `Task`, `Reminder`) están en `AriaDatabase`, con su
      `TypeConverters` para el enum `TaskPriority`.
- [x] El `AndroidManifest.xml` declara todos los permisos que se usan en tiempo de ejecución
      (RECORD_AUDIO, POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, READ/WRITE_CALENDAR,
      ACCESS_COARSE_LOCATION), el `<receiver>` de recordatorios, y el `<queries>` para ver apps instaladas.
- [x] No quedan imports sin usar en los archivos revisados en la Fase 13.
- [x] 7 tests unitarios reales (`CommandEngine`, parseo de recordatorios/tareas, personalidad,
      conversión de prioridad, y dos repositorios con fakes en memoria) — no se pueden *ejecutar* aquí
      sin Gradle, pero el código de los tests es sintácticamente correcto y usa solo JUnit +
      kotlinx-coroutines, ya declarados como dependencias desde la Fase 1.

## ⚠️ Lo que SOLO puede confirmar una compilación real (no se puede verificar sin Android Studio/Gradle)
- Que el proyecto compile sin errores de tipos, de Compose o de KSP (Room/Hilt).
- Que `./gradlew test` pase los 7 tests unitarios.
- Que la app corra sin crashear en un dispositivo/emulador real.
- Que las claves de API que ingreses en Configuración funcionen contra Claude/OpenAI/Gemini (necesita
  tus propias claves y conexión a Internet real).

## Pasos para compilar y generar el APK

1. Descomprime el ZIP y abre la carpeta `ARIA/` con **Android Studio** (Hedgehog/Iguana o más nuevo).
2. Espera a que sincronice Gradle (regenerará automáticamente `gradle-wrapper.jar`, que falta porque
   este entorno no tiene red para descargarlo).
3. Corre los tests unitarios primero para detectar cualquier problema temprano:
   ```bash
   ./gradlew test
   ```
4. Genera el APK de debug:
   ```bash
   ./gradlew assembleDebug
   ```
   El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.
5. Instálalo en tu teléfono (`adb install app-debug.apk`, o cópialo y ábrelo directamente si tienes
   "orígenes desconocidos" habilitado).
6. En la app, ve a **Configuración > Proveedor de IA** y pega tu propia API key de Claude, OpenAI o
   Gemini si quieres respuestas generadas (si no, A.R.I.A. sigue funcionando con el proveedor local).

## Antes de publicar una versión real (más allá de esta rc1)
- Reemplazar `fallbackToDestructiveMigration()` en `DatabaseModule.kt` por migraciones reales de Room
  (ahora mismo, cualquier cambio de esquema borra los datos locales del usuario).
- Decidir si activar `minifyEnabled = true` en `app/build.gradle.kts`, probando cuidadosamente después
  (ver notas en `proguard-rules.pro`).
- Reemplazar los íconos vectoriales simples de `res/drawable/ic_launcher_*.xml` por una identidad
  visual definitiva.
- Revisar los textos de permisos y disclaimers según las políticas de la tienda donde publiques
  (Play Store exige explicar por qué se piden permisos como ubicación o calendario).
