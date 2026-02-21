package com.example.runningtracker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.runningtracker.battery.BatteryReceiver
import com.example.runningtracker.location.LocationPermissionHelper
import com.example.runningtracker.presentation.running.RunningEvent
import com.example.runningtracker.presentation.running.RunningViewModel
import com.example.runningtracker.service.RunningService
import com.example.runningtracker.service.ServiceAction
import com.example.runningtracker.ui.theme.RunningTrackerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 액션을 ViewModel로 전달하는 메인 액티비티.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: RunningViewModel by viewModel()
    private var pendingStartAfterPermission = false
    private var service: RunningService? = null
    private var isBound = false
    private var batteryReceiver: BatteryReceiver? = null
    private var isBatteryReceiverRegistered = false
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val granted = grants.values.any { it }
            if (granted && pendingStartAfterPermission) {
                viewModel.startRunning()
            } else if (pendingStartAfterPermission) {
                Toast.makeText(
                    this,
                    "위치 권한이 필요합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            pendingStartAfterPermission = false
        }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as RunningService.LocalBinder).getService()
            viewModel.attachService(service!!)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            service = null
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, RunningService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )

        if (!isBatteryReceiverRegistered) {
            batteryReceiver = BatteryReceiver { percent ->
                viewModel.onBatteryPercentChanged(percent)
            }
            registerReceiver(
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            isBatteryReceiverRegistered = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        if (isBatteryReceiverRegistered) {
            unregisterReceiver(batteryReceiver)
            batteryReceiver = null
            isBatteryReceiverRegistered = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current

            // Service Action 처리
            LaunchedEffect(Unit) {
                viewModel.serviceActions.collect { action ->
                    val intent = Intent(context, RunningService::class.java).apply {
                        this.action = action.name
                    }
                    if (action == ServiceAction.START) {
                        ContextCompat.startForegroundService(context, intent)
                    } else {
                        context.startService(intent)
                    }
                }
            }

            // UI 이벤트 (Toast)
            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is RunningEvent.ShowMessage -> {
                            Toast.makeText(
                                context,
                                event.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            RunningTrackerTheme {
                val uiState by viewModel.uiState.collectAsState()
                val onAction: (ServiceAction) -> Unit = { action ->
                    when (action) {
                        ServiceAction.START -> {
                            if (LocationPermissionHelper.hasLocationPermissions(this)) {
                                viewModel.startRunning()
                            } else {
                                pendingStartAfterPermission = true
                                LocationPermissionHelper.requestLocationPermissions(locationPermissionLauncher)
                            }
                        }
                        ServiceAction.PAUSE -> viewModel.pauseRunning()
                        ServiceAction.STOP -> viewModel.stopRunning()
                    }
                }
                
                com.example.runningtracker.presentation.MainScreen(
                    uiState = uiState,
                    onAction = onAction
                )
            }

        }
    }
}
