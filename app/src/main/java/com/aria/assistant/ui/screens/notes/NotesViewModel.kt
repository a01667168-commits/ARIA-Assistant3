package com.aria.assistant.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aria.assistant.notes.NoteEntity
import com.aria.assistant.notes.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    val notes: StateFlow<List<NoteEntity>> = noteRepository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addNote(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch { noteRepository.create(content.trim()) }
    }

    fun delete(note: NoteEntity) {
        viewModelScope.launch { noteRepository.delete(note) }
    }
}
