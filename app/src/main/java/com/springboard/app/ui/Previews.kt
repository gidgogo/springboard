package com.springboard.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.springboard.app.MainUiState
import com.springboard.app.NoOpActions
import com.springboard.app.platform.speech.PleosSpeechToText
import com.springboard.app.platform.speech.PleosTextToSpeech
import com.springboard.app.ui.theme.SpringboardTheme
import com.springboard.core.driving.DrivingState
import com.springboard.core.driving.DrivingUiPolicy
import com.springboard.core.driving.Gear
import com.springboard.core.driving.UxRestrictions
import com.springboard.core.vehicle.VehicleReading

// Android Studio 의 Preview 로 에뮬레이터 없이 화면을 확인한다.
// Pleos 화면 크기 참고: landscape(창 2개) / portrait(창 1개). 실제 dp 는 폼팩터 테스트 도구로 확인한다.

private val parkedSample = MainUiState(
    isAutomotive = true,
    hasVehicleService = true,
    isDebugBuild = true,
    sdkVersions = "Vehicle 2.3.2 · NaviHelper 3.0.5 · TTS 3.3.1 · STT 3.3.1",
    driving = DrivingState(gear = Gear.PARK, speedKph = 0f, source = DrivingState.Source.CAR),
    odometer = VehicleReading.Value(12_345.6f),
    trip = VehicleReading.TimedOut,
    driverDoor = VehicleReading.Value(0),
    ttsStatus = PleosTextToSpeech.Status.READY,
    sttStatus = PleosSpeechToText.Status.READY,
    heard = "다시 말해 줘",
    command = "Command(name=REPEAT, keyword=다시)",
    log = listOf("10:00:01  시작: AAOS=true, Pleos 차량 서비스=true", "10:00:03  차량 데이터 읽기 완료"),
)

private val drivingSample = parkedSample.copy(
    driving = DrivingState(
        restrictions = UxRestrictions.FULLY_RESTRICTED,
        gear = Gear.DRIVE,
        speedKph = 62f,
        source = DrivingState.Source.CAR,
    ),
    policy = DrivingUiPolicy.from(UxRestrictions.FULLY_RESTRICTED),
    heard = null,
)

@Preview(name = "주차 - landscape", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun ParkedLandscapePreview() {
    SpringboardTheme { SpringboardRoot(state = parkedSample, actions = NoOpActions, onRequestSpeedPermission = {}) }
}

@Preview(name = "주차 - portrait", widthDp = 640, heightDp = 900, showBackground = true)
@Composable
private fun ParkedPortraitPreview() {
    SpringboardTheme { SpringboardRoot(state = parkedSample, actions = NoOpActions, onRequestSpeedPermission = {}) }
}

@Preview(name = "주행", widthDp = 1280, heightDp = 720, showBackground = true)
@Composable
private fun DrivingPreview() {
    SpringboardTheme { SpringboardRoot(state = drivingSample, actions = NoOpActions, onRequestSpeedPermission = {}) }
}
