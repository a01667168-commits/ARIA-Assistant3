package com.aria.assistant.reminders

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Un recordatorio programado con AlarmManager (sección 11). */
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val message: String,
    val triggerAtMillis: Long
)
