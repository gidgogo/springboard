package com.springboard.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.springboard.app.platform.car.CarDrivingStateMonitor
import com.springboard.app.ui.SpringboardRoot
import com.springboard.app.ui.theme.SpringboardTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // PERF_VEHICLE_SPEED 는 dangerous 권한이라 사용자가 허용해야 한다 (주차 중 화면에서 요청).
    private val speedPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onSpeedPermissionResult(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pleos CCP Back 정책(필수): 최상위 화면에서 Back 이 들어오면 앱을 종료하지 않고 숨긴다.
        // finish() 를 쓰면 심사 반려 대상. 하위 화면을 만들면 그 화면의 Back 처리를 먼저 등록한다.
        onBackPressedDispatcher.addCallback(this) { moveTaskToBack(true) }

        viewModel.handleIntent(intent)

        setContent {
            SpringboardTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                SpringboardRoot(
                    state = state,
                    actions = viewModel,
                    onRequestSpeedPermission = {
                        speedPermissionLauncher.launch(CarDrivingStateMonitor.SPEED_PERMISSION)
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIntent(intent)
    }
}
