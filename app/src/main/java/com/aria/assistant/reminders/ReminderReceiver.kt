package com.aria.assistant.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

const val REMINDER_CHANNEL_ID = "aria_reminders"
private const val EXTRA_MESSAGE = "extra_message"
private const val EXTRA_REMINDER_ID = "extra_reminder_id"

/**
 * Recibe la alarma programada por [ReminderScheduler] y muestra una
 * notificación nativa de Android (sección 11). Si el usuario no concedió
 * el permiso POST_NOTIFICATIONS (Android 13+), el sistema simplemente no
 * la mostrará — no hay forma de forzarla, y no lo intentamos (regla 26).
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Tienes un recordatorio."
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, 0L)

        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("A.R.I.A.")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(reminderId.toInt(), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(REMINDER_CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        REMINDER_CHANNEL_ID,
                        "Recordatorios de A.R.I.A.",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
        }
    }

    companion object {
        fun buildIntent(context: Context, reminderId: Long, message: String): Intent =
            Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_MESSAGE, message)
            }
    }
}
