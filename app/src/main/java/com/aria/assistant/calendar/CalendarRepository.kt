package com.aria.assistant.calendar

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class CalendarEvent(val id: Long, val title: String, val startMillis: Long)

/**
 * Módulo Calendar (sección 12). Consultar eventos usa la API oficial
 * CalendarContract.Instances (requiere permiso READ_CALENDAR ya concedido).
 * Crear eventos delega en la app de Calendario del usuario vía Intent —
 * no requiere WRITE_CALENDAR y respeta la app/calendario que el usuario
 * ya tenga configurado, en vez de escribir directamente en su base de datos.
 */
@Singleton
class CalendarRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Lanza SecurityException si READ_CALENDAR no está concedido. */
    suspend fun upcomingEvents(limit: Int = 10): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN
        )
        val now = System.currentTimeMillis()
        val oneMonthLater = now + 30L * 24 * 60 * 60 * 1000

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(now.toString())
            .appendPath(oneMonthLater.toString())
            .build()

        val events = mutableListOf<CalendarEvent>()
        context.contentResolver.query(uri, projection, null, null, "begin ASC")?.use { cursor ->
            val idIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
            val titleIdx = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
            val beginIdx = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
            while (cursor.moveToNext() && events.size < limit) {
                events.add(
                    CalendarEvent(
                        id = cursor.getLong(idIdx),
                        title = cursor.getString(titleIdx) ?: "(sin título)",
                        startMillis = cursor.getLong(beginIdx)
                    )
                )
            }
        }
        events
    }

    fun createEventIntent(title: String): Intent =
        Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
            putExtra(CalendarContract.Events.TITLE, title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
}
