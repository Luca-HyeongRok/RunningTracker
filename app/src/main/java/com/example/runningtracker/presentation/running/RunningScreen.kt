package com.example.runningtracker.presentation.running

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runningtracker.presentation.ButtonControls
import com.example.runningtracker.presentation.RunningMap
import com.example.runningtracker.util.TimeFormatter

@Composable
fun RunningScreen(
    viewModel: RunningViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = TimeFormatter.formatTime(uiState.elapsedTime),
            modifier = Modifier.padding(16.dp)
        )

        RunningMap(
            path = uiState.path,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ButtonControls(
            isTracking = uiState.isTracking,
            onStartClick = viewModel::onStart,
            onPauseClick = viewModel::onPause,
            onStopClick = viewModel::onStop
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
