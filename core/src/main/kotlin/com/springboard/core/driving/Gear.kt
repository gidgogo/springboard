package com.springboard.core.driving

/**
 * 변속 기어.
 *
 * 앱 모듈이 AAOS `VehiclePropertyIds.GEAR_SELECTION` 값(`android.car.VehicleGear`)을 이 enum으로 바꿔 넘긴다.
 * Pleos Vehicle SDK에는 공개 기어 API가 없으므로 AAOS 표준 API를 쓴다
 * (Pleos "SDK API 정책": 표준 데이터는 AAOS Car API 사용).
 */
enum class Gear {
    PARK,
    REVERSE,
    NEUTRAL,
    DRIVE,
    UNKNOWN,
}
