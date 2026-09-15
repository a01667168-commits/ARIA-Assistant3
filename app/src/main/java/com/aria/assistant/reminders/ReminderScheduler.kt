package com.aria.assistant.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsula AlarmManager (sección 11). Intenta una alarma exacta primero;
 * si Android 12+ no concedió el permiso de alarmas exactas, cae a una
 * alarma inexacta en vez de fallar — se informa la diferencia al usuario
 * desde el CommandHandler (regla 26: no fallar en silencio).
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** @return true si se programó una alarma exacta, false si se usó el respaldo inexacto. */
    fun schedule(reminderId: Long, message: String, triggerAtMillis: Long): Boolean {
        val pendingIntent = buildPendingIntent(reminderId, message)
        return try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            true
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            false
        }
    }

    fun cancel(reminderId: Long, message: String) {
        alarmManager.cancel(buildPendingIntent(reminderId, message))
    }

    private fun buildPendingIntent(reminderId: Long, message: String): PendingIntent {
        val intent = ReminderReceiver.buildIntent(context, reminderId, message)
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
