package com.example.runningtracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtracker.domain.model.RunningResult
import com.example.runningtracker.domain.repository.RunningRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * 저장된 러닝 기록 목록을 UI에 제공하는 뷰모델.
 */
class HistoryViewModel(
    repository: RunningRepository
) : ViewModel() {

    val history: StateFlow<List<RunningResult>> =
        repository.getAllRunningResults()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
