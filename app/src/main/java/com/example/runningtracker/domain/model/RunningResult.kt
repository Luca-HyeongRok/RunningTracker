package com.example.runningtracker.domain.model

import android.graphics.Bitmap
import java.util.Date

/**
 * 한 번의 러닝 운동 결과를 나타내는 도메인 모델 클래스.
 * UI 레이어에서 사용되며, 데이터베이스 엔티티와는 독립적입니다.
 *
 * @param id 고유 ID
 * @param image 경로 스냅샷 이미지
 * @param startTimeStamp 러닝 시작 시각
 * @param totalTimeInMillis 총 러닝 시간 (밀리초)
 * @param avgSpeedInKMH 평균 속도 (km/h)
 * @param distanceInMeters 총 이동 거리 (미터)
 */
data class RunningResult(
    val id: Int? = null,
    val image: Bitmap? = null,
    val startTimeStamp: Date,
    val totalTimeInMillis: Long,
    val avgSpeedInKMH: Float,
    val distanceInMeters: Int
)