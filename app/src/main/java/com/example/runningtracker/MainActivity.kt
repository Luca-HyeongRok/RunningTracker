package com.example.runningtracker

import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import com.example.runningtracker.presentation.MainScreen
import com.example.runningtracker.presentation.running.RunningViewModel
import com.example.runningtracker.service.RunningService
import com.example.runningtracker.ui.theme.MyApplicationTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 서비스 바인딩과 액션 전달을 담당하는 메인 액티비티.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: RunningViewModel by viewModel()
    private var service: RunningService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as RunningService.LocalBinder).getService()
            viewModel.attachService(service!!)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
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
        unbindService(connection)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                MainScreen(
                    uiState = viewModel.uiState.collectAsState().value,
                    onAction = { action ->
                        Intent(this, RunningService::class.java).also {
                            it.action = action.name
                            startService(it)
                        }
                    }
                )
            }
        }
    }
}
