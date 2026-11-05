package com.springboard.core.vehicle

/**
 * 앱 기능이 쓰는 차량 값 묶음.
 *
 * 실제 차량에서는 Pleos Vehicle SDK(도메인별 API)로 채우고, 데모 시나리오에서는 미리 정한 값으로 채운다.
 * 아이디어가 정해지면 필요한 값만 남기고 늘리면 된다. 어떤 값이 SDK로 읽히는지는
 * docs/02-pleos/sdk-reference.md 의 "Vehicle SDK 도메인" 표를 본다.
 */
data class VehicleSnapshot(
    /** 누적 주행거리 (계기판, km) — Odometer.getCurrentDistanceDriven */
    val odometerKm: Float? = null,
    /** 현재 트립 주행거리 — Odometer.getTripCurrentDrivingDistance (단위는 SDK 레퍼런스 확인) */
    val tripDistance: Float? = null,
    /** EV 배터리 충전율 % — EvBattery 도메인 */
    val evBatteryPercent: Float? = null,
    /** 연료 잔량 % — Fuel 도메인 */
    val fuelPercent: Float? = null,
    /** 운전석 도어 열림 여부 — Door.getCurrentDoorPosition(ROW_1_LEFT) */
    val driverDoorOpen: Boolean? = null,
    /** 동승석 탑승 여부 — Seat 도메인 occupancy */
    val passengerSeatOccupied: Boolean? = null,
    /** 실내 현재 온도 °C — HVAC 도메인 */
    val cabinTemperatureC: Float? = null,
)
