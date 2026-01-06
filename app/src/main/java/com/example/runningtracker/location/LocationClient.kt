package com.example.runningtracker.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * FusedLocationProviderClient를 래핑하여 위치 업데이트를 Flow 형태로 제공하는 클라이언트 클래스.
 *
 * @param context 애플리케이션 컨텍스트.
 * @param client FusedLocationProviderClient 인스턴스.
 */
class LocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) {

    /**
     * 위치 업데이트를 Flow<Location> 형태로 제공합니다.
     * @param interval 위치 업데이트 간격 (밀리초).
     * @return 위치 정보를 방출하는 Flow.
     * @throws SecurityException 위치 권한이 없을 경우 발생.
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(interval: Long): Flow<Location> = callbackFlow {
        // 위치 권한이 있는지 확인. 없다면 예외를 발생시키고 Flow를 닫습니다.
        if (!LocationPermissionHelper.hasLocationPermissions(context)) {
            throw SecurityException("Location permission not granted.")
        }

        // 위치 요청 설정
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(interval)
            .setMaxUpdateDelayMillis(interval * 2)
            .build()

        // 위치 업데이트를 수신할 콜백
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                // 마지막 위치 정보를 Flow로 보냅니다.
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        // 위치 업데이트 시작
        client.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper() // 메인 스레드에서 콜백을 받도록 설정
        )

        // Flow가 닫힐 때 (소비자가 구독을 취소할 때) 위치 업데이트를 중지합니다.
        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }
}