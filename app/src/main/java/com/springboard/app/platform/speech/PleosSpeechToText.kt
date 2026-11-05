package com.springboard.app.platform.speech

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.ErrorType
import ai.pleos.playground.stt.constant.Mode
import ai.pleos.playground.stt.listener.OnInitializeListener
import ai.pleos.playground.stt.listener.ResultListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Pleos SpeechToText SDK (차량 온디바이스 엔진, 한국어).
 *
 * - 차량 음성 서비스가 마이크를 직접 연다 → 앱에 RECORD_AUDIO 권한이 필요 없다.
 * - 받아쓰기만 한다. "응/아니/취소" 같은 해석은 core 의 VoiceCommandMatcher 로 한다.
 * - 에뮬레이터에서는 Extended Controls > Microphone 에서 호스트 마이크 입력을 켜야 한다.
 * - 에뮬레이터에서 마이크를 처음 여는 데 수 초가 걸릴 수 있어 [OPEN_TIMEOUT_MS] 를 넉넉히 둔다.
 *
 * 한 번에 한 문장만 듣는다: [listen] → (Opened) → Partial… → Final / Silence / Failed
 */
class PleosSpeechToText(context: Context) {

    enum class Status { IDLE, CONNECTING, READY, LISTENING, UNAVAILABLE }

    sealed interface Event {
        data object Opened : Event
        data class Partial(val text: String) : Event
        data class Final(val text: String) : Event
        data object Silence : Event
        data class Failed(val reason: String) : Event
    }

    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var engine: SpeechToText? = null

    private val _status = MutableStateFlow(Status.IDLE)
    val status: StateFlow<Status> = _status.asStateFlow()

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 16)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    private var opened = false
    private var lastWords: String? = null

    private val connectTimeout = Runnable { unavailable("응답 없음") }
    private val openTimeout = Runnable { finish(Event.Failed("마이크가 열리지 않음")) }
    private val listenTimeout = Runnable { finish(lastWords?.let { Event.Final(it) } ?: Event.Silence) }

    private val resultListener = object : ResultListener {
        override fun onReady() = onMain { markOpened() }
        override fun onStartedRecognition() = onMain { markOpened() }
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onUpdatedEpdData(on: Long, off: Long) = Unit

        override fun onUpdated(stt: String, completed: Boolean) = onMain {
            if (_status.value != Status.LISTENING) return@onMain
            markOpened()
            if (stt.isNotBlank()) {
                lastWords = stt
                _events.tryEmit(Event.Partial(stt))
            }
            if (completed) finish(lastWords?.let { Event.Final(it) } ?: Event.Silence)
        }

        override fun onEndedRecognition() = onMain {
            if (_status.value != Status.LISTENING) return@onMain
            // 최종 문장이 조금 늦게 올 수 있어 잠깐 기다린다.
            main.removeCallbacks(listenTimeout)
            main.postDelayed(listenTimeout, FINAL_WAIT_MS)
        }

        override fun onError(errorType: ErrorType) = onMain {
            Log.w(TAG, "STT 오류: $errorType")
            finish(if (errorType == ErrorType.TIMEOUT) Event.Silence else Event.Failed(errorType.toString()))
        }
    }

    fun connect() = onMain {
        if (_status.value != Status.IDLE) return@onMain
        _status.value = Status.CONNECTING
        main.postDelayed(connectTimeout, CONNECT_TIMEOUT_MS)
        try {
            val created = SpeechToText(appContext, Mode.ON_DEVICE)
            engine = created
            created.addListener(resultListener)
            created.initialize(
                object : OnInitializeListener {
                    override fun onSuccess() = onMain { ready() }
                    override fun onFailure(errorType: ErrorType) = onMain { unavailable(errorType.toString()) }
                },
            )
        } catch (e: RuntimeException) {
            unavailable(e.toString())
        } catch (e: LinkageError) {
            unavailable(e.toString())
        }
    }

    /** 한 문장 듣기 시작. 준비되지 않았거나 이미 듣는 중이면 false. */
    fun listen(): Boolean {
        val current = engine ?: return false
        if (_status.value != Status.READY) return false
        opened = false
        lastWords = null
        _status.value = Status.LISTENING
        main.postDelayed(openTimeout, OPEN_TIMEOUT_MS)
        return try {
            current.request()
            true
        } catch (e: RuntimeException) {
            finish(Event.Failed(e.javaClass.simpleName))
            false
        }
    }

    /** 듣기를 취소한다 (결과 이벤트 없음). */
    fun cancel() {
        if (_status.value != Status.LISTENING) return
        removeTimers()
        closeMicrophone()
        _status.value = Status.READY
    }

    fun release() = onMain {
        removeTimers()
        main.removeCallbacks(connectTimeout)
        engine?.let { current ->
            runCatching { current.stop() }
            runCatching { current.removeListener(resultListener) }
            runCatching { current.release() }
        }
        engine = null
        _status.value = Status.IDLE
    }

    private fun ready() {
        val current = engine ?: return
        if (_status.value != Status.CONNECTING) return
        main.removeCallbacks(connectTimeout)
        try {
            current.setLocale(Locale.KOREA)
            _status.value = Status.READY
            Log.i(TAG, "Pleos STT 준비됨 (on-device, ko-KR)")
        } catch (e: RuntimeException) {
            unavailable(e.toString())
        }
    }

    private fun markOpened() {
        if (_status.value != Status.LISTENING || opened) return
        opened = true
        main.removeCallbacks(openTimeout)
        main.postDelayed(listenTimeout, LISTEN_TIMEOUT_MS)
        _events.tryEmit(Event.Opened)
    }

    private fun finish(event: Event) {
        if (_status.value != Status.LISTENING) return
        removeTimers()
        closeMicrophone()
        _status.value = Status.READY
        _events.tryEmit(event)
    }

    private fun unavailable(reason: String) {
        if (_status.value == Status.UNAVAILABLE) return
        main.removeCallbacks(connectTimeout)
        Log.i(TAG, "Pleos STT 사용 불가: $reason")
        engine?.let { current ->
            runCatching { current.removeListener(resultListener) }
            runCatching { current.release() }
        }
        engine = null
        _status.value = Status.UNAVAILABLE
    }

    private fun closeMicrophone() {
        runCatching { engine?.stop() }
    }

    private fun removeTimers() {
        main.removeCallbacks(openTimeout)
        main.removeCallbacks(listenTimeout)
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else main.post(block)
    }

    private companion object {
        const val TAG = "PleosStt"
        const val CONNECT_TIMEOUT_MS = 5_000L
        const val OPEN_TIMEOUT_MS = 6_000L
        const val LISTEN_TIMEOUT_MS = 8_000L
        const val FINAL_WAIT_MS = 1_500L
    }
}
