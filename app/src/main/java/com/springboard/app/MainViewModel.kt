package com.springboard.app

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.springboard.app.platform.car.CarDrivingStateMonitor
import com.springboard.app.platform.gleo.GleoBridge
import com.springboard.app.platform.speech.PleosSpeechToText
import com.springboard.app.platform.speech.PleosTextToSpeech
import com.springboard.core.demo.DemoFrame
import com.springboard.core.demo.DemoScenarioPlayer
import com.springboard.core.demo.DemoScenarios
import com.springboard.core.driving.DrivingState
import com.springboard.core.driving.DrivingUiPolicy
import com.springboard.core.gleo.GleoCallback
import com.springboard.core.voice.VoiceCommandMatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 화면 상태를 만드는 곳.
 *
 * 주행 상태 결정 규칙
 * 1. 실제 차량(에뮬레이터)이 주행 규제(DO 필요)를 걸면 → 무조건 그 상태 (안전 우선)
 * 2. 아니면 데모 시나리오가 재생 중이면 → 데모 상태 (debug 빌드 전용)
 * 3. 아니면 → 실제 차량 상태 (차량이 아니면 UNAVAILABLE)
 */
class MainViewModel(application: Application) : AndroidViewModel(application), MainActions {

    private val container = (application as SpringboardApplication).container

    private val matcher = VoiceCommandMatcher(
        linkedMapOf(
            "REPEAT" to listOf("다시", "한 번 더"),
            "STATUS" to listOf("상태", "주행거리"),
        ),
    )

    private val demoFrame = MutableStateFlow<DemoFrame?>(null)
    private var demoJob: Job? = null
    private var lastSpoken: String = GREETING

    private val _uiState = MutableStateFlow(
        MainUiState(
            isAutomotive = container.platform.isAutomotive,
            hasVehicleService = container.platform.hasPleosVehicleService,
            isDebugBuild = BuildConfig.DEBUG,
            sdkVersions = "Vehicle ${BuildConfig.PLEOS_VEHICLE_SDK} · NaviHelper ${BuildConfig.PLEOS_NAVI_HELPER_SDK} · " +
                "TTS ${BuildConfig.PLEOS_TTS_SDK} · STT ${BuildConfig.PLEOS_STT_SDK}",
            speedPermissionGranted = hasPermission(CarDrivingStateMonitor.SPEED_PERMISSION),
        ),
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        container.drivingStateMonitor.start()

        viewModelScope.launch {
            combine(container.drivingStateMonitor.state, demoFrame) { car, demo -> car to demo }
                .collect { pair -> applyDriving(pair.first, pair.second) }
        }
        viewModelScope.launch { container.tts.status.collect { s -> _uiState.update { it.copy(ttsStatus = s) } } }
        viewModelScope.launch { container.stt.status.collect { s -> _uiState.update { it.copy(sttStatus = s) } } }
        viewModelScope.launch { container.stt.events.collect { event -> onSpeechEvent(event) } }
        viewModelScope.launch { container.navi.events.collect { line -> log(line) } }

        // Pleos 에뮬레이터·차량이면 음성 엔진을 미리 연결해 둔다 (첫 사용 지연 줄이기).
        if (container.platform.isAutomotive) connectVoice()
        log("시작: AAOS=${container.platform.isAutomotive}, Pleos 차량 서비스=${container.platform.hasPleosVehicleService}")
    }

    // ---------------------------------------------------------------- 주행 상태

    private fun applyDriving(car: DrivingState, demo: DemoFrame?) {
        val effective = when {
            car.source == DrivingState.Source.CAR && car.isDrivingMode -> car
            demo != null -> demo.step.driving
            else -> car
        }
        val previous = _uiState.value.driving
        _uiState.update {
            it.copy(
                driving = effective,
                policy = DrivingUiPolicy.from(effective),
                demo = demo?.let { frame -> DemoUi(frame.scenario.title, frame.progressLabel, frame.step.vehicle) },
            )
        }
        if (previous.isDrivingMode != effective.isDrivingMode) {
            log(if (effective.isDrivingMode) "주행 모드로 전환" else "주차 모드로 전환")
        }
    }

    fun onSpeedPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(speedPermissionGranted = granted) }
        log(if (granted) "CAR_SPEED 권한 허용" else "CAR_SPEED 권한 거부")
        if (granted) container.drivingStateMonitor.onPermissionsChanged()
    }

    // ---------------------------------------------------------------- 차량 데이터

    override fun readVehicle() {
        viewModelScope.launch {
            val vehicle = container.vehicle
            val odometer = vehicle.readOdometerKm()
            val trip = vehicle.readTripDistance()
            val door = vehicle.readDriverDoorPosition()
            _uiState.update { it.copy(odometer = odometer, trip = trip, driverDoor = door) }
            log("차량 데이터 읽기 완료")
        }
    }

    // ---------------------------------------------------------------- 음성

    override fun connectVoice() {
        container.tts.connect()
        container.stt.connect()
    }

    override fun speakTest() {
        speak(GREETING)
    }

    override fun repeatLastMessage() {
        speak(lastSpoken)
    }

    override fun listenOnce() {
        container.tts.stop()
        if (!container.stt.listen()) log("듣기를 시작할 수 없음 (STT ${container.stt.status.value})")
    }

    private fun speak(text: String) {
        lastSpoken = text
        val ok = container.tts.speak(_uiState.value.policy.clip(text))
        if (!ok) log("말하기 실패 (TTS ${container.tts.status.value}): $text")
    }

    private fun onSpeechEvent(event: PleosSpeechToText.Event) {
        when (event) {
            PleosSpeechToText.Event.Opened -> log("마이크 열림 — 말씀하세요")
            is PleosSpeechToText.Event.Partial -> _uiState.update { it.copy(heard = event.text) }
            is PleosSpeechToText.Event.Final -> onHeard(event.text)
            PleosSpeechToText.Event.Silence -> log("아무 말도 듣지 못함")
            is PleosSpeechToText.Event.Failed -> log("듣기 실패: ${event.reason}")
        }
    }

    private fun onHeard(text: String) {
        val match = matcher.match(text)
        _uiState.update { it.copy(heard = text, command = match.toString()) }
        log("들음: \"$text\" → $match")
        when (match) {
            VoiceCommandMatcher.Match.Yes -> speak("네, 알겠습니다.")
            VoiceCommandMatcher.Match.No -> speak("알겠습니다. 다음에 할게요.")
            VoiceCommandMatcher.Match.Cancel -> speak("취소했어요.")
            is VoiceCommandMatcher.Match.Command -> when (match.name) {
                "REPEAT" -> repeatLastMessage()
                "STATUS" -> speak(statusSentence())
            }
            VoiceCommandMatcher.Match.Unknown -> speak("잘 못 들었어요. 다시 말씀해 주세요.")
        }
    }

    private fun statusSentence(): String {
        val state = _uiState.value
        val speed = state.driving.speedKph?.let { "시속 ${it.toInt()}킬로미터" } ?: "속도 정보 없음"
        val mode = if (state.driving.isDrivingMode) "주행 중" else "주차 중"
        return "지금은 $mode, $speed 입니다."
    }

    // ---------------------------------------------------------------- 내비

    override fun connectNavi() {
        container.navi.connect()
    }

    override fun requestNaviLocation() {
        container.navi.requestCurrentLocation()
    }

    // ---------------------------------------------------------------- 데모 (debug 전용)

    override fun startDemo(scenarioId: String) {
        if (!BuildConfig.DEBUG) return
        val scenario = DemoScenarios.byId(scenarioId) ?: return
        // 이전 데모는 멈추기만 한다. 화면 상태(demoFrame)는 새 데모의 첫 장면이 바로 덮어쓴다.
        demoJob?.cancel()
        demoJob = viewModelScope.launch {
            DemoScenarioPlayer.play(scenario, loop = true).collect { frame -> demoFrame.value = frame }
        }
        log("데모 시작: ${scenario.title}")
    }

    override fun stopDemo() {
        val job = demoJob ?: return
        job.cancel()
        demoJob = null
        demoFrame.value = null
        log("데모 중지")
    }

    // ---------------------------------------------------------------- Gleo

    /** MainActivity 가 받은 Intent 가 Gleo Action(component=activity) 이면 처리한다. */
    fun handleIntent(intent: Intent?) {
        val action = GleoBridge.parse(intent) ?: return
        log("Gleo action: ${action.name} ${action.params}")
        // TODO(아이디어 확정 후): function 별 처리. 7초 안에 응답해야 한다.
        GleoBridge.respond(
            getApplication<Application>(),
            GleoCallback.fail(action.name, "This action is not available yet."),
        )
    }

    // ---------------------------------------------------------------- 로그

    override fun clearLog() {
        _uiState.update { it.copy(log = emptyList()) }
    }

    private fun log(message: String) {
        val line = "${LocalTime.now().format(TIME)}  $message"
        _uiState.update { it.copy(log = (listOf(line) + it.log).take(MAX_LOG_LINES)) }
    }

    private fun hasPermission(permission: String): Boolean =
        getApplication<Application>().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    override fun onCleared() {
        demoJob?.cancel()
        container.stt.cancel()
        super.onCleared()
    }

    private companion object {
        const val GREETING = "안녕하세요. 음성 안내 테스트입니다."
        const val MAX_LOG_LINES = 40
        val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
