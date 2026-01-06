package com.example.runningtracker.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import com.example.runningtracker.presentation.history.HistoryScreen
import com.example.runningtracker.presentation.history.HistoryViewModel
import com.example.runningtracker.presentation.running.RunningScreen
import com.example.runningtracker.presentation.running.RunningUiState
import com.example.runningtracker.service.ServiceAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    uiState: RunningUiState,
    onAction: (ServiceAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val historyViewModel: HistoryViewModel = koinViewModel()
    val history by historyViewModel.history.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("러닝") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("기록") }
            )
        }

        when (selectedTab) {
            0 -> RunningScreen(uiState = uiState, onAction = onAction)
            else -> HistoryScreen(results = history)
        }
    }
}
