package com.aria.assistant.ui.screens.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aria.assistant.memory.MemoryEntity
import com.aria.assistant.memory.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    val memories: StateFlow<List<MemoryEntity>> = memoryRepository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(entry: MemoryEntity) {
        viewModelScope.launch { memoryRepository.forget(entry) }
    }

    fun deleteAll() {
        viewModelScope.launch { memoryRepository.forgetAll() }
    }
}
