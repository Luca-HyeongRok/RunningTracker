package com.example.runningtracker.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ButtonControls(
    isTracking: Boolean,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Row {
        if (!isTracking) {
            Button(onClick = onStartClick) {
                Text("Start")
            }
        } else {
            Button(onClick = onPauseClick) {
                Text("Pause")
            }
            Button(onClick = onStopClick) {
                Text("Stop")
            }
        }
    }
}
