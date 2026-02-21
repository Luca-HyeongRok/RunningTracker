package com.example.runningtracker.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runningtracker.domain.model.RunningResult
import com.example.runningtracker.util.formatTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun HistoryScreen(
    results: List<RunningResult>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Running History",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "${results.size} activities",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header: Title and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = buildRunTitle(result.startTimeStamp.time),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = formatDate(result.startTimeStamp.time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(
                    label = "DISTANCE",
                    value = formatDistance(result.distanceInMeters),
                    unit = "km",
                    modifier = Modifier.weight(1f)
                )
                StatColumn(
                    label = "DURATION",
                    value = formatTime(result.totalTimeInMillis),
                    unit = "",
                    modifier = Modifier.weight(1.2f)
                )
                StatColumn(
                    label = "AVG SPEED",
                    value = formatSpeed(result.avgSpeedInKMH),
                    unit = "km/h",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value.replace(unit, "").trim(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        )
    }
}

private fun formatDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
    return formatter.format(timeMillis)
}

private fun buildRunTitle(timeMillis: Long): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA).apply {
        timeInMillis = timeMillis
    }
    val day = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "SUNDAY"
        Calendar.MONDAY -> "MONDAY"
        Calendar.TUESDAY -> "TUESDAY"
        Calendar.WEDNESDAY -> "WEDNESDAY"
        Calendar.THURSDAY -> "THURSDAY"
        Calendar.FRIDAY -> "FRIDAY"
        else -> "SATURDAY"
    }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val period = when (hour) {
        in 5..11 -> "MORNING"
        in 12..17 -> "AFTERNOON"
        in 18..21 -> "EVENING"
        else -> "NIGHT"
    }
    return "$day $period RUN"
}

private fun formatDistance(distanceMeters: Int): String {
    val km = distanceMeters / 1000f
    return String.format(Locale.KOREA, "%.2f km", km)
}

private fun formatSpeed(speedKmh: Float): String {
    return String.format(Locale.KOREA, "%.1f km/h", speedKmh)
}
