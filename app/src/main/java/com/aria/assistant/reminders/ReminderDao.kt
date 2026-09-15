package com.aria.assistant.reminders

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY triggerAtMillis ASC")
    fun getAll(): Flow<List<ReminderEntity>>

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Delete
    suspend fun delete(reminder: ReminderEntity)
}
