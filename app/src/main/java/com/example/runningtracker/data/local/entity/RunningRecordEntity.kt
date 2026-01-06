package com.example.runningtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Room 데이터베이스에 저장될 러닝 기록 엔티티.
 * 데이터베이스의 'running_records' 테이블과 매핑됩니다.
 *
 * @param id 레코드의 고유 식별자 (자동 생성).
 * @param imagePath 경로 스냅샷 이미지의 파일 경로. Bitmap 객체는 DB에 직접 저장하기 부적합하므로 경로로 관리합니다.
 * @param startTimeStamp 러닝 시작 시각의 타임스탬프.
 * @param totalTimeInMillis 총 운동 시간 (밀리초 단위).
 * @param avgSpeedInKMH 평균 속도 (km/h).
 * @param distanceInMeters 총 이동 거리 (미터 단위).
 */
@Entity(tableName = "running_records")
data class RunningRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val imagePath: String? = null,
    val startTimeStamp: Date,
    val totalTimeInMillis: Long,
    val avgSpeedInKMH: Float,
    val distanceInMeters: Int
)