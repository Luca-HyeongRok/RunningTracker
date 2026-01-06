package com.example.runningtracker.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.runningtracker.data.local.RunningDatabase
import com.example.runningtracker.data.repository.RunningRepositoryImpl
import com.example.runningtracker.domain.model.RunningResult
import com.example.runningtracker.location.LocationClient
import com.example.runningtracker.util.DistanceCalculator
import com.example.runningtracker.util.NotificationUtil
import com.example.runningtracker.util.formatTime
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Date

/**
 * 러닝 측정 상태를 유지하고
 * 타이머 / 위치 수집 / 기록 저장을 담당하는 포그라운드 서비스
 */
class RunningService : LifecycleService() {

    private val binder = LocalBinder()

    // --- 상태 ---
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _path = MutableStateFlow<List<LatLng>>(emptyList())
    val path: StateFlow<List<LatLng>> = _path.asStateFlow()

    // --- 내부 변수 ---
    private var timerJob: Job? = null
    private var locationJob: Job? = null

    private var timeStarted = 0L
    private var timeRun = 0L
    private var lastSecondTimestamp = 0L
    private var currentRunStartTime: Date? = null
    private var stopHandled = false

    // --- 의존성 ---
    private val dao by lazy {
        RunningDatabase.getDatabase(applicationContext).runningDao()
    }
    private val repository by lazy {
        RunningRepositoryImpl(dao)
    }
    private val locationClient by lazy {
        LocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    private var notificationBuilder: NotificationCompat.Builder? = null
    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    override fun onCreate() {
        super.onCreate()
        NotificationUtil.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            restoreStateIfNeeded()
            return START_STICKY
        }
        when (intent?.action) {
            ServiceAction.START.name -> start()
            ServiceAction.PAUSE.name -> pause()
            ServiceAction.STOP.name -> stop()
        }
        return START_STICKY
    }

    // -----------------------------
    // 러닝 제어
    // -----------------------------

    private fun start() {
        if (_isTracking.value) return

        stopHandled = false
        if (currentRunStartTime == null) {
            currentRunStartTime = Date()
        }

        _isTracking.value = true
        persistState()
        startForegroundNotification()
        startTimer()
        startLocationUpdates()

        Log.d("RunningService", "START")
    }

    private fun pause() {
        if (!_isTracking.value) return

        _isTracking.value = false
        timeRun = _elapsedTime.value
        persistState()

        timerJob?.cancel()
        locationJob?.cancel()

        updateNotification(isPaused = true)

        Log.d("RunningService", "PAUSE")
    }

    private fun stop() {
        if (stopHandled) return
        stopHandled = true

        _isTracking.value = false
        timerJob?.cancel()
        locationJob?.cancel()
        persistState()

        val elapsedSnapshot = _elapsedTime.value
        val pathSnapshot = _path.value
        val startTimeSnapshot = currentRunStartTime

        Log.d(
            "RunningService",
            "STOP clicked | elapsed=$elapsedSnapshot | pathSize=${pathSnapshot.size}"
        )

        lifecycleScope.launch(Dispatchers.IO) {
            saveRunResult(
                pathSnapshot = pathSnapshot,
                elapsedSnapshot = elapsedSnapshot,
                startTimeSnapshot = startTimeSnapshot
            )
            withContext(Dispatchers.Main) {
                resetAndStop()
            }
        }
    }

    // -----------------------------
    // 타이머
    // -----------------------------

    private fun startTimer() {
        timeStarted = SystemClock.elapsedRealtime()
        lastSecondTimestamp = (_elapsedTime.value / 1000L) * 1000L

        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            while (isActive && _isTracking.value) {
                val lap = SystemClock.elapsedRealtime() - timeStarted
                val total = timeRun + lap
                _elapsedTime.value = total

                if (total >= lastSecondTimestamp + 1000L) {
                    lastSecondTimestamp += 1000L
                    persistState()
                    updateNotification(isPaused = false)
                }

                delay(200L)
            }
        }
    }

    // -----------------------------
    // 위치 수집
    // -----------------------------

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = lifecycleScope.launch {
            try {
                locationClient.getLocationUpdates(1000L).collect { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    _path.value = _path.value + latLng
                }
            } catch (e: SecurityException) {
                Log.w("RunningService", "Location permission missing", e)
            } catch (e: Exception) {
                Log.w("RunningService", "Location updates failed", e)
            }
        }
    }

    // -----------------------------
    // 저장 로직
    // -----------------------------

    private suspend fun saveRunResult(
        pathSnapshot: List<LatLng>,
        elapsedSnapshot: Long,
        startTimeSnapshot: Date?
    ) {
        //  시간 자체가 없으면 저장 안 함
        if (elapsedSnapshot <= 0L) {
            Log.w("RunningService", "SAVE SKIPPED: elapsed time is zero")
            return
        }

        // 위치 없어도 저장 허용
        val distanceMeters =
            if (pathSnapshot.size >= 2) {
                DistanceCalculator
                    .calculatePolylineDistance(pathSnapshot)
                    .toInt()
            } else {
                0
            }

        val avgSpeed =
            if (distanceMeters > 0) {
                val hours = elapsedSnapshot / 1000f / 3600f
                (distanceMeters / 1000f) / hours
            } else {
                0f
            }

        val result = RunningResult(
            startTimeStamp = startTimeSnapshot ?: Date(),
            totalTimeInMillis = elapsedSnapshot,
            avgSpeedInKMH = avgSpeed,
            distanceInMeters = distanceMeters
        )

        repository.insertRunningResult(result)

        Log.d(
            "RunningService",
            "RUN SAVED ✔ elapsed=$elapsedSnapshot, distance=$distanceMeters"
        )
    }

    // -----------------------------
    // 알림
    // -----------------------------

    private fun startForegroundNotification() {
        val builder = notificationBuilder
            ?: NotificationUtil.createNotification(this).also {
                notificationBuilder = it
            }

        val notification = builder
            .setContentText("운동 중 · ${formatTime(_elapsedTime.value)}")
            .build()

        startForeground(NotificationUtil.NOTIFICATION_ID, notification)
    }

    private fun updateNotification(isPaused: Boolean) {
        val builder = notificationBuilder ?: return
        val status = if (isPaused) "일시정지" else "운동 중"

        val notification = builder
            .setContentText("$status · ${formatTime(_elapsedTime.value)}")
            .build()

        NotificationManagerCompat
            .from(this)
            .notify(NotificationUtil.NOTIFICATION_ID, notification)
    }

    // -----------------------------
    // 종료 처리
    // -----------------------------

    private fun resetAndStop() {
        _elapsedTime.value = 0L
        _path.value = emptyList()
        _isTracking.value = false
        timeRun = 0L
        currentRunStartTime = null
        clearState()

        stopForeground(true)
        stopSelf()
    }

    // -----------------------------
    // Binder
    // -----------------------------

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): RunningService = this@RunningService
    }

    private fun restoreStateIfNeeded(): Boolean {
        val wasTracking = prefs.getBoolean(KEY_IS_TRACKING, false)
        val savedElapsedTime = prefs.getLong(KEY_ELAPSED_TIME, 0L)
        val savedStartTime = prefs.getLong(KEY_START_TIME, 0L)

        if (savedElapsedTime > 0L) {
            _elapsedTime.value = savedElapsedTime
            timeRun = savedElapsedTime
        }
        if (savedStartTime > 0L) {
            currentRunStartTime = Date(savedStartTime)
        }

        if (wasTracking) {
            stopHandled = false
            _isTracking.value = true
            startForegroundNotification()
            startTimer()
            startLocationUpdates()
            return true
        }
        return false
    }

    private fun persistState() {
        prefs.edit()
            .putBoolean(KEY_IS_TRACKING, _isTracking.value)
            .putLong(KEY_ELAPSED_TIME, _elapsedTime.value)
            .putLong(KEY_START_TIME, currentRunStartTime?.time ?: 0L)
            .apply()
    }

    private fun clearState() {
        prefs.edit().clear().apply()
    }

    private companion object {
        private const val PREFS_NAME = "running_service_state"
        private const val KEY_IS_TRACKING = "key_is_tracking"
        private const val KEY_ELAPSED_TIME = "key_elapsed_time"
        private const val KEY_START_TIME = "key_start_time"
    }
}
