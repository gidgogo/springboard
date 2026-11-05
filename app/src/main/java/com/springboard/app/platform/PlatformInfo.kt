package com.springboard.app.platform

import android.content.Context
import android.content.pm.PackageManager

/**
 * 지금 앱이 어떤 기기에서 돌고 있는지.
 *
 * - [isAutomotive]: AAOS 기기(Pleos Connect Emulator 포함)인가
 * - [hasPleosVehicleService]: Pleos Vehicle Service 가 설치돼 있는가 (일반 AAOS 에뮬레이터에는 없다)
 *
 * 참고: 실차의 플랫폼 구분은 android.os.Build.DEVICE (CONNECT_S / CONNECT_L / CONNECT_W) 로 하지만
 * 에뮬레이터에서는 값이 유효하지 않다 (Pleos FAQ "실행 플랫폼 확인").
 */
class PlatformInfo(private val context: Context) {

    val isAutomotive: Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

    val hasPleosVehicleService: Boolean
        get() = isPackageInstalled(VEHICLE_SERVICE_PACKAGE)

    @Suppress("DEPRECATION")
    fun isPackageInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    companion object {
        /** Pleos 공식 Vehicle 샘플이 버전 확인에 쓰는 서비스 패키지 */
        const val VEHICLE_SERVICE_PACKAGE = "ai.pleos.playground.service.vehicle"
    }
}
