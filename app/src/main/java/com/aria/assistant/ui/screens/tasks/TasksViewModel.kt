package com.aria.assistant.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aria.assistant.tasks.TaskEntity
import com.aria.assistant.tasks.TaskPriority
import com.aria.assistant.tasks.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = taskRepository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTask(title: String, priority: TaskPriority) {
        if (title.isBlank()) return
        viewModelScope.launch { taskRepository.create(title.trim(), priority) }
    }

    fun toggleDone(task: TaskEntity) {
        viewModelScope.launch { taskRepository.toggleDone(task) }
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch { taskRepository.delete(task) }
    }
}
