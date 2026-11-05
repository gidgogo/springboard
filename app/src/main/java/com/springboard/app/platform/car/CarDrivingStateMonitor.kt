package com.springboard.app.platform.car

import android.car.Car
import android.car.VehicleGear
import android.car.VehiclePropertyIds
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.springboard.app.platform.PlatformInfo
import com.springboard.core.driving.DrivingState
import com.springboard.core.driving.Gear
import com.springboard.core.driving.UxRestrictions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * 차량의 주행 상황을 [state] 로 내보낸다.
 *
 * 1. 주행 규제: AAOS `CarUxRestrictionsManager` (Pleos "주행 규제 개발 가이드"가 요구하는 공식 경로)
 * 2. 기어: `VehiclePropertyIds.GEAR_SELECTION` (권한 CAR_POWERTRAIN, normal)
 * 3. 속도: `VehiclePropertyIds.PERF_VEHICLE_SPEED` (권한 CAR_SPEED, dangerous → 허용된 경우에만)
 *    클러스터와 같은 속도를 쓰려면 이 값을 쓴다 (Pleos FAQ "클러스터 속도 조회 방법").
 *
 * 화면을 주행 모드로 바꿀지는 오직 `isRequiresDistractionOptimization` 으로 판단한다.
 * 기어·속도로 규제를 추정하지 않는다 (Google AAOS 가이드).
 *
 * 차량이 아닌 기기(일반 폰)에서는 아무것도 하지 않고 Source.UNAVAILABLE 상태로 남는다.
 */
class CarDrivingStateMonitor(
    context: Context,
    private val platform: PlatformInfo,
) {
    private val appContext = context.applicationContext
    private val _state = MutableStateFlow(DrivingState.UNKNOWN)
    val state: StateFlow<DrivingState> = _state.asStateFlow()

    private var car: Car? = null
    private var uxManager: CarUxRestrictionsManager? = null

    @Volatile
    private var propertyManager: CarPropertyManager? = null

    @Volatile
    private var propertyCallback: CarPropertyManager.CarPropertyEventCallback? = null

    @Volatile
    private var speedSubscribed = false

    // registerCallback 은 차량 서비스에서 설정을 받아오느라 오래 걸릴 수 있다.
    // 에뮬레이터에서 메인 스레드를 막아 ANR 이 난 사례가 있어 별도 스레드에서 부른다.
    private val worker: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "car-property").apply { isDaemon = true }
    }

    private var started = false

    fun start() {
        if (started) return
        started = true
        if (!platform.isAutomotive) {
            Log.i(TAG, "AAOS 기기가 아님: 주행 상태를 읽지 않는다 (데모 모드 사용)")
            return
        }
        try {
            // 서비스 준비/재시작 때마다 메인 스레드에서 호출된다.
            car = Car.createCar(appContext, null, Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT) { connectedCar, ready ->
                if (ready) onCarReady(connectedCar) else onCarLost()
            }
        } catch (e: RuntimeException) {
            Log.w(TAG, "Car 서비스에 연결하지 못함", e)
        } catch (e: LinkageError) {
            Log.w(TAG, "android.car 라이브러리가 없음", e)
        }
    }

    /** CAR_SPEED 권한을 새로 받았을 때 속도 구독을 다시 시도한다. */
    fun onPermissionsChanged() {
        val manager = propertyManager ?: return
        val callback = propertyCallback ?: return
        worker.execute { subscribeSpeed(manager, callback) }
    }

    private fun onCarReady(connectedCar: Car) {
        _state.update { it.copy(source = DrivingState.Source.CAR) }

        val manager = try {
            connectedCar.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
        } catch (e: RuntimeException) {
            Log.w(TAG, "CarUxRestrictionsManager 를 얻지 못함", e)
            null
        }
        uxManager = manager
        if (manager != null) {
            onUxRestrictions(manager.currentCarUxRestrictions)
            manager.registerListener { restrictions -> onUxRestrictions(restrictions) }
        }

        worker.execute { subscribeProperties(connectedCar) }
    }

    private fun onCarLost() {
        Log.w(TAG, "Car 서비스 연결이 끊김 (재연결을 기다림)")
        uxManager = null
        propertyManager = null
        propertyCallback = null
        speedSubscribed = false
    }

    private fun onUxRestrictions(restrictions: CarUxRestrictions?) {
        if (restrictions == null) return
        val mapped = UxRestrictions(
            requiresDistractionOptimization = restrictions.isRequiresDistractionOptimization,
            activeFlags = restrictions.activeRestrictions,
            maxStringLength = restrictions.maxRestrictedStringLength,
            maxContentItems = restrictions.maxCumulativeContentItems,
            maxContentDepth = restrictions.maxContentDepth,
        )
        Log.i(TAG, "주행 규제 DO=${mapped.requiresDistractionOptimization} flags=${mapped.activeFlagNames()}")
        _state.update { it.copy(restrictions = mapped, source = DrivingState.Source.CAR) }
    }

    private fun subscribeProperties(connectedCar: Car) {
        val manager = try {
            connectedCar.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
        } catch (e: RuntimeException) {
            Log.w(TAG, "CarPropertyManager 를 얻지 못함", e)
            null
        } ?: return

        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) = onPropertyChanged(value)

            override fun onErrorEvent(propertyId: Int, areaId: Int) {
                Log.w(TAG, "속성 오류: ${VehiclePropertyIds.toString(propertyId)} area=$areaId")
            }
        }
        propertyManager = manager
        propertyCallback = callback

        register(manager, callback, VehiclePropertyIds.GEAR_SELECTION, GEAR_PERMISSION, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        subscribeSpeed(manager, callback)
    }

    private fun subscribeSpeed(manager: CarPropertyManager, callback: CarPropertyManager.CarPropertyEventCallback) {
        if (speedSubscribed) return
        speedSubscribed =
            register(manager, callback, VehiclePropertyIds.PERF_VEHICLE_SPEED, SPEED_PERMISSION, CarPropertyManager.SENSOR_RATE_NORMAL)
    }

    // registerCallback 은 API 35 에서 deprecated 지만, Pleos Connect(Android 14, API 34)에는
    // 대체 API(subscribePropertyEvents)가 없으므로 이것을 쓴다.
    @Suppress("DEPRECATION")
    private fun register(
        manager: CarPropertyManager,
        callback: CarPropertyManager.CarPropertyEventCallback,
        propertyId: Int,
        permission: String,
        rateHz: Float,
    ): Boolean {
        val name = VehiclePropertyIds.toString(propertyId)
        if (appContext.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "$name: 권한 없음 ($permission)")
            return false
        }
        return try {
            manager.registerCallback(callback, propertyId, rateHz).also { ok ->
                Log.i(TAG, "$name 구독 ${if (ok) "성공" else "실패"}")
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "$name: 권한 거부", e)
            false
        } catch (e: RuntimeException) {
            Log.w(TAG, "$name: 구독 실패", e)
            false
        }
    }

    private fun onPropertyChanged(value: CarPropertyValue<*>) {
        when (value.propertyId) {
            VehiclePropertyIds.GEAR_SELECTION -> {
                val gear = (value.value as? Int)?.let(::mapGear) ?: Gear.UNKNOWN
                _state.update { it.copy(gear = gear) }
            }

            VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                val metersPerSecond = value.value as? Float ?: return
                _state.update { it.copy(speedKph = DrivingState.metersPerSecondToKph(abs(metersPerSecond))) }
            }
        }
    }

    private fun mapGear(raw: Int): Gear = when (raw) {
        VehicleGear.GEAR_PARK -> Gear.PARK
        VehicleGear.GEAR_REVERSE -> Gear.REVERSE
        VehicleGear.GEAR_NEUTRAL -> Gear.NEUTRAL
        VehicleGear.GEAR_UNKNOWN -> Gear.UNKNOWN
        // D 와 수동 1~9단은 모두 "전진"으로 본다.
        else -> if (raw >= VehicleGear.GEAR_DRIVE) Gear.DRIVE else Gear.UNKNOWN
    }

    @Suppress("DEPRECATION")
    fun stop() {
        worker.execute {
            val manager = propertyManager
            val callback = propertyCallback
            if (manager != null && callback != null) runCatching { manager.unregisterCallback(callback) }
            propertyManager = null
            propertyCallback = null
            speedSubscribed = false
        }
        runCatching { uxManager?.unregisterListener() }
        runCatching { car?.disconnect() }
        uxManager = null
        car = null
        started = false
    }

    companion object {
        private const val TAG = "DrivingState"
        const val GEAR_PERMISSION = "android.car.permission.CAR_POWERTRAIN"
        const val SPEED_PERMISSION = "android.car.permission.CAR_SPEED"
    }
}
