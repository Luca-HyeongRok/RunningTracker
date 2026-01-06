package com.example.runningtracker.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RunningService : Service() {

    private val binder = LocalBinder()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime = _elapsedTime.asStateFlow()

    private var timerJob: Job? = null
    private var startTime = 0L

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ServiceAction.START.name -> start()
            ServiceAction.PAUSE.name -> pause()
            ServiceAction.STOP.name -> stop()
        }
        return START_STICKY
    }

    private fun start() {
        if (_isRunning.value) return
        _isRunning.value = true
        startTime = SystemClock.elapsedRealtime()

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && _isRunning.value) {
                _elapsedTime.value =
                    SystemClock.elapsedRealtime() - startTime
                delay(1000)
            }
        }
    }

    private fun pause() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    private fun stop() {
        _isRunning.value = false
        timerJob?.cancel()
        _elapsedTime.value = 0L
        stopSelf()
    }

    inner class LocalBinder : Binder() {
        fun getService(): RunningService = this@RunningService
    }
}
