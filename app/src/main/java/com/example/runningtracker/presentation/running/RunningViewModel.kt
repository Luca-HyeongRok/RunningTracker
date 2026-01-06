package com.example.runningtracker.presentation.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtracker.service.RunningService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Service 상태를 UI 상태로 중계하는 ViewModel.
 */
class RunningViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    private var service: RunningService? = null
    private var observeJob: Job? = null

    fun attachService(service: RunningService) {
        this.service = service
        observeService()
    }

    private fun observeService() {
        val svc = service ?: return
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                svc.isTracking,
                svc.elapsedTime,
                svc.path
            ) { isTracking, elapsedTime, path ->
                RunningUiState(
                    isTracking = isTracking,
                    elapsedTimeMillis = elapsedTime,
                    path = path
                )
            }.collect { _uiState.value = it }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}
