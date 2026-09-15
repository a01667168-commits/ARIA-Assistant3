# Reglas de ProGuard/R8 para A.R.I.A. (Fase 13: preparadas, pero minifyEnabled
# sigue en false hasta que el usuario pueda compilar y probar un build de
# release real — activarlo aquí sin poder verificarlo sería declarar que
# funciona sin haberlo comprobado, regla 25 del proyecto).

# Room, Hilt y Jetpack Compose ya incluyen sus propias "consumer proguard
# rules" dentro de sus AAR, así que no hace falta duplicarlas aquí.

# Los BroadcastReceiver y Activities declarados en el Manifest (ReminderReceiver,
# MainActivity) ya quedan protegidos automáticamente por las reglas por
# defecto de Android Gradle Plugin basadas en el Manifest.

# Este proyecto no usa reflection propia sobre sus propias clases (el
# parseo de respuestas de IA/clima se hace manualmente con org.json,
# sin Gson/Moshi), así que no se necesitan @Keep adicionales todavía.

# Si en la Fase 15 se activa minifyEnabled = true, revisar primero:
#   - que el login por voz (SpeechRecognizer/TextToSpeech) siga funcionando
#   - que las respuestas de Claude/OpenAI/Gemini se sigan parseando bien
#   - que Room siga generando/leyendo la base de datos sin errores
-dontwarn org.json.**
