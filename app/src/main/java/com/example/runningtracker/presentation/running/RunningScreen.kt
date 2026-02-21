package com.example.runningtracker.presentation.running

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runningtracker.presentation.component.ButtonControls
import com.example.runningtracker.presentation.component.RunningMap
import com.example.runningtracker.service.ServiceAction
import com.example.runningtracker.util.DistanceCalculator
import com.example.runningtracker.util.formatTime
import java.util.Locale

@Composable
fun RunningScreen(
    uiState: RunningUiState,
    onAction: (ServiceAction) -> Unit
) {
    val distanceMeters = DistanceCalculator.calculatePolylineDistance(uiState.path)
    val distanceKm = distanceMeters / 1000f
    
    // Pace calculation: min/km
    val paceString = if (distanceKm > 0 && uiState.elapsedTimeMillis > 0) {
        val totalMinutes = (uiState.elapsedTimeMillis / 1000f) / 60f
        val paceDecimal = totalMinutes / distanceKm
        val paceMinutes = paceDecimal.toInt()
        val paceSeconds = ((paceDecimal - paceMinutes) * 60).toInt()
        String.format(Locale.KOREA, "%d'%02d\"", paceMinutes, paceSeconds)
    } else {
        "0'00\""
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full screen map
        RunningMap(
            path = uiState.path,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom Info Panel
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DISTANCE",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.KOREA, "%.2f", distanceKm),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "km",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AVG PACE",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = paceString,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "/km",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Large Timer Text
                Text(
                    text = formatTime(uiState.elapsedTimeMillis),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Button Controls
                ButtonControls(
                    isTracking = uiState.isTracking,
                    onStartClick = { onAction(ServiceAction.START) },
                    onPauseClick = { onAction(ServiceAction.PAUSE) },
                    onStopClick = { onAction(ServiceAction.STOP) }
                )
            }
        }
    }
}
