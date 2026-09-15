# Generar el APK de A.R.I.A. con GitHub Actions

1. Crea un repositorio en GitHub.
2. Sube el CONTENIDO de esta carpeta `ARIA` al repositorio (no una carpeta `ARIA` dentro de otra).
3. Ve a `Actions`.
4. Abre `Build APK`.
5. Pulsa `Run workflow` y selecciona `main`.
6. Espera a que termine con una marca verde.
7. Entra a la ejecución terminada.
8. En `Artifacts`, descarga `aria-debug-apk`.
9. Descomprime el ZIP descargado y encontrarás `app-debug.apk`.
10. Pasa el APK a tu Android, ábrelo y permite la instalación desde esa fuente si Android lo solicita.

Nota: este workflow usa Gradle 8.7 directamente mediante `gradle/actions/setup-gradle`, así que no depende del `gradle-wrapper.jar` que falta en el ZIP original.
