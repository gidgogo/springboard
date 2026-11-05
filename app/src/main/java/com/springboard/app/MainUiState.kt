package com.springboard.app

import com.springboard.app.platform.speech.PleosSpeechToText
import com.springboard.app.platform.speech.PleosTextToSpeech
import com.springboard.core.driving.DrivingState
import com.springboard.core.driving.DrivingUiPolicy
import com.springboard.core.vehicle.VehicleReading
import com.springboard.core.vehicle.VehicleSnapshot

/** 화면이 그리는 데 필요한 모든 값 (한 방향 데이터 흐름: ViewModel → 화면). */
data class MainUiState(
    val isAutomotive: Boolean = false,
    val hasVehicleService: Boolean = false,
    val isDebugBuild: Boolean = false,
    val sdkVersions: String = "",

    val driving: DrivingState = DrivingState.UNKNOWN,
    val policy: DrivingUiPolicy = DrivingUiPolicy.PARKED,
    val demo: DemoUi? = null,
    val speedPermissionGranted: Boolean = false,

    val odometer: VehicleReading<Float> = VehicleReading.NotRead,
    val trip: VehicleReading<Float> = VehicleReading.NotRead,
    val driverDoor: VehicleReading<Int> = VehicleReading.NotRead,

    val ttsStatus: PleosTextToSpeech.Status = PleosTextToSpeech.Status.IDLE,
    val sttStatus: PleosSpeechToText.Status = PleosSpeechToText.Status.IDLE,
    val heard: String? = null,
    val command: String? = null,

    val log: List<String> = emptyList(),
)

/** 데모 시나리오 재생 정보 (debug 빌드 전용). */
data class DemoUi(
    val scenarioTitle: String,
    val progressLabel: String,
    val vehicle: VehicleSnapshot,
)

/** 화면 → ViewModel 이벤트. 프리뷰에서는 [NoOpActions] 를 쓴다. */
interface MainActions {
    fun readVehicle()
    fun connectVoice()
    fun speakTest()
    fun repeatLastMessage()
    fun listenOnce()
    fun connectNavi()
    fun requestNaviLocation()
    fun startDemo(scenarioId: String)
    fun stopDemo()
    fun clearLog()
}

object NoOpActions : MainActions {
    override fun readVehicle() = Unit
    override fun connectVoice() = Unit
    override fun speakTest() = Unit
    override fun repeatLastMessage() = Unit
    override fun listenOnce() = Unit
    override fun connectNavi() = Unit
    override fun requestNaviLocation() = Unit
    override fun startDemo(scenarioId: String) = Unit
    override fun stopDemo() = Unit
    override fun clearLog() = Unit
}
