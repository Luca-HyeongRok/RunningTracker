package com.example.runningtracker.presentation.running

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RunningViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState

    fun onStart() {
        _uiState.value = _uiState.value.copy(isTracking = true)
    }

    fun onPause() {
        _uiState.value = _uiState.value.copy(isTracking = false)
    }

    fun onStop() {
        _uiState.value = RunningUiState()
    }
}
