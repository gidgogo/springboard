package com.springboard.app.platform.gleo

import android.content.Context
import android.content.Intent
import android.util.Log
import com.springboard.core.gleo.GleoActionParams
import com.springboard.core.gleo.GleoCallback
import com.springboard.core.gleo.GleoSchemaRules

/**
 * Gleo AI ↔ 앱 연결 (Pleos "Gleo Interface").
 *
 * 흐름
 * 1. 앱이 assets/schema.json (App Schema) 에 음성으로 부를 function 을 최대 3개 등록 → Pleos 검토·승인
 * 2. 사용자가 말하면 Gleo 가 function 이름을 Intent action 으로, 파라미터를 extra "ACTION_PARAM"(JSON) 으로 보낸다
 *    - component = "activity" → startActivity  → MainActivity.onCreate / onNewIntent
 *    - component = "service"  → sendBroadcast  → GleoActionReceiver
 * 3. 앱은 7초 안에 결과를 broadcast 로 돌려준다 → [respond]
 *
 * 문서: docs/02-pleos/gleo-interface.md, 예시 스키마: docs/templates/gleo-schema.example.json
 */
object GleoBridge {
    const val ACTION_RESPONSE = "ai.pleos.playground.intent.action.APP_ACTION_RESPONSE"
    private const val EXTRA_PACKAGE_NAME = "packageName"
    private const val EXTRA_RESPONSE = "response"
    private const val TAG = "Gleo"

    data class Action(val name: String, val params: Map<String, String>)

    /** Gleo 가 보낸 Intent 이면 Action 으로 바꾸고, 아니면 null. */
    fun parse(intent: Intent?): Action? {
        val name = intent?.action ?: return null
        if (!GleoSchemaRules.isValidFunctionName(name)) return null
        val params = GleoActionParams.parse(intent.getStringExtra(GleoActionParams.EXTRA_ACTION_PARAM))
        return Action(name, params)
    }

    /** 처리 결과를 Gleo 에 돌려준다. 7초(GleoSchemaRules.CALLBACK_TIMEOUT_MS) 안에 보내야 한다. */
    fun respond(context: Context, callback: GleoCallback) {
        val intent = Intent(ACTION_RESPONSE).apply {
            putExtra(EXTRA_PACKAGE_NAME, context.packageName)
            putExtra(EXTRA_RESPONSE, callback.toJson())
        }
        context.sendBroadcast(intent)
        Log.i(TAG, "응답 ${callback.functionName} ${callback.status}")
    }
}
