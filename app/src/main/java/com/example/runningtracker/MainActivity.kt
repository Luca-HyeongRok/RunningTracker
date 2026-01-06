package com.example.runningtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import com.example.runningtracker.presentation.MainScreen
import com.example.runningtracker.presentation.running.RunningViewModel
import com.example.runningtracker.service.ServiceAction
import com.example.runningtracker.ui.theme.MyApplicationTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 액션을 ViewModel로 전달하는 메인 액티비티.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: RunningViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                MainScreen(
                    uiState = viewModel.uiState.collectAsState().value,
                    onAction = { action ->
                        when (action) {
                            ServiceAction.START -> viewModel.startRunning()
                            ServiceAction.PAUSE -> viewModel.pauseRunning()
                            ServiceAction.STOP -> viewModel.stopRunning()
                        }
                    }
                )
            }
        }
    }
}
