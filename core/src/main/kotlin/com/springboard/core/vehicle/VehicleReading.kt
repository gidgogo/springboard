package com.springboard.core.vehicle

/**
 * 차량 값 한 번 읽기의 결과.
 *
 * Pleos Vehicle SDK 호출은 비동기 콜백이고, 서비스 연결 전에 부른 호출은 응답이 오지 않을 수 있다.
 * 그래서 "값 없음(null)", "실패", "시간 초과", "서비스 없음"을 구분해서 화면에 보여준다.
 */
sealed interface VehicleReading<out T> {
    /** 차량이 응답함. [value]가 null 이면 차량이 값을 주지 않은 것 (미지원 영역 등). */
    data class Value<T>(val value: T?) : VehicleReading<T>

    /** onFailed 콜백 또는 예외. */
    data class Failed(val reason: String) : VehicleReading<Nothing>

    /** 정해진 시간 안에 응답이 없음. */
    data object TimedOut : VehicleReading<Nothing>

    /** Pleos 차량 서비스가 없는 기기 (일반 폰/태블릿 에뮬레이터). */
    data object NotAvailable : VehicleReading<Nothing>

    /** 아직 읽지 않음. */
    data object NotRead : VehicleReading<Nothing>
}

/** 화면에 보여줄 짧은 문장으로 바꾼다. */
fun <T> VehicleReading<T>.describe(format: (T) -> String = { it.toString() }): String = when (this) {
    is VehicleReading.Value -> value?.let(format) ?: "값 없음"
    is VehicleReading.Failed -> "실패: $reason"
    VehicleReading.TimedOut -> "응답 없음 (시간 초과)"
    VehicleReading.NotAvailable -> "차량 서비스 없음"
    VehicleReading.NotRead -> "-"
}

/** 값이 있으면 꺼낸다. */
fun <T> VehicleReading<T>.valueOrNull(): T? = (this as? VehicleReading.Value<T>)?.value
