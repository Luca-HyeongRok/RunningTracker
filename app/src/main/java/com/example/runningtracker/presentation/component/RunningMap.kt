package com.example.runningtracker.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.example.runningtracker.location.LocationPermissionHelper
import com.example.runningtracker.ui.theme.PrimaryPurple

@Composable
fun RunningMap(
    path: List<LatLng>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasLocationPermission = LocationPermissionHelper.hasLocationPermissions(context)

    // 기본 카메라 위치 (서울 시청 기준)
    val cameraPositionState = remember {
        CameraPositionState(
            position = CameraPosition.fromLatLngZoom(
                LatLng(37.5665, 126.9780),
                15f
            )
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = hasLocationPermission
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = hasLocationPermission,
            compassEnabled = false
        )
    ) {
        if (path.isNotEmpty()) {
            Polyline(
                points = path,
                color = PrimaryPurple,
                width = 12f
            )
        }
    }
}

