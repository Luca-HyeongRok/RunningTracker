package com.example.runningtracker.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.runningtracker.domain.model.RunningResult
import com.example.runningtracker.util.formatTime
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    results: List<RunningResult>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "러닝 기록",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        HistoryList(results = results)
    }
}

@Composable
private fun HistoryList(
    results: List<RunningResult>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(results) { result ->
            HistoryItem(result = result)
        }
    }
}

@Composable
private fun HistoryItem(
    result: RunningResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formatDate(result.startTimeStamp.time),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "거리")
                    Text(
                        text = formatDistance(result.distanceInMeters),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column {
                    Text(text = "시간")
                    Text(
                        text = formatTime(result.totalTimeInMillis),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column {
                    Text(text = "평균 속도")
                    Text(
                        text = formatSpeed(result.avgSpeedInKMH),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

private fun formatDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
    return formatter.format(timeMillis)
}

private fun formatDistance(distanceMeters: Int): String {
    val km = distanceMeters / 1000f
    return String.format(Locale.KOREA, "%.2f km", km)
}

private fun formatSpeed(speedKmh: Float): String {
    return String.format(Locale.KOREA, "%.1f km/h", speedKmh)
}
