package com.springboard.app

import android.app.Application
import com.springboard.app.platform.PlatformInfo
import com.springboard.app.platform.car.CarDrivingStateMonitor
import com.springboard.app.platform.navi.PleosNaviClient
import com.springboard.app.platform.speech.PleosSpeechToText
import com.springboard.app.platform.speech.PleosTextToSpeech
import com.springboard.app.platform.vehicle.PleosVehicleClient

/**
 * 앱 수명 동안 하나씩만 만드는 객체들.
 *
 * - 차량 연결(Car API, Pleos SDK)은 화면이 다시 만들어져도(창 크기 변경·회전) 끊기지 않도록 여기서 관리한다.
 * - SDK 객체는 처음 쓸 때 만든다(lazy). 일반 폰에서 실행해도 바로 실패하지 않게 하기 위함이다.
 */
class AppContainer(application: Application) {
    val platform = PlatformInfo(application)
    val drivingStateMonitor = CarDrivingStateMonitor(application, platform)
    val vehicle by lazy { PleosVehicleClient(application, platform) }
    val navi by lazy { PleosNaviClient(application) }
    val tts by lazy { PleosTextToSpeech(application) }
    val stt by lazy { PleosSpeechToText(application) }
}
