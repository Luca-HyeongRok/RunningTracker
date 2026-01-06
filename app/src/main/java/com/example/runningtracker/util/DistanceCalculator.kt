package com.example.runningtracker.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * 위치 좌표 간의 거리를 계산하는 유틸리티 객체.
 */
object DistanceCalculator {

    /**
     * 두 LatLng 좌표 사이의 거리를 미터(meter) 단위로 계산합니다.
     *
     * @param start 시작점 좌표.
     * @param end 끝점 좌표.
     * @return 두 지점 사이의 거리 (미터).
     */
    fun calculateDistance(start: LatLng, end: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude,
            results
        )
        return results[0]
    }

    /**
     * Polyline (경로)의 총 거리를 계산합니다.
     * 경로를 구성하는 모든 좌표들 사이의 거리를 누적하여 합산합니다.
     *
     * @param polyline LatLng 좌표의 리스트로 표현되는 경로.
     * @return 경로의 총 거리 (미터).
     */
    fun calculatePolylineDistance(polyline: List<LatLng>): Float {
        var distance = 0f
        for (i in 0..polyline.size - 2) {
            val start = polyline[i]
            val end = polyline[i + 1]
            distance += calculateDistance(start, end)
        }
        return distance
    }
}