package com.springboard.core.demo

import com.springboard.core.driving.DrivingState
import com.springboard.core.driving.Gear
import com.springboard.core.driving.UxRestrictions
import com.springboard.core.vehicle.VehicleSnapshot

/**
 * 데모 시나리오의 한 장면.
 *
 * Pleos Connect Emulator는 외장 GPU가 필요하다. GPU가 없는 팀원도 일반 폰/태블릿 에뮬레이터에서
 * "주차 → 주행 → 정차" 흐름과 화면 전환을 개발·시연할 수 있도록 debug 빌드에서만 쓴다.
 * 실제 차량(에뮬레이터)의 주행 규제가 켜지면 데모보다 실제 값이 우선한다.
 */
data class DemoStep(
    val label: String,
    val durationMs: Long,
    val driving: DrivingState,
    val vehicle: VehicleSnapshot = VehicleSnapshot(),
) {
    init {
        require(durationMs > 0) { "durationMs must be positive: $label" }
    }
}

data class DemoScenario(
    val id: String,
    val title: String,
    val steps: List<DemoStep>,
) {
    init {
        require(steps.isNotEmpty()) { "scenario $id has no steps" }
    }

    val totalDurationMs: Long get() = steps.sumOf { it.durationMs }
}

/** 기본 제공 시나리오. 팀 아이디어에 맞는 장면을 여기에 추가한다. */
object DemoScenarios {

    private fun parked() = DrivingState(
        restrictions = UxRestrictions.NONE,
        gear = Gear.PARK,
        speedKph = 0f,
        source = DrivingState.Source.DEMO,
    )

    private fun driving(speedKph: Float, gear: Gear = Gear.DRIVE) = DrivingState(
        restrictions = UxRestrictions.FULLY_RESTRICTED,
        gear = gear,
        speedKph = speedKph,
        source = DrivingState.Source.DEMO,
    )

    val CITY_COMMUTE = DemoScenario(
        id = "city-commute",
        title = "시내 출근길",
        steps = listOf(
            DemoStep(
                label = "주차 중 (P)",
                durationMs = 4_000,
                driving = parked(),
                vehicle = VehicleSnapshot(odometerKm = 12_345f, tripDistance = 0f, evBatteryPercent = 78f),
            ),
            DemoStep(
                label = "출발 (D · 20km/h)",
                durationMs = 5_000,
                driving = driving(20f),
                vehicle = VehicleSnapshot(odometerKm = 12_345f, tripDistance = 0.3f, evBatteryPercent = 78f),
            ),
            DemoStep(
                label = "간선도로 (D · 60km/h)",
                durationMs = 8_000,
                driving = driving(60f),
                vehicle = VehicleSnapshot(odometerKm = 12_349f, tripDistance = 4.2f, evBatteryPercent = 76f),
            ),
            DemoStep(
                label = "신호 대기 (D · 0km/h)",
                durationMs = 4_000,
                driving = driving(0f),
                vehicle = VehicleSnapshot(odometerKm = 12_350f, tripDistance = 5.1f, evBatteryPercent = 76f),
            ),
            DemoStep(
                label = "도착 후 주차 (P)",
                durationMs = 5_000,
                driving = parked(),
                vehicle = VehicleSnapshot(odometerKm = 12_356f, tripDistance = 11.4f, evBatteryPercent = 74f),
            ),
        ),
    )

    val HIGHWAY_REST_STOP = DemoScenario(
        id = "highway-rest-stop",
        title = "고속도로 → 휴게소",
        steps = listOf(
            DemoStep(
                label = "고속도로 주행 (D · 100km/h)",
                durationMs = 8_000,
                driving = driving(100f),
                vehicle = VehicleSnapshot(odometerKm = 20_100f, tripDistance = 85f, evBatteryPercent = 31f),
            ),
            DemoStep(
                label = "휴게소 진입 (D · 30km/h)",
                durationMs = 4_000,
                driving = driving(30f),
                vehicle = VehicleSnapshot(odometerKm = 20_102f, tripDistance = 87f, evBatteryPercent = 30f),
            ),
            DemoStep(
                label = "주차 (P) · 충전 시작",
                durationMs = 6_000,
                driving = parked(),
                vehicle = VehicleSnapshot(odometerKm = 20_102f, tripDistance = 87f, evBatteryPercent = 45f),
            ),
            DemoStep(
                label = "후진 출차 (R · 5km/h)",
                durationMs = 3_000,
                driving = driving(5f, Gear.REVERSE),
                vehicle = VehicleSnapshot(odometerKm = 20_102f, tripDistance = 87f, evBatteryPercent = 62f),
            ),
        ),
    )

    val ALL: List<DemoScenario> = listOf(CITY_COMMUTE, HIGHWAY_REST_STOP)

    fun byId(id: String): DemoScenario? = ALL.firstOrNull { it.id == id }
}
