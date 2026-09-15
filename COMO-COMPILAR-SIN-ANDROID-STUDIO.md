# Compilar el APK sin instalar Android Studio (vía GitHub Actions)

Esta es una alternativa real a la Opción A del `CHECKLIST.md`. GitHub Actions compila el proyecto en
un servidor de GitHub (que ya tiene el SDK de Android instalado) y te deja el APK listo para descargar
— todo gratis, y podés hacerlo desde el navegador.

## Pasos

1. **Crea una cuenta gratuita en [github.com](https://github.com)** si no tienes una.

2. **Crea un repositorio nuevo** (puede ser privado): botón verde "New" en tu perfil de GitHub.
   No hace falta inicializarlo con nada (ni README ni .gitignore).

3. **Sube el proyecto**: la forma más simple es arrastrar toda la carpeta `ARIA/` (descomprimida, con
   la carpeta `.github/` incluida) a la página de subida de archivos de GitHub:
   - Entra al repositorio recién creado.
   - Click en "uploading an existing file" (o "Add file" > "Upload files").
   - Arrastra el contenido de la carpeta `ARIA/` (todo lo de adentro, no la carpeta en sí) a la zona
     de subida. Chrome y Edge en computadora permiten arrastrar carpetas completas y mantienen la
     estructura de subcarpetas.
   - Click en "Commit changes".

4. **La compilación empieza sola**: en cuanto termines de subir los archivos, GitHub Actions detecta
   el archivo `.github/workflows/build-apk.yml` y arranca automáticamente. Si no arrancó, ve a la
   pestaña **Actions** del repositorio y click en "Run workflow" (botón manual).

5. **Espera a que termine** (2-5 minutos normalmente). Vas a ver un círculo amarillo (en progreso),
   luego un tilde verde ✅ (éxito) o una X roja ❌ (error).

6. **Descarga el APK**:
   - Click en la ejecución que terminó (la fila más reciente en la pestaña Actions).
   - Bajá hasta la sección **Artifacts** al final de la página.
   - Descarga `aria-debug-apk` — es un .zip que contiene `app-debug.apk` adentro.

7. **Instálalo en tu teléfono**: pasa el archivo `app-debug.apk` a tu teléfono (por correo, Google
   Drive, WhatsApp a ti mismo, cable USB, lo que te resulte más fácil), ábrelo desde el teléfono, y
   Android te va a pedir permiso para "instalar apps de fuentes desconocidas" la primera vez — acéptalo
   para esa app y listo.

## Si la ❌ falla

Abre la ejecución fallida y fíjate qué paso specific falló (cada paso es expandible). Los errores más
probables:
- **Error de compilación de Kotlin/Compose**: copia el mensaje de error exacto y pégamelo en el chat —
  puedo revisar el código y corregirlo aunque no pueda ejecutar el build yo mismo.
- **Falta de memoria en el runner**: muy poco común con este proyecto (es chico), pero si pasa, se
  soluciona agregando `org.gradle.jvmargs=-Xmx3072m` en `gradle.properties`.
- **Versión de Gradle/AGP incompatible con el runner**: si GitHub actualiza sus runners y algo deja de
  coincidir, avísame el mensaje de error y ajusto las versiones en `build.gradle.kts`.

## Nota sobre firma del APK

Este flujo genera un **APK de debug**, firmado automáticamente con una clave de depuración genérica
(no la tuya). Sirve perfecto para instalar y probar en tu teléfono, pero si en el futuro quieres subir
la app a Google Play, vas a necesitar generar un **APK/AAB de release** firmado con tu propia clave —
eso es un paso adicional que no cubre este workflow (y que conviene dejar para cuando el proyecto esté
más maduro, como ya se menciona en `CHECKLIST.md`).
