package com.aria.assistant.tasks

import androidx.room.TypeConverter

class TaskConverters {
    @TypeConverter
    fun fromPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): TaskPriority =
        runCatching { TaskPriority.valueOf(value) }.getOrDefault(TaskPriority.MEDIA)
}
