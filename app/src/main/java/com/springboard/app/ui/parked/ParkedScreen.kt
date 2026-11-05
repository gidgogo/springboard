package com.springboard.app.ui.parked

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.springboard.app.MainActions
import com.springboard.app.MainUiState
import com.springboard.app.platform.speech.PleosSpeechToText
import com.springboard.app.platform.speech.PleosTextToSpeech
import com.springboard.app.ui.components.ButtonRow
import com.springboard.app.ui.components.CarButton
import com.springboard.app.ui.components.HintText
import com.springboard.app.ui.components.InfoRow
import com.springboard.app.ui.components.SectionCard
import com.springboard.app.ui.components.StatusPill
import com.springboard.app.ui.driving.gearLabel
import com.springboard.app.ui.theme.CarDimens
import com.springboard.core.demo.DemoScenarios
import com.springboard.core.driving.DrivingState
import com.springboard.core.vehicle.describe

/**
 * 주차 중 화면 — 지금은 "SDK 연결 점검 홈".
 *
 * Pleos 에뮬레이터에서 앱을 켜고 이 화면 하나로 확인할 수 있는 것:
 * 주행 규제·기어·속도 / Vehicle SDK 값 / TTS·STT / NaviHelper / 데모 시나리오(debug)
 *
 * 아이디어가 정해지면 이 파일을 실제 홈 화면으로 바꾸고, 점검 카드들은 별도 "개발자 화면"으로 옮긴다.
 */
@Composable
fun ParkedScreen(
    state: MainUiState,
    actions: MainActions,
    onRequestSpeedPermission: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(CarDimens.CardSpacing),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(CarDimens.ScreenPadding),
    ) {
        Header(state)

        // Pleos 는 창 1개(portrait) 또는 2개(landscape)로 앱을 띄운다. 넓으면 두 줄, 좁으면 한 줄.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth >= TWO_COLUMN_MIN_WIDTH) {
                Row(horizontalArrangement = Arrangement.spacedBy(CarDimens.CardSpacing)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(CarDimens.CardSpacing),
                        modifier = Modifier.weight(1f),
                    ) {
                        DrivingCard(state, onRequestSpeedPermission)
                        VehicleCard(state, actions)
                        if (state.isDebugBuild) DemoCard(state, actions)
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(CarDimens.CardSpacing),
                        modifier = Modifier.weight(1f),
                    ) {
                        VoiceCard(state, actions)
                        NaviCard(actions)
                        LogCard(state, actions)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(CarDimens.CardSpacing)) {
                    DrivingCard(state, onRequestSpeedPermission)
                    VehicleCard(state, actions)
                    VoiceCard(state, actions)
                    NaviCard(actions)
                    if (state.isDebugBuild) DemoCard(state, actions)
                    LogCard(state, actions)
                }
            }
        }

        HintText(text = state.sdkVersions)
    }
}

@Composable
private fun Header(state: MainUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Springboard", style = MaterialTheme.typography.headlineLarge)
            Text(
                text = "  주차 중 · ${gearLabel(state.driving.gear)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusPill(text = if (state.isAutomotive) "AAOS 기기" else "일반 기기", positive = state.isAutomotive)
            StatusPill(
                text = if (state.hasVehicleService) "Pleos 차량 서비스 있음" else "Pleos 차량 서비스 없음",
                positive = state.hasVehicleService,
            )
            if (state.demo != null) StatusPill(text = "데모 재생 중", positive = true)
        }
    }
}

@Composable
private fun DrivingCard(state: MainUiState, onRequestSpeedPermission: () -> Unit) {
    val driving = state.driving
    SectionCard(title = "주행 상태 (CarUxRestrictions · Car API)") {
        InfoRow("데이터 출처", sourceLabel(driving.source, state.demo != null))
        InfoRow("주행 규제(DO 필요)", if (driving.isDrivingMode) "예" else "아니오")
        InfoRow("활성 제한", driving.restrictions.activeFlagNames().joinToString().ifEmpty { "없음" })
        InfoRow("기어", gearLabel(driving.gear))
        // speedKph 는 다른 모듈(:core)의 프로퍼티라 스마트 캐스트가 안 되므로 지역 변수로 받는다.
        val speedKph = driving.speedKph
        InfoRow(
            "속도",
            when {
                speedKph != null -> "${speedKph.toInt()} km/h"
                !state.speedPermissionGranted -> "권한 필요 (CAR_SPEED)"
                else -> "값 없음"
            },
        )
        state.demo?.let { InfoRow("데모 장면", it.progressLabel) }
        if (state.isAutomotive && !state.speedPermissionGranted) {
            CarButton(text = "속도 권한 요청", onClick = onRequestSpeedPermission, primary = false)
        }
        HintText("에뮬레이터: Virtual Console 에서 기어를 D 로 바꾸거나 adb 로 주행 규제를 켜면 주행 화면으로 바뀝니다.")
    }
}

@Composable
private fun VehicleCard(state: MainUiState, actions: MainActions) {
    SectionCard(title = "차량 데이터 (Pleos Vehicle SDK)") {
        InfoRow("누적 주행거리", state.odometer.describe { "%.1f km".format(it) })
        InfoRow("현재 트립", state.trip.describe { "%.1f".format(it) })
        InfoRow("운전석 도어", state.driverDoor.describe { if (it == 0) "닫힘" else "열림($it)" })
        state.demo?.let { demo ->
            InfoRow("데모 주행거리", demo.vehicle.odometerKm?.let { "%.0f km".format(it) } ?: "-")
            InfoRow("데모 배터리", demo.vehicle.evBatteryPercent?.let { "%.0f %%".format(it) } ?: "-")
        }
        CarButton(text = "차량 값 읽기", onClick = actions::readVehicle)
        if (!state.hasVehicleService) HintText("Pleos Connect Emulator(차량 서비스 포함)에서만 실제 값이 나옵니다.")
    }
}

@Composable
private fun VoiceCard(state: MainUiState, actions: MainActions) {
    SectionCard(title = "음성 (Pleos TTS · STT, on-device)") {
        InfoRow("TTS", ttsLabel(state.ttsStatus))
        InfoRow("STT", sttLabel(state.sttStatus))
        InfoRow("들은 말", state.heard ?: "-")
        InfoRow("해석", state.command ?: "-")
        ButtonRow {
            CarButton(text = "말하기", onClick = actions::speakTest, enabled = state.ttsStatus == PleosTextToSpeech.Status.READY)
            CarButton(
                text = "듣기",
                onClick = actions::listenOnce,
                enabled = state.sttStatus == PleosSpeechToText.Status.READY,
                primary = false,
            )
        }
        if (state.ttsStatus == PleosTextToSpeech.Status.IDLE || state.sttStatus == PleosSpeechToText.Status.IDLE) {
            CarButton(text = "음성 엔진 연결", onClick = actions::connectVoice, primary = false)
        }
        HintText("에뮬레이터 마이크: Extended Controls > Microphone 에서 호스트 오디오 입력을 켭니다.")
    }
}

@Composable
private fun NaviCard(actions: MainActions) {
    SectionCard(title = "내비 (Pleos NaviHelper)") {
        ButtonRow {
            CarButton(text = "연결", onClick = actions::connectNavi, primary = false)
            CarButton(text = "현재 위치 요청", onClick = actions::requestNaviLocation)
        }
        HintText("결과는 콜백으로 와서 아래 로그에 찍힙니다. 기본 내비 앱이 있어야 응답합니다.")
    }
}

@Composable
private fun DemoCard(state: MainUiState, actions: MainActions) {
    SectionCard(title = "데모 시나리오 (debug 전용)") {
        HintText("GPU 가 없어 Pleos 에뮬레이터를 못 쓸 때, 주차→주행→정차 흐름과 화면 전환을 흉내 냅니다.")
        DemoScenarios.ALL.forEach { scenario ->
            CarButton(text = "▶ ${scenario.title}", onClick = { actions.startDemo(scenario.id) }, primary = false)
        }
        if (state.demo != null) CarButton(text = "데모 중지", onClick = actions::stopDemo)
    }
}

@Composable
private fun LogCard(state: MainUiState, actions: MainActions) {
    SectionCard(title = "로그") {
        if (state.log.isEmpty()) {
            HintText("아직 로그가 없습니다.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                state.log.take(VISIBLE_LOG_LINES).forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        CarButton(text = "로그 지우기", onClick = actions::clearLog, primary = false)
    }
}

private fun sourceLabel(source: DrivingState.Source, demo: Boolean): String = when {
    demo -> "데모 시나리오"
    source == DrivingState.Source.CAR -> "차량 (Car API)"
    source == DrivingState.Source.DEMO -> "데모 시나리오"
    else -> "없음 (차량 아님)"
}

private fun ttsLabel(status: PleosTextToSpeech.Status): String = when (status) {
    PleosTextToSpeech.Status.IDLE -> "연결 전"
    PleosTextToSpeech.Status.CONNECTING -> "연결 중"
    PleosTextToSpeech.Status.READY -> "준비됨"
    PleosTextToSpeech.Status.SPEAKING -> "말하는 중"
    PleosTextToSpeech.Status.UNAVAILABLE -> "사용 불가 (Pleos 음성 서비스 없음)"
}

private fun sttLabel(status: PleosSpeechToText.Status): String = when (status) {
    PleosSpeechToText.Status.IDLE -> "연결 전"
    PleosSpeechToText.Status.CONNECTING -> "연결 중"
    PleosSpeechToText.Status.READY -> "준비됨"
    PleosSpeechToText.Status.LISTENING -> "듣는 중"
    PleosSpeechToText.Status.UNAVAILABLE -> "사용 불가 (Pleos 음성 서비스 없음)"
}

private val TWO_COLUMN_MIN_WIDTH = 900.dp
private const val VISIBLE_LOG_LINES = 12
