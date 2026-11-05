package com.springboard.app.platform.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.DoorArea
import ai.pleos.playground.vehicle.constant.area.DoorValue
import android.content.Context
import android.util.Log
import com.springboard.app.platform.PlatformInfo
import com.springboard.core.vehicle.VehicleReading
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * Pleos Vehicle SDK 를 코루틴으로 감싼 얇은 클라이언트.
 *
 * SDK 사용 순서 (공식 가이드 "Pleos Connect 앱 개발 시작하기", Vehicle SDK Sample App):
 *   Vehicle(context) → initialize() → getXxx() 도메인 객체 → (checkXxxCapabilities) → get/set/register → release()
 *
 * 주의할 점
 * - 모든 호출은 비동기 콜백(onComplete / onFailed)이다.
 * - 서비스 연결 전에 부른 호출은 응답이 영영 오지 않을 수 있어서 호출마다 [TIMEOUT_MS] 제한을 둔다.
 * - 값의 범위·지원 여부는 차종마다 다르다 → 기능을 확정하면 checkXxxCapabilities 로 먼저 확인한다.
 * - 새 도메인을 쓰면 AndroidManifest.xml 에 해당 권한(pleos.car.permission.CAR_*)을 추가한다.
 *   API·권한 목록: docs/02-pleos/sdk-reference.md
 */
class PleosVehicleClient(
    context: Context,
    private val platform: PlatformInfo,
) {
    private val appContext = context.applicationContext
    private var vehicle: Vehicle? = null

    val isAvailable: Boolean get() = platform.hasPleosVehicleService

    /** 누적 주행거리(계기판, km). 권한: CAR_DRIVE */
    suspend fun readOdometerKm(): VehicleReading<Float> = read { session, ok, fail ->
        session.getOdometer().getCurrentDistanceDriven({ ok(it) }, { fail(it) })
    }

    /** 현재 트립 주행거리. 단위는 SDK API 레퍼런스에서 확인. 권한: CAR_DRIVE */
    suspend fun readTripDistance(): VehicleReading<Float> = read { session, ok, fail ->
        session.getOdometer().getTripCurrentDrivingDistance({ ok(it) }, { fail(it) })
    }

    /** 운전석(1열 왼쪽) 도어 위치. 0 = 닫힘. 권한: CAR_DOORS */
    suspend fun readDriverDoorPosition(): VehicleReading<Int> = read { session, ok, fail ->
        session.getDoor().getCurrentDoorPosition(
            area = DoorArea(DoorValue.ROW_1_LEFT),
            onComplete = { position -> ok(position) },
            onFailed = { error -> fail(error) },
        )
    }

    @Synchronized
    fun release() {
        runCatching { vehicle?.release() }
        vehicle = null
    }

    @Synchronized
    private fun session(): Vehicle? {
        vehicle?.let { return it }
        if (!isAvailable) return null
        return try {
            Vehicle(appContext).also {
                it.initialize()
                vehicle = it
            }
        } catch (e: RuntimeException) {
            Log.w(TAG, "Vehicle SDK 초기화 실패", e)
            null
        } catch (e: LinkageError) {
            Log.w(TAG, "Vehicle SDK 를 불러오지 못함", e)
            null
        }
    }

    private suspend fun <T> read(
        call: (session: Vehicle, ok: (T?) -> Unit, fail: (Exception) -> Unit) -> Unit,
    ): VehicleReading<T> {
        val session = session() ?: return VehicleReading.NotAvailable
        val result = withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine<VehicleReading<T>> { continuation ->
                val finished = AtomicBoolean(false)
                val finish: (VehicleReading<T>) -> Unit = { reading ->
                    if (finished.compareAndSet(false, true) && continuation.isActive) {
                        continuation.resume(reading)
                    }
                }
                try {
                    call(
                        session,
                        { value -> finish(VehicleReading.Value(value)) },
                        { error -> finish(VehicleReading.Failed(error.message ?: error.javaClass.simpleName)) },
                    )
                } catch (e: RuntimeException) {
                    finish(VehicleReading.Failed(e.message ?: e.javaClass.simpleName))
                }
            }
        }
        return result ?: VehicleReading.TimedOut
    }

    private companion object {
        const val TAG = "PleosVehicle"
        const val TIMEOUT_MS = 3_000L
    }
}
