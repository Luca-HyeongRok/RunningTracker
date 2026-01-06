package com.example.runningtracker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.runningtracker.location.LocationPermissionHelper
import com.example.runningtracker.service.RunningService
import com.example.runningtracker.service.ServiceAction
import com.example.runningtracker.ui.theme.MyApplicationTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.concurrent.TimeUnit

/**
 * 앱의 메인 Activity
 * - RunningService와 바인딩
 * - Compose UI에 Service 상태 전달
 */
class MainActivity : ComponentActivity() {

    private var runningService: RunningService? = null
    private var isBound by mutableStateOf(false)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RunningService.LocalBinder
            runningService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            runningService = null
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, RunningService::class.java).also {
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                RunningScreen(
                    runningService = if (isBound) runningService else null,
                    onAction = ::sendCommandToService
                )
            }
        }
    }

    private fun sendCommandToService(action: ServiceAction) {
        Intent(this, RunningService::class.java).also {
            it.action = action.name
            startService(it)
        }
    }
}

@Composable
fun RunningScreen(
    runningService: RunningService?,
    onAction: (ServiceAction) -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    // Service state 수집
    val isTracking by runningService?.isTracking
        ?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    val path by runningService?.path
        ?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    val elapsedTime by runningService?.elapsedTimeInMillis
        ?.collectAsState(initial = 0L)
        ?: remember { mutableStateOf(0L) }

    // 권한 런처
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { }

    // 권한 체크
    LaunchedEffect(Unit) {
        if (!LocationPermissionHelper.hasLocationPermissions(context)) {
            LocationPermissionHelper.requestLocationPermissions(permissionLauncher)
        }
        if (!LocationPermissionHelper.hasNotificationPermissions(context)) {
            LocationPermissionHelper.requestNotificationPermission(permissionLauncher)
        }
    }

    // 러닝 중 화면 꺼짐 방지
    LaunchedEffect(isTracking) {
        activity?.window?.let { window ->
            if (isTracking) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RunningMap(path)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(elapsedTime),
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                ButtonControls(
                    isTracking = isTracking,
                    onStartClick = { onAction(ServiceAction.START) },
                    onPauseClick = { onAction(ServiceAction.PAUSE) },
                    onStopClick = { onAction(ServiceAction.STOP) }
                )
            }
        }
    }
}

@Composable
fun RunningMap(path: List<LatLng>) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            path.lastOrNull() ?: LatLng(37.5665, 126.9780),
            15f
        )
    }

    LaunchedEffect(path) {
        if (path.isNotEmpty()) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(path.last(), 15f),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        if (path.size > 1) {
            Polyline(
                points = path,
                color = MaterialTheme.colorScheme.primary,
                width = 15f
            )
        }
    }
}

@Composable
fun ButtonControls(
    isTracking: Boolean,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (!isTracking) {
            Button(onClick = onStartClick) {
                Text("시작")
            }
        } else {
            Button(onClick = onPauseClick) {
                Text("일시정지")
            }
            Button(onClick = onStopClick) {
                Text("종료")
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
