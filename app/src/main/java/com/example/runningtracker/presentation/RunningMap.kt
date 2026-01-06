package com.example.runningtracker.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline

@Composable
fun RunningMap(
    path: List<LatLng>,
    modifier: Modifier = Modifier
) {
    GoogleMap(
        modifier = modifier
    ) {
        if (path.isNotEmpty()) {
            Polyline(
                points = path
            )
        }
    }
}
