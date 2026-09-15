package com.aria.assistant.ui.screens.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aria.assistant.reminders.ReminderEntity
import com.aria.assistant.reminders.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    val reminders: StateFlow<List<ReminderEntity>> = reminderRepository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addReminder(message: String, hour: Int, minute: Int) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
            }
            reminderRepository.create(message.trim(), calendar.timeInMillis)
        }
    }

    fun cancel(reminder: ReminderEntity) {
        viewModelScope.launch { reminderRepository.cancel(reminder) }
    }
}
