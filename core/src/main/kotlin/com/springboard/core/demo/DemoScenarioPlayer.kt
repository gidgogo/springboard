package com.springboard.core.demo

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** 재생 중인 장면 하나. */
data class DemoFrame(
    val scenario: DemoScenario,
    val stepIndex: Int,
    val step: DemoStep,
) {
    val progressLabel: String get() = "${stepIndex + 1}/${scenario.steps.size} · ${step.label}"
}

/**
 * 시나리오를 장면 순서대로 내보내는 Flow.
 * 수집(collect)을 취소하면 재생이 멈춘다. [loop]가 true 면 끝나고 처음부터 반복한다.
 */
object DemoScenarioPlayer {
    fun play(scenario: DemoScenario, loop: Boolean = false): Flow<DemoFrame> = flow {
        do {
            scenario.steps.forEachIndexed { index, step ->
                emit(DemoFrame(scenario, index, step))
                delay(step.durationMs)
            }
        } while (loop)
    }
}
