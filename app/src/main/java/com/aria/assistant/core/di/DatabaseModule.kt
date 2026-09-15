package com.aria.assistant.core.di

import android.content.Context
import androidx.room.Room
import com.aria.assistant.core.AriaDatabase
import com.aria.assistant.memory.MemoryDao
import com.aria.assistant.notes.NoteDao
import com.aria.assistant.reminders.ReminderDao
import com.aria.assistant.tasks.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAriaDatabase(@ApplicationContext context: Context): AriaDatabase =
        Room.databaseBuilder(context, AriaDatabase::class.java, "aria_local_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMemoryDao(database: AriaDatabase): MemoryDao = database.memoryDao()

    @Provides
    fun provideNoteDao(database: AriaDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideTaskDao(database: AriaDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideReminderDao(database: AriaDatabase): ReminderDao = database.reminderDao()
}
