package com.aria.assistant.reminders

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler
) {
    val allReminders: Flow<List<ReminderEntity>> = dao.getAll()

    /** @return true si quedó como alarma exacta, false si se usó el respaldo inexacto. */
    suspend fun create(message: String, triggerAtMillis: Long): Boolean {
        val id = dao.insert(ReminderEntity(message = message, triggerAtMillis = triggerAtMillis))
        return scheduler.schedule(id, message, triggerAtMillis)
    }

    suspend fun cancel(reminder: ReminderEntity) {
        scheduler.cancel(reminder.id, reminder.message)
        dao.delete(reminder)
    }
}
