package com.example.runningtracker.presentation.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtracker.location.LocationClient
import com.google.android.gms.maps.model.LatLng
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
class RunningViewModel(
    private val locationClient: LocationClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var locationJob: Job? = null

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

        startLocationUpdates()
    }

    /** ⏸ PAUSE */
    fun pauseRunning() {
        _uiState.update {
            it.copy(isTracking = false)
        }
        timerJob?.cancel()
        timerJob = null
        stopLocationUpdates()
    }

    /** ⏹ STOP */
    fun stopRunning() {
        timerJob?.cancel()
        timerJob = null
        stopLocationUpdates()
        _uiState.update {
            it.copy(
                isTracking = false,
                elapsedTimeMillis = 0L,
                path = emptyList()
            )
        }
    }

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            try {
                locationClient.getLocationUpdates(1000L).collect { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    _uiState.update { state ->
                        state.copy(path = state.path + latLng)
                    }
                }
            } catch (e: SecurityException) {
                stopLocationUpdates()
            }
        }
    }

    private fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        locationJob?.cancel()
    }
}
