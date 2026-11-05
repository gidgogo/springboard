package com.springboard.app

import android.app.Application

/**
 * 앱 전체에서 하나만 있어야 하는 객체(차량 연결, SDK 세션)를 [AppContainer]에 모아 둔다.
 * 규모가 작은 공모전 앱이라 Hilt 같은 DI 라이브러리 없이 수동으로 주입한다.
 */
class SpringboardApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
