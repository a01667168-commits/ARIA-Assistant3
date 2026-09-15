package com.aria.assistant.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aria.assistant.weather.WeatherRepository
import com.aria.assistant.weather.WeatherResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _result = MutableStateFlow<WeatherResult?>(null)
    val result: StateFlow<WeatherResult?> = _result.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun refresh(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _result.value = weatherRepository.currentWeather(forceRefresh)
            _isLoading.value = false
        }
    }
}
