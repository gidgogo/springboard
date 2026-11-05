package com.springboard.core.driving

/**
 * 앱이 보는 "지금 차의 주행 상황" 한 장.
 *
 * - [restrictions]: 플랫폼 주행 규제 (화면 전환 판단의 유일한 기준)
 * - [gear], [speedKph]: 기능 로직용 보조 정보 (예: 후진 중에는 음성 안내 보류)
 * - [source]: 실제 차량(에뮬레이터)에서 왔는지, 데모 시나리오인지
 */
data class DrivingState(
    val restrictions: UxRestrictions = UxRestrictions.NONE,
    val gear: Gear = Gear.UNKNOWN,
    val speedKph: Float? = null,
    val source: Source = Source.UNAVAILABLE,
) {
    enum class Source {
        /** CarUxRestrictionsManager / CarPropertyManager 에서 읽은 값 */
        CAR,

        /** 데모 시나리오 (debug 빌드 전용) */
        DEMO,

        /** 차량 서비스가 없는 기기 (일반 폰·태블릿 에뮬레이터) */
        UNAVAILABLE,
    }

    /** 화면을 "주행 모드"(최소 정보, 큰 버튼, 음성 우선)로 바꿔야 하는가. */
    val isDrivingMode: Boolean get() = restrictions.requiresDistractionOptimization

    val isParked: Boolean get() = gear == Gear.PARK

    val isReversing: Boolean get() = gear == Gear.REVERSE

    /** 속도를 모르면 false. 속도는 위험 권한(CAR_SPEED)이 있어야 읽힌다. */
    val isMoving: Boolean get() = (speedKph ?: 0f) > MOVING_THRESHOLD_KPH

    companion object {
        const val MOVING_THRESHOLD_KPH = 3f

        val UNKNOWN = DrivingState()

        /** m/s(AAOS PERF_VEHICLE_SPEED 단위) → km/h */
        fun metersPerSecondToKph(mps: Float): Float = mps * 3.6f
    }
}
