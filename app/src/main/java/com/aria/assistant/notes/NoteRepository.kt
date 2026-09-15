package com.aria.assistant.notes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val dao: NoteDao
) {
    val allNotes: Flow<List<NoteEntity>> = dao.getAll()

    suspend fun create(content: String) {
        dao.insert(NoteEntity(content = content, createdAt = System.currentTimeMillis()))
    }

    suspend fun delete(note: NoteEntity) = dao.delete(note)

    suspend fun deleteAll() = dao.deleteAll()

    /** Busca y borra la primera nota cuyo texto contenga la consulta (case-insensitive). */
    suspend fun deleteMatching(query: String): Boolean {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return false
        val match = allNotes.first().firstOrNull { it.content.lowercase().contains(normalized) }
        return if (match != null) {
            dao.delete(match)
            true
        } else false
    }
}
