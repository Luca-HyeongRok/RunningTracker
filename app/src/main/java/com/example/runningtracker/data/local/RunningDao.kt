package com.example.runningtracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.runningtracker.data.local.entity.RunningRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 러닝 기록 데이터에 접근하기 위한 DAO(Data Access Object) 인터페이스.
 * Room 라이브러리가 이 인터페이스의 구현체를 자동으로 생성합니다.
 */
@Dao
interface RunningDao {

    /**
     * 새로운 러닝 기록을 데이터베이스에 삽입합니다.
     * 만약 동일한 ID의 기록이 이미 존재한다면, 새로운 기록으로 교체합니다. (OnConflictStrategy.REPLACE)
     * @param record 삽입할 러닝 기록 엔티티.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunningRecord(record: RunningRecordEntity)

    /**
     * 데이터베이스에 저장된 모든 러닝 기록을 조회합니다.
     * 결과를 Flow 형태로 반환하여, 데이터 변경 시 자동으로 UI를 업데이트할 수 있도록 합니다.
     * @return 모든 러닝 기록 리스트를 감싸는 Flow.
     */
    @Query("SELECT * FROM running_records ORDER BY startTimeStamp DESC")
    fun getAllRunningRecords(): Flow<List<RunningRecordEntity>>
}