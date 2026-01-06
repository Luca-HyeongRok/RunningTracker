package com.example.runningtracker.util

import java.util.concurrent.TimeUnit


fun formatTime(timeMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeMillis)
    val minutes =
        TimeUnit.MILLISECONDS.toMinutes(timeMillis) -
                TimeUnit.HOURS.toMinutes(hours)
    val seconds =
        TimeUnit.MILLISECONDS.toSeconds(timeMillis) -
                TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(timeMillis)
                )

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

