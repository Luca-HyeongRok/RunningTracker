package com.example.runningtracker.presentation.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 러닝 타이머 상태를 관리하고 UI 상태로 제공하는 ViewModel.
 *
 * 규칙:
 * - Start: 타이머 시작 (이미 시간이 있으면 이어서)
 * - Pause: 타이머 정지 (시간 유지)
 * - Stop: 타이머 정지 + 시간 초기화
 */
class RunningViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    /** ▶ START */
    fun startRunning() {
        if (_uiState.value.isTracking) return

        _uiState.update {
            it.copy(isTracking = true)
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _uiState.update { state ->
                    if (state.isTracking) {
                        state.copy(
                            elapsedTimeMillis = state.elapsedTimeMillis + 1000L
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    /** ⏸ PAUSE */
    fun pauseRunning() {
        _uiState.update {
            it.copy(isTracking = false)
        }
        timerJob?.cancel()
        timerJob = null
    }

    /** ⏹ STOP */
    fun stopRunning() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            it.copy(
                isTracking = false,
                elapsedTimeMillis = 0L
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
