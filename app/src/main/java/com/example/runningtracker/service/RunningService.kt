package com.example.runningtracker.service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.runningtracker.data.local.RunningDatabase
import com.example.runningtracker.data.repository.RunningRepositoryImpl
import com.example.runningtracker.domain.model.RunningResult
import com.example.runningtracker.location.LocationPermissionHelper
import com.example.runningtracker.util.DistanceCalculator
import com.example.runningtracker.util.NotificationUtil
import com.example.runningtracker.util.formatTime
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * ?щ떇 痢≪젙 ?곹깭瑜??좎??섍퀬
 * ??대㉧ / ?꾩튂 ?섏쭛 / 湲곕줉 ??μ쓣 ?대떦?섎뒗 ?ш렇?쇱슫???쒕퉬??
 */
class RunningService : LifecycleService() {

    private val binder = LocalBinder()

    // -----------------------------
    // ?곹깭
    // -----------------------------

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _path = MutableStateFlow<List<LatLng>>(emptyList())
    val path: StateFlow<List<LatLng>> = _path.asStateFlow()

    // -----------------------------
    // ?대? 而댄룷?뚰듃
    // -----------------------------

    private lateinit var runTimer: RunTimer
    private lateinit var locationTracker: RunLocationTracker

    private var currentRunStartTime: Date? = null
    private var stopHandled = false

    // -----------------------------
    // ?섏〈??
    // -----------------------------

    private val dao by lazy {
        RunningDatabase.getDatabase(applicationContext).runningDao()
    }

    private val repository by lazy {
        RunningRepositoryImpl(dao)
    }

    private var notificationBuilder: NotificationCompat.Builder? = null
    private val prefs by lazy { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }

    // -----------------------------
    // Lifecycle
    // -----------------------------

    override fun onCreate() {
        super.onCreate()

        NotificationUtil.createNotificationChannel(this)

        runTimer = RunTimer(
            scope = lifecycleScope,
            onTick = { millis ->
                _elapsedTime.value = millis
                updateNotification(isPaused = false)
                persistState()
            }
        )

        locationTracker = RunLocationTracker(
            scope = lifecycleScope,
            context = applicationContext,
            onLocation = { latLng ->
                _path.value = _path.value + latLng
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent == null) {
            restoreStateIfNeeded()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ServiceAction.START.name -> start()
            ServiceAction.PAUSE.name -> pause()
            ServiceAction.STOP.name -> stop()
        }

        return START_NOT_STICKY
    }

    // -----------------------------
    // ?щ떇 ?쒖뼱
    // -----------------------------

    private fun start() {
        if (_isTracking.value) return
        if (!LocationPermissionHelper.hasLocationPermissions(this)) {
            Log.w("RunningService", "START ignored: location permission not granted")
            stopSelf()
            return
        }

        stopHandled = false
        if (currentRunStartTime == null) {
            currentRunStartTime = Date()
        }

        _isTracking.value = true
        persistState()

        val started = startForegroundNotification()
        if (!started) {
            _isTracking.value = false
            persistState()
            stopSelf()
            return
        }
        runTimer.start()
        locationTracker.start()

        Log.d("RunningService", "START")
    }

    private fun pause() {
        if (!_isTracking.value) return

        _isTracking.value = false
        persistState()

        runTimer.pause()
        locationTracker.stop()
        updateNotification(isPaused = true)

        Log.d("RunningService", "PAUSE")
    }

    private fun stop() {
        if (stopHandled) return
        stopHandled = true

        _isTracking.value = false
        persistState()

        runTimer.stop()
        locationTracker.stop()

        val elapsedSnapshot = _elapsedTime.value
        val pathSnapshot = _path.value
        val startTimeSnapshot = currentRunStartTime

        lifecycleScope.launch(Dispatchers.IO) {
            saveRunResult(
                elapsedSnapshot = elapsedSnapshot,
                pathSnapshot = pathSnapshot,
                startTimeSnapshot = startTimeSnapshot
            )
            withContext(Dispatchers.Main) {
                resetAndStop()
            }
        }
    }

    // -----------------------------
    // ???
    // -----------------------------

    private suspend fun saveRunResult(
        elapsedSnapshot: Long,
        pathSnapshot: List<LatLng>,
        startTimeSnapshot: Date?
    ) {
        if (elapsedSnapshot <= 0L) return

        val distanceMeters =
            if (pathSnapshot.size >= 2) {
                DistanceCalculator
                    .calculatePolylineDistance(pathSnapshot)
                    .toInt()
            } else 0

        val avgSpeed =
            if (distanceMeters > 0) {
                val hours = elapsedSnapshot / 1000f / 3600f
                (distanceMeters / 1000f) / hours
            } else 0f

        repository.insertRunningResult(
            RunningResult(
                startTimeStamp = startTimeSnapshot ?: Date(),
                totalTimeInMillis = elapsedSnapshot,
                avgSpeedInKMH = avgSpeed,
                distanceInMeters = distanceMeters
            )
        )
    }

    // -----------------------------
    // Notification
    // -----------------------------

    private fun startForegroundNotification(): Boolean {
        val builder = notificationBuilder
            ?: NotificationUtil.createNotification(this).also {
                notificationBuilder = it
            }

        return try {
            startForeground(
                NotificationUtil.NOTIFICATION_ID,
                builder.setContentText("\uC774\uB3D9 \uC911 \u23F1 ${formatTime(_elapsedTime.value)}").build()
            )
            true
        } catch (e: SecurityException) {
            Log.e("RunningService", "Failed to start foreground service", e)
            false
        } catch (e: RuntimeException) {
            Log.e("RunningService", "Foreground service start not allowed or failed", e)
            false
        }
    }

    private fun updateNotification(isPaused: Boolean) {
        val builder = notificationBuilder ?: return
        val status = if (isPaused) "Paused" else "Tracking"
        if (!hasNotificationPermission()) return

        try {
            NotificationManagerCompat.from(this).notify(
                NotificationUtil.NOTIFICATION_ID,
                builder.setContentText("$status 쨌 ${formatTime(_elapsedTime.value)}").build()
            )
        } catch (_: SecurityException) {
            // Permission can be revoked while service is running.
        }
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // -----------------------------
    // 醫낅즺 / 蹂듦뎄
    // -----------------------------

    private fun resetAndStop() {
        _elapsedTime.value = 0L
        _path.value = emptyList()
        _isTracking.value = false
        currentRunStartTime = null

        clearState()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun restoreStateIfNeeded() {
        val wasTracking = prefs.getBoolean(KEY_IS_TRACKING, false)
        val savedElapsedTime = prefs.getLong(KEY_ELAPSED_TIME, 0L)
        val savedStartTime = prefs.getLong(KEY_START_TIME, 0L)

        if (savedElapsedTime > 0L) {
            _elapsedTime.value = savedElapsedTime
        }

        if (savedStartTime > 0L) {
            currentRunStartTime = Date(savedStartTime)
        }

        if (wasTracking) {
            // API 34+ foreground restrictions: do not auto-restart tracking here.
            _isTracking.value = false
            persistState()
        }
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

    private companion object {
        private const val PREFS_NAME = "running_service_state"
        private const val KEY_IS_TRACKING = "key_is_tracking"
        private const val KEY_ELAPSED_TIME = "key_elapsed_time"
        private const val KEY_START_TIME = "key_start_time"
    }
}

