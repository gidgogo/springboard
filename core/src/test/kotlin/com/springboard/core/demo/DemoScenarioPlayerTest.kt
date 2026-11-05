package com.springboard.core.demo

import com.springboard.core.driving.Gear
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DemoScenarioPlayerTest {

    /** 장면을 순서대로 내보내고 각 장면 길이만큼 기다린다 */
    @Test
    fun emitsStepsInOrderAndWaitsEachDuration() = runTest {
        val scenario = DemoScenarios.CITY_COMMUTE

        val frames = DemoScenarioPlayer.play(scenario).toList()

        assertEquals(scenario.steps.size, frames.size)
        assertEquals(scenario.steps.map { it.label }, frames.map { it.step.label })
        assertEquals(scenario.totalDurationMs, currentTime)
    }

    /** 시내 시나리오는 주차로 시작해 주차로 끝나고 중간에는 주행 모드다 */
    @Test
    fun cityScenarioStartsAndEndsParked() {
        val steps = DemoScenarios.CITY_COMMUTE.steps

        assertEquals(Gear.PARK, steps.first().driving.gear)
        assertFalse(steps.first().driving.isDrivingMode)
        assertTrue(steps[1].driving.isDrivingMode)
        assertEquals(Gear.PARK, steps.last().driving.gear)
    }

    /** 반복 재생은 끝난 뒤 처음 장면으로 돌아간다 */
    @Test
    fun loopRestartsFromFirstStep() = runTest {
        val scenario = DemoScenarios.HIGHWAY_REST_STOP
        val count = scenario.steps.size + 1

        val frames = DemoScenarioPlayer.play(scenario, loop = true).take(count).toList()

        assertEquals(0, frames.last().stepIndex)
        assertEquals("1/${scenario.steps.size} · ${scenario.steps.first().label}", frames.last().progressLabel)
    }

    /** 수집을 취소하면 재생이 멈춘다 */
    @Test
    fun cancellingCollectionStopsPlayback() = runTest {
        val received = mutableListOf<DemoFrame>()
        val job = launch { DemoScenarioPlayer.play(DemoScenarios.CITY_COMMUTE).collect { received += it } }

        runCurrent()
        advanceTimeBy(DemoScenarios.CITY_COMMUTE.steps.first().durationMs + 1)
        job.cancel()
        advanceTimeBy(60_000)

        assertEquals(2, received.size)
    }

    /** 모든 기본 시나리오는 id로 찾을 수 있다 */
    @Test
    fun everyScenarioIsFoundById() {
        DemoScenarios.ALL.forEach { scenario ->
            assertEquals(scenario, DemoScenarios.byId(scenario.id))
            assertTrue(scenario.totalDurationMs > 0)
        }
    }
}
