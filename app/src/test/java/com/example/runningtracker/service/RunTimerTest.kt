package com.example.runningtracker.service

import android.os.SystemClock
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

/**
 * RunTimer 단위 테스트
 *
 * 목적:
 * - 실제 러닝 앱의 "시간 측정 로직"이 정상 동작하는지 검증
 * - Android UI / Service 와 완전히 분리된 비즈니스 로직 테스트
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RunTimerTest {

    // 테스트용 코루틴 디스패처 (시간 제어 가능)
    private val dispatcher = StandardTestDispatcher()

    // 테스트 전용 CoroutineScope
    private val scope = TestScope(dispatcher)

    @After
    fun tearDown() {
        // 테스트 종료 시 코루틴 정리 (메모리 누수 방지)
        scope.cancel()
    }

    /**
     * [테스트 목적]
     * start() 호출 시 시간이 정상적으로 증가하는지 검증
     */
    @Test
    fun start_increasesTime() {
        val emitted = mutableListOf<Long>()
        val timer = RunTimer(scope, emitted::add)

        timer.start()
        advanceTime(600L)

        assertTrue(emitted.isNotEmpty())
        assertTrue(emitted.last() > 0L)
    }

    /**
     * [테스트 목적]
     * pause() 호출 시 시간이 더 이상 증가하지 않는지 검증
     */
    @Test
    fun pause_stopsTime() {
        val emitted = mutableListOf<Long>()
        val timer = RunTimer(scope, emitted::add)

        timer.start()
        advanceTime(600L)
        val beforePause = emitted.last()

        timer.pause()
        val sizeAfterPause = emitted.size

        // 시간이 흘러도 tick 발생하지 않아야 함
        advanceTime(600L)

        assertEquals(sizeAfterPause, emitted.size)
        assertEquals(beforePause, emitted.last())
    }

    /**
     * [테스트 목적]
     * stop() 후 다시 start() 하면 시간이 초기화되는지 검증
     */
    @Test
    fun stop_resetsTime() {
        val emitted = mutableListOf<Long>()
        val timer = RunTimer(scope, emitted::add)

        timer.start()
        advanceTime(600L)
        val beforeStop = emitted.last()

        timer.stop()
        timer.start()
        advanceTime(100L)
        val afterRestart = emitted.last()

        assertTrue(afterRestart < beforeStop)
        assertTrue(afterRestart <= 200L)
    }

    /**
     * 테스트에서 "시간 흐름"을 가짜로 진행시키는 유틸 함수
     *
     * - ShadowSystemClock: Android 시스템 시간 이동
     * - advanceTimeBy: 코루틴 delay 처리
     * - runCurrent: 대기 중인 코루틴 즉시 실행
     */
    private fun advanceTime(millis: Long) {
        ShadowSystemClock.advanceBy(Duration.ofMillis(millis))
        scope.advanceTimeBy(millis)
        scope.runCurrent()
        SystemClock.elapsedRealtime()
    }
}
