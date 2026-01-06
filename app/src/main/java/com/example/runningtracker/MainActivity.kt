package com.example.runningtracker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import com.example.runningtracker.presentation.MainScreen
import com.example.runningtracker.presentation.running.RunningViewModel
import com.example.runningtracker.service.RunningService
import com.example.runningtracker.service.ServiceAction
import com.example.runningtracker.ui.theme.MyApplicationTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 액션을 ViewModel로 전달하는 메인 액티비티.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: RunningViewModel by viewModel()
    private var service: RunningService? = null
    private var isBound = false

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
                MainScreen(
                    uiState = viewModel.uiState.collectAsState().value,
                    onAction = { action ->
                        val intent = Intent(this, RunningService::class.java).apply {
                            this.action = action.name
                        }
                        if (action == ServiceAction.START) {
                            ContextCompat.startForegroundService(this, intent)
                        } else {
                            startService(intent)
                        }
                    }
                )
            }
        }
    }
}
