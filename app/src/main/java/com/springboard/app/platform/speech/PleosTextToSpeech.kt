package com.springboard.app.platform.speech

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Pleos TextToSpeech SDK (차량 온디바이스 엔진).
 *
 * - Pleos Connect 에는 표준 Android TTS 엔진이 없을 수 있어 Pleos SDK 를 쓴다.
 * - Mode.ON_DEVICE 는 네트워크·앱 등록(클라이언트 ID/시크릿) 없이 동작한다.
 *   (Playground 콘솔의 TTS "준비 중" 표시는 서버 모드 신청에 관한 것)
 * - 음성 안내 앞에는 150~200ms 선행 묵음을 권장한다 (Pleos "TTS/음성 안내 선행 묵음 가이드").
 * - 재생 중 오디오 포커스 정책은 docs/02-pleos/audio-and-voice.md 를 따른다.
 *
 * Pleos 음성 서비스가 없는 기기(일반 폰)에서는 [Status.UNAVAILABLE] 이 된다.
 */
class PleosTextToSpeech(context: Context) {

    enum class Status { IDLE, CONNECTING, READY, SPEAKING, UNAVAILABLE }

    private val appContext = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var engine: TextToSpeech? = null

    private val _status = MutableStateFlow(Status.IDLE)
    val status: StateFlow<Status> = _status.asStateFlow()

    private val connectTimeout = Runnable { unavailable("응답 없음") }

    private val eventListener = object : EventListener {
        override fun onReady() = Unit
        override fun onStart() = onMain { _status.value = Status.SPEAKING }
        override fun onDone() = onMain { if (_status.value == Status.SPEAKING) _status.value = Status.READY }
        override fun onStop() = onMain { if (_status.value == Status.SPEAKING) _status.value = Status.READY }
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onError(errorType: ErrorType) = onMain {
            Log.w(TAG, "TTS 오류: $errorType")
            if (_status.value == Status.SPEAKING) _status.value = Status.READY
        }
    }

    /** 엔진에 연결한다. 결과는 [status] 로 확인한다. */
    fun connect() = onMain {
        if (_status.value != Status.IDLE) return@onMain
        _status.value = Status.CONNECTING
        main.postDelayed(connectTimeout, CONNECT_TIMEOUT_MS)
        try {
            val created = TextToSpeech(appContext, Mode.ON_DEVICE)
            engine = created
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

    /** 말하기. 준비되지 않았으면 false. 이전 발화는 멈추고 새 문장을 읽는다. */
    fun speak(text: String): Boolean {
        val current = engine ?: return false
        if (_status.value != Status.READY && _status.value != Status.SPEAKING) return false
        return try {
            current.stop()
            current.speak(text, true)
            true
        } catch (e: RuntimeException) {
            Log.w(TAG, "TTS speak 실패", e)
            false
        }
    }

    fun stop() {
        runCatching { engine?.stop() }
    }

    fun release() = onMain {
        main.removeCallbacks(connectTimeout)
        engine?.let { current ->
            runCatching { current.removeEventListener(eventListener) }
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
            current.addEventListener(eventListener)
            current.setLocale(Locale.KOREA)
            _status.value = Status.READY
            Log.i(TAG, "Pleos TTS 준비됨 (on-device, ko-KR)")
        } catch (e: RuntimeException) {
            unavailable(e.toString())
        }
    }

    private fun unavailable(reason: String) {
        if (_status.value == Status.UNAVAILABLE) return
        main.removeCallbacks(connectTimeout)
        Log.i(TAG, "Pleos TTS 사용 불가: $reason")
        engine?.let { runCatching { it.release() } }
        engine = null
        _status.value = Status.UNAVAILABLE
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else main.post(block)
    }

    private companion object {
        const val TAG = "PleosTts"
        const val CONNECT_TIMEOUT_MS = 5_000L
    }
}
