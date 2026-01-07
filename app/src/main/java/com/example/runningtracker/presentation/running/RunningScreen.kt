package com.example.runningtracker.presentation.running

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.runningtracker.presentation.component.ButtonControls
import com.example.runningtracker.presentation.component.RunningMap
import com.example.runningtracker.service.ServiceAction
import com.example.runningtracker.util.formatTime

@Composable
fun RunningScreen(
    uiState: RunningUiState,
    onAction: (ServiceAction) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // 지도 + 경로
        RunningMap(
            path = uiState.path,
            modifier = Modifier.fillMaxSize()
        )

        // 하단 컨트롤 영역
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 경과 시간 표시
            Text(
                text = formatTime(uiState.elapsedTimeMillis),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 버튼 영역
            ButtonControls(
                isTracking = uiState.isTracking,
                onStartClick = { onAction(ServiceAction.START) },
                onPauseClick = { onAction(ServiceAction.PAUSE) },
                onStopClick = { onAction(ServiceAction.STOP) }
            )
        }
    }
}
