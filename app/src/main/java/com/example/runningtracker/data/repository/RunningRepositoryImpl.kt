package com.example.runningtracker.data.repository

import android.graphics.Bitmap
import com.example.runningtracker.data.local.RunningDao
import com.example.runningtracker.data.local.entity.RunningRecordEntity
import com.example.runningtracker.domain.model.RunningResult
import com.example.runningtracker.domain.repository.RunningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * RunningRepository 인터페이스의 실제 구현체.
 *
 * 이 클래스는 '데이터 계층'에 속하며, 데이터 소스(여기서는 Room DB)와 직접 상호작용합니다.
 * DAO를 통해 DB 작업을 수행하고, DB 엔티티(RunningRecordEntity)를
 * 도메인 모델(RunningResult)로 변환하는 역할을 담당합니다.
 *
 * 이처럼 인터페이스와 구현체를 분리하면, 나중에 데이터 소스를 Room이 아닌
 * 다른 DB나 네트워크 API로 변경하더라도, 이 클래스만 수정하면 되므로 유지보수성이 향상됩니다.
 *
 * @param dao Room 데이터베이스에 접근하기 위한 DAO.
 */
class RunningRepositoryImpl(
    private val dao: RunningDao
) : RunningRepository {

    override suspend fun insertRunningResult(result: RunningResult) {
        val record = result.toEntity()
        dao.insertRunningRecord(record)
    }

    override fun getAllRunningResults(): Flow<List<RunningResult>> {
        return dao.getAllRunningRecords().map { records ->
            records.map { it.toDomainModel() }
        }
    }

    // 도메인 모델(RunningResult)을 데이터베이스 엔티티(RunningRecordEntity)로 변환
    private fun RunningResult.toEntity(): RunningRecordEntity {
        // 실제 앱에서는 이미지를 파일로 저장하고 그 경로를 저장해야 합니다.
        // 여기서는 이미지 처리를 단순화하여 null로 처리합니다.
        return RunningRecordEntity(
            id = this.id,
            imagePath = null, // TODO: Bitmap을 파일로 저장하고 경로를 반환하는 로직 구현 필요
            startTimeStamp = this.startTimeStamp,
            totalTimeInMillis = this.totalTimeInMillis,
            avgSpeedInKMH = this.avgSpeedInKMH,
            distanceInMeters = this.distanceInMeters
        )
    }

    // 데이터베이스 엔티티(RunningRecordEntity)를 도메인 모델(RunningResult)로 변환
    private fun RunningRecordEntity.toDomainModel(): RunningResult {
        // 실제 앱에서는 저장된 경로의 이미지를 불러와야 합니다.
        // 여기서는 이미지 처리를 단순화하여 null로 처리합니다.
        return RunningResult(
            id = this.id,
            image = null, // TODO: imagePath를 이용해 실제 Bitmap을 로드하는 로직 구현 필요
            startTimeStamp = this.startTimeStamp,
            totalTimeInMillis = this.totalTimeInMillis,
            avgSpeedInKMH = this.avgSpeedInKMH,
            distanceInMeters = this.distanceInMeters
        )
    }
}