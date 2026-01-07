package com.example.runningtracker.service

import android.os.SystemClock
import kotlinx.coroutines.*

class RunTimer(
    private val scope: CoroutineScope,
    private val onTick: (Long) -> Unit
) {
    private var job: Job? = null
    private var timeStarted = 0L
    private var accumulated = 0L

    fun start() {
        timeStarted = SystemClock.elapsedRealtime()
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                val lap = SystemClock.elapsedRealtime() - timeStarted
                onTick(accumulated + lap)
                delay(200L)
            }
        }
    }

    fun pause() {
        accumulated += SystemClock.elapsedRealtime() - timeStarted
        job?.cancel()
    }

    fun stop() {
        accumulated = 0L
        job?.cancel()
    }
}
