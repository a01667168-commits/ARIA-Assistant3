package com.aria.assistant.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un "recuerdo" que el usuario permitió guardar explícitamente
 * (ej. "ARIA, recuerda que mi equipo entrena los martes").
 * Almacenado localmente (Room). Se implementa completo en la Fase 6.
 */
@Entity(tableName = "memory_entries")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val createdAt: Long
)
