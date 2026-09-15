package com.aria.assistant.notes

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Una nota creada por el usuario (sección 9), persistida localmente. */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val createdAt: Long
)
