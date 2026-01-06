package com.example.runningtracker.presentation.running

import com.google.android.gms.maps.model.LatLng

data class RunningUiState(
    val isTracking: Boolean = false,
    val elapsedTime: Long = 0L,
    val path: List<LatLng> = emptyList()
)
