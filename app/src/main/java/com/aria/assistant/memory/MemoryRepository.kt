package com.aria.assistant.memory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Único punto de acceso a la memoria local de A.R.I.A. (sección 5).
 * Todo lo que el usuario permite recordar pasa por aquí; nunca se guarda
 * nada sin que el usuario lo haya pedido explícitamente con un comando.
 */
@Singleton
class MemoryRepository @Inject constructor(
    private val dao: MemoryDao
) {
    val allMemories: Flow<List<MemoryEntity>> = dao.getAll()

    suspend fun remember(content: String) {
        dao.insert(MemoryEntity(content = content, createdAt = System.currentTimeMillis()))
    }

    suspend fun forget(entry: MemoryEntity) {
        dao.delete(entry)
    }

    suspend fun forgetAll() {
        dao.deleteAll()
    }

    /** Borrado por coincidencia de texto (case-insensitive). Devuelve cuántos recuerdos borró. */
    suspend fun forgetMatching(query: String): Int {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) return 0
        val matches = allMemories.first().filter { it.content.lowercase().contains(normalizedQuery) }
        matches.forEach { dao.delete(it) }
        return matches.size
    }
}
