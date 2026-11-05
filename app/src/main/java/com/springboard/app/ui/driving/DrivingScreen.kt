package com.springboard.app.ui.driving

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.springboard.app.MainActions
import com.springboard.app.MainUiState
import com.springboard.app.platform.speech.PleosSpeechToText
import com.springboard.app.ui.components.ButtonRow
import com.springboard.app.ui.components.CarButton
import com.springboard.app.ui.components.StatusPill
import com.springboard.app.ui.theme.CarDimens
import com.springboard.core.driving.Gear

/**
 * 주행 중 화면.
 *
 * 규칙 (Pleos 주행 안전 설계 / 디자인 가이드)
 * - 1~2초 안에 파악: 핵심 정보 1~2개만, 큰 글씨
 * - 동영상·애니메이션·자동 스크롤·긴 글·키보드 입력 금지, 목록 최소화
 * - 조작은 한 손, 최소 횟수 — 가능하면 음성(STT/TTS, Gleo)으로
 * - 응답이 0.25초를 넘으면 처리 중임을 보여준다
 *
 * 아이디어가 정해지면 "가운데 큰 정보"와 "버튼 1~2개"를 그 기능에 맞게 바꾼다.
 */
@Composable
fun DrivingScreen(state: MainUiState, actions: MainActions) {
    val policy = state.policy
    val listening = state.sttStatus == PleosSpeechToText.Status.LISTENING

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(CarDimens.ScreenPadding),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "주행 중", style = MaterialTheme.typography.headlineLarge)
            val sourceLabel = if (state.demo != null) "데모" else "차량"
            StatusPill(text = "$sourceLabel · ${gearLabel(state.driving.gear)}", positive = true)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            val speed = state.driving.speedKph
            Text(
                text = speed?.let { "${it.toInt()} km/h" } ?: "—",
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(Modifier.height(16.dp))
            val message = when {
                listening -> "듣고 있어요…"
                state.heard != null -> "\"${state.heard}\""
                else -> "화면 대신 음성으로 안내합니다"
            }
            Text(
                text = policy.clip(message),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = policy.maxLines ?: 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        ButtonRow(modifier = Modifier.fillMaxWidth()) {
            CarButton(
                text = if (listening) "듣는 중" else "말로 하기",
                onClick = actions::listenOnce,
                enabled = state.sttStatus == PleosSpeechToText.Status.READY,
            )
            CarButton(text = "다시 듣기", onClick = actions::speakTest, primary = false)
            if (state.isDebugBuild && state.demo != null) {
                CarButton(text = "데모 종료", onClick = actions::stopDemo, primary = false)
            }
        }
    }
}

internal fun gearLabel(gear: Gear): String = when (gear) {
    Gear.PARK -> "P"
    Gear.REVERSE -> "R"
    Gear.NEUTRAL -> "N"
    Gear.DRIVE -> "D"
    Gear.UNKNOWN -> "기어 ?"
}
