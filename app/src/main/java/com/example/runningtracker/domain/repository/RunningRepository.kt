package com.example.runningtracker.domain.repository

import com.example.runningtracker.domain.model.RunningResult
import kotlinx.coroutines.flow.Flow

/**
 * 러닝 데이터에 대한 데이터 액세스를 정의하는 리포지토리 인터페이스.
 *
 * 이 인터페이스는 '도메인 계층'에 속하며, 데이터 소스(DB, 네트워크 등)에 대한 구체적인 구현을 숨깁니다.
 * 서비스나 뷰모델 등 다른 비즈니스 로직은 이 인터페이스에만 의존하게 됩니다.
 * 이를 통해 데이터 계층의 구현이 변경되더라도 비즈니스 로직은 영향을 받지 않는
 * '관심사 분리(Separation of Concerns)' 원칙을 따릅니다.
 */
interface RunningRepository {

    /**
     * 새로운 러닝 결과를 저장합니다.
     * @param result 저장할 러닝 결과 객체.
     */
    suspend fun insertRunningResult(result: RunningResult)

    /**
     * 저장된 모든 러닝 결과를 조회합니다.
     * @return 모든 러닝 결과 리스트를 감싸는 Flow.
     */
    fun getAllRunningResults(): Flow<List<RunningResult>>
}