package com.aria.assistant.tasks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val dao: TaskDao
) {
    val allTasks: Flow<List<TaskEntity>> = dao.getAll()

    suspend fun create(title: String, priority: TaskPriority = TaskPriority.MEDIA, category: String? = null) {
        dao.insert(
            TaskEntity(
                title = title,
                priority = priority,
                category = category,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleDone(task: TaskEntity) {
        dao.update(task.copy(isDone = !task.isDone))
    }

    suspend fun delete(task: TaskEntity) = dao.delete(task)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun pendingTasks(): List<TaskEntity> = allTasks.first().filter { !it.isDone }

    /** Marca como completada la primera tarea pendiente cuyo título contenga la consulta. */
    suspend fun completeMatching(query: String): Boolean {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return false
        val match = allTasks.first().firstOrNull { !it.isDone && it.title.lowercase().contains(normalized) }
        return if (match != null) {
            dao.update(match.copy(isDone = true))
            true
        } else false
    }
}
