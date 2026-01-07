package com.example.runningtracker.service

import android.content.Context
import com.example.runningtracker.location.LocationClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*

class RunLocationTracker(
    private val scope: CoroutineScope,
    context: Context,
    private val onLocation: (LatLng) -> Unit
) {
    private val client = LocationClient(
        context,
        LocationServices.getFusedLocationProviderClient(context)
    )

    private var job: Job? = null

    fun start() {
        job?.cancel()
        job = scope.launch {
            try {
                client.getLocationUpdates(1000L).collect { location ->
                    onLocation(LatLng(location.latitude, location.longitude))
                }
            } catch (_: SecurityException) {}
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
