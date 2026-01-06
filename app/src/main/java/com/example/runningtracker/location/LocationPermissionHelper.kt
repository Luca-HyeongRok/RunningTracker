package com.example.runningtracker.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * 위치 정보 접근 권한 처리를 돕는 헬퍼 클래스.
 */
object LocationPermissionHelper {

    /**
     * 위치 정보 접근에 필요한 모든 권한의 배열.
     * Android 10 (Q) 이상에서는 백그라운드 위치 정보 접근 권한이 추가됩니다.
     */
    private val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    private val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    /**
     * 위치 정보 권한이 부여되었는지 확인합니다.
     * @param context 애플리케이션 컨텍스트.
     * @return 모든 필수 위치 권한이 부여되었으면 true, 그렇지 않으면 false.
     */
    fun hasLocationPermissions(context: Context): Boolean {
        return locationPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    fun hasNotificationPermissions(context: Context): Boolean {
        return notificationPermission.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 사용자에게 위치 정보 권한을 요청합니다.
     * @param launcher 권한 요청을 시작할 ActivityResultLauncher.
     */
    fun requestLocationPermissions(launcher: ActivityResultLauncher<Array<String>>) {
        launcher.launch(locationPermissions)
    }
    fun requestNotificationPermission(launcher: ActivityResultLauncher<Array<String>>) {
        launcher.launch(notificationPermission)
    }
}