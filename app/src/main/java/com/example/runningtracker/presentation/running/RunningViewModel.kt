package com.example.runningtracker.presentation.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtracker.service.RunningService
import com.example.runningtracker.service.ServiceAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Service 상태를 UI 상태로 중계하고
 * 러닝 관련 비즈니스 규칙(배터리 조건 등)을 판단하는 ViewModel.
 */
class RunningViewModel : ViewModel() {

    /* ---------- UI STATE ---------- */

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    /* ---------- SERVICE ACTION ---------- */

    private val _serviceActions =
        MutableSharedFlow<ServiceAction>(extraBufferCapacity = 1)
    val serviceActions: SharedFlow<ServiceAction> =
        _serviceActions.asSharedFlow()

    /* ---------- UI EVENT ---------- */

    private val _events =
        MutableSharedFlow<RunningEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<RunningEvent> =
        _events.asSharedFlow()

    /* ---------- SERVICE ---------- */

    private var service: RunningService? = null
    private var observeJob: Job? = null

    /* ---------- BATTERY STATE ---------- */

    private var lastBatteryPercent: Int? = null
    private var lastIsTracking: Boolean = false
    private var hasWarnedLowBatteryAtStart = false
    private var lowBatteryStopTriggered = false

    /* ---------- SERVICE BIND ---------- */

    fun attachService(service: RunningService) {
        this.service = service
        observeService()
    }

    /* ---------- BATTERY CALLBACK ---------- */

    fun onBatteryPercentChanged(percent: Int) {
        lastBatteryPercent = percent

        // 운동 중 배터리 20% 이하 → 강제 종료
        if (lastIsTracking && percent <= 20 && !lowBatteryStopTriggered) {
            lowBatteryStopTriggered = true
            stopRunning()
        }
    }

    /* ---------- USER ACTION ---------- */

    fun startRunning() {
        // 시작 시 배터리 30% 이하 경고 (운동은 허용)
        if (!hasWarnedLowBatteryAtStart) {
            val percent = lastBatteryPercent
            if (percent != null && percent <= 30) {
                hasWarnedLowBatteryAtStart = true
                emitEvent(
                    RunningEvent.ShowMessage("배터리가 부족합니다")
                )
            }
        }
        emitServiceAction(ServiceAction.START)
    }

    fun pauseRunning() {
        emitServiceAction(ServiceAction.PAUSE)
    }

    fun stopRunning() {
        hasWarnedLowBatteryAtStart = false
        lowBatteryStopTriggered = false
        emitServiceAction(ServiceAction.STOP)
    }

    /* ---------- SERVICE OBSERVE ---------- */

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
            }.collect { newState ->
                _uiState.value = newState
                lastIsTracking = newState.isTracking
            }
        }
    }

    /* ---------- EMIT HELPERS ---------- */

    private fun emitServiceAction(action: ServiceAction) {
        viewModelScope.launch {
            _serviceActions.emit(action)
        }
    }

    private fun emitEvent(event: RunningEvent) {
        _events.tryEmit(event)
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}
