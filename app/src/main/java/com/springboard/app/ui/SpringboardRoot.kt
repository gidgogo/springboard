package com.springboard.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.springboard.app.MainActions
import com.springboard.app.MainUiState
import com.springboard.app.ui.driving.DrivingScreen
import com.springboard.app.ui.parked.ParkedScreen

/**
 * 최상위 화면 분기.
 *
 * 주행 규제(DO 필요)가 걸리면 화면 전체를 [DrivingScreen] 으로 바꾼다.
 * - 주행 중: 1~2초 안에 읽히는 정보 1~2개 + 큰 버튼 + 음성 우선 (키보드·긴 글·목록·영상·애니메이션 금지)
 * - 주차 중: [ParkedScreen] — 지금은 SDK 연결 점검 화면, 아이디어가 정해지면 실제 홈 화면으로 바꾼다.
 *
 * 화면 크기는 Pleos 창 정책상 portrait(창 1개)·landscape(창 2개) 모두 가능하다. 두 화면 모두
 * 가로·세로 어느 쪽이든 깨지지 않게 만든다 (폼팩터 테스트: docs/02-pleos/emulator-and-virtual-console.md).
 */
@Composable
fun SpringboardRoot(
    state: MainUiState,
    actions: MainActions,
    onRequestSpeedPermission: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        if (state.policy.drivingMode) {
            DrivingScreen(state = state, actions = actions)
        } else {
            ParkedScreen(state = state, actions = actions, onRequestSpeedPermission = onRequestSpeedPermission)
        }
    }
}
