package com.aria.assistant.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority { ALTA, MEDIA, BAJA }

/** Una tarea del usuario (sección 10): título, prioridad, categoría, estado. */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val priority: TaskPriority = TaskPriority.MEDIA,
    val category: String? = null,
    val isDone: Boolean = false,
    val createdAt: Long
)
