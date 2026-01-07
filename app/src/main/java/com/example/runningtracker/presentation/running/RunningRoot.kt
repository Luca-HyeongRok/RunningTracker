package com.example.runningtracker.presentation.running

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.example.runningtracker.service.ServiceAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun RunningRoot(
    viewModel: RunningViewModel = koinViewModel()
) {
    RunningScreen(
        uiState = viewModel.uiState.collectAsState().value,
        onAction = { action ->
            when (action) {
                ServiceAction.START -> viewModel.startRunning()
                ServiceAction.PAUSE -> viewModel.pauseRunning()
                ServiceAction.STOP -> viewModel.stopRunning()
            }
        }
    )
}
