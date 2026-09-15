package com.aria.assistant.music

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class MusicOpenResult {
    data class OpenedIn(val appName: String) : MusicOpenResult()
    object Failed : MusicOpenResult()
}

/**
 * Music Engine (sección 8). Play/pausa/siguiente/anterior se implementan
 * con eventos de medios del sistema (AudioManager) — funcionan con
 * cualquier app de música que el usuario tenga abierta (Spotify, YouTube
 * Music, etc.) sin necesitar OAuth ni SDKs propietarios.
 *
 * "Reproduce X" abre Spotify directo en la búsqueda de esa canción usando
 * su esquema de URI público (spotify:search:...), que no requiere
 * autenticación; si Spotify no está instalado, cae a una búsqueda en
 * YouTube. Un control más fino (qué sale exactamente, listas de
 * reproducción propias) sí requeriría el SDK oficial de Spotify App
 * Remote con un Client ID del usuario — se deja como extensión futura en
 * vez de inventar una solución que no es real (regla 26).
 */
@Singleton
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playPause() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    fun next() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    fun previous() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)

    private fun sendMediaKey(keyCode: Int) {
        val eventTime = System.currentTimeMillis()
        audioManager.dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0))
        audioManager.dispatchMediaKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0))
    }

    fun searchAndOpen(query: String): MusicOpenResult {
        val spotifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${Uri.encode(query)}")).apply {
            setPackage("com.spotify.music")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (spotifyIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(spotifyIntent)
            return MusicOpenResult.OpenedIn("Spotify")
        }

        return try {
            val youtubeIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(youtubeIntent)
            MusicOpenResult.OpenedIn("YouTube")
        } catch (e: Exception) {
            MusicOpenResult.Failed
        }
    }
}
