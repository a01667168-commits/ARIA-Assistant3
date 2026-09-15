package com.aria.assistant.memory

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_entries ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MemoryEntity>>

    @Insert
    suspend fun insert(entry: MemoryEntity)

    @Delete
    suspend fun delete(entry: MemoryEntity)

    @Query("DELETE FROM memory_entries")
    suspend fun deleteAll()
}
