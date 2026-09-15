package com.aria.assistant.core

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aria.assistant.memory.MemoryDao
import com.aria.assistant.memory.MemoryEntity
import com.aria.assistant.notes.NoteDao
import com.aria.assistant.notes.NoteEntity
import com.aria.assistant.reminders.ReminderDao
import com.aria.assistant.reminders.ReminderEntity
import com.aria.assistant.tasks.TaskConverters
import com.aria.assistant.tasks.TaskDao
import com.aria.assistant.tasks.TaskEntity

/**
 * Base de datos local única de A.R.I.A. (principio "local first").
 * Fase 8: se agrega Reminders. fallbackToDestructiveMigration sigue
 * activo en el módulo de DI mientras el proyecto está en 0.x (regla 24:
 * priorizar avanzar fases sobre escribir migraciones todavía innecesarias).
 */
@Database(
    entities = [MemoryEntity::class, NoteEntity::class, TaskEntity::class, ReminderEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(TaskConverters::class)
abstract class AriaDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
}
