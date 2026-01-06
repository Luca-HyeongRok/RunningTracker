package com.example.runningtracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.runningtracker.MainActivity
import com.example.runningtracker.R

/**
 * 포그라운드 서비스의 알림(Notification) 생성 및 관리를 돕는 유틸리티 객체.
 */
object NotificationUtil {

    // 알림 채널 ID
    const val NOTIFICATION_CHANNEL_ID = "running_channel"

    // 알림 채널 이름
    private const val NOTIFICATION_CHANNEL_NAME = "Running"

    // 알림 ID
    const val NOTIFICATION_ID = 1

    /**
     * 포그라운드 서비스에 사용할 알림을 생성합니다.
     *
     * @param context 애플리케이션 컨텍스트.
     * @return 생성된 NotificationCompat.Builder 객체.
     */
    fun createNotification(context: Context): NotificationCompat.Builder {
        // 알림 클릭 시 MainActivity를 열기 위한 PendingIntent 생성
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                // 이미 실행 중인 액티비티가 있다면, 새 인스턴스를 만드는 대신 기존 인스턴스를 재사용
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            // Android 12 이상에서는 PendingIntent의 mutability를 명시해야 함
            PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 빌더 초기화
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) // 사용자가 스와이프해도 알림이 사라지지 않음
            .setOngoing(true) // 진행 중인 이벤트임을 나타냄
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 아이콘 설정 (TODO: 적절한 러닝 아이콘으로 변경)
            .setContentTitle("러닝 트래커")
            .setContentText("러닝 측정 중...")
            .setContentIntent(pendingIntent) // 알림 클릭 시 실행될 Intent
    }

    /**
     * 알림 채널을 생성합니다. Android 8.0 (Oreo) 이상에서는 알림을 표시하기 전에 반드시 채널을 생성해야 합니다.
     *
     * @param context 애플리케이션 컨텍스트.
     */
    fun createNotificationChannel(context: Context) {
        // Oreo(API 26) 이상 버전에서만 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // 알림 중요도 설정 (소리는 없지만 헤드업 알림은 표시될 수 있음)
            )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}