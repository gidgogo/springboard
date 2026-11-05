package com.springboard.app.platform.gleo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.springboard.core.gleo.GleoCallback

/**
 * component = "service" 인 Gleo Action 을 받는 수신기.
 * AndroidManifest.xml 의 <receiver> 주석을 풀고, schema.json 의 function 이름을 action 으로 등록해야 동작한다.
 *
 * TODO(아이디어 확정 후): function 별 실제 처리로 바꾼다. 지금은 "아직 구현 안 됨"으로 응답한다.
 */
class GleoActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = GleoBridge.parse(intent) ?: return
        Log.i(TAG, "Gleo action ${action.name} params=${action.params.keys}")

        val callback = when (action.name) {
            // 예: "GET_TRIP_SUMMARY" -> GleoCallback.success(action.name, "The trip summary is ready.", data = ...)
            else -> GleoCallback.fail(action.name, "This action is not available yet.")
        }
        GleoBridge.respond(context, callback)
    }

    private companion object {
        const val TAG = "GleoReceiver"
    }
}
