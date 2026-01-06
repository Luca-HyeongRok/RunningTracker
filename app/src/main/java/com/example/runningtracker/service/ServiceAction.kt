package com.example.runningtracker.service

/**
 * RunningService를 제어하기 위한 액션(Action)을 정의하는 enum 클래스.
 * Intent에 이 액션을 담아 서비스를 시작/제어합니다.
 */
enum class ServiceAction {
    /** 러닝 추적 시작 또는 재개 */
    START,

    /** 러닝 추적 일시정지 */
    PAUSE,

    /** 러닝 추적 종료 및 저장 */
    STOP
}