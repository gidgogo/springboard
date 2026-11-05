package com.springboard.core.gleo

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Callback의 처리 상태 (Pleos "Callback Schema Writing Guide"). */
enum class GleoStatus(val wire: String) {
    /** 정상 처리 (결과가 없어도 정상이면 success) */
    SUCCESS("success"),

    /** 필수 정보가 빠졌거나 사용자 확인이 필요함 */
    ASKING("asking"),

    /** 처리 불가 (비즈니스 조건 실패, 앱 오류, 시간 초과 등) */
    FAIL("fail"),
}

/** Gleo가 사용자에게 결과를 알리는 방식. 지정하지 않으면 Gleo 기본값(voice). */
enum class GleoMode(val wire: String) {
    VOICE("voice"),
    BEEP("beep"),

    /** 앱이 직접 TTS·음악을 재생할 때 */
    SILENT("silent"),
}

/**
 * 앱 → Gleo 응답(Callback) 한 건.
 *
 * - [message]는 완전한 영어 문장 (검색어·장소명 같은 값은 다른 언어 가능)
 * - [data]는 Gleo가 답을 만들 때 참고할 최소한의 결과. JSON 배열 "문자열"로 직렬화된다.
 *
 * 전송은 앱 모듈의 GleoBridge 가 broadcast `ai.pleos.playground.intent.action.APP_ACTION_RESPONSE` 로 한다.
 */
data class GleoCallback(
    val functionName: String,
    val status: GleoStatus,
    val message: String,
    val mode: GleoMode? = null,
    val data: List<Map<String, String>>? = null,
) {
    init {
        require(GleoSchemaRules.isValidFunctionName(functionName)) {
            "functionName must be UPPER_SNAKE_CASE: $functionName"
        }
        require(message.isNotBlank()) { "message must not be blank" }
    }

    fun toJson(): String = buildJsonObject {
        put("functionName", functionName)
        put("status", status.wire)
        put("message", message)
        mode?.let { put("mode", it.wire) }
        data?.let { rows ->
            val array = JsonArray(rows.map { row -> JsonObject(row.mapValues { (_, v) -> JsonPrimitive(v) }) })
            put("data", array.toString())
        }
    }.toString()

    companion object {
        fun success(functionName: String, message: String, data: List<Map<String, String>>? = null) =
            GleoCallback(functionName, GleoStatus.SUCCESS, message, data = data)

        fun asking(functionName: String, message: String, missingField: String? = null) =
            GleoCallback(
                functionName,
                GleoStatus.ASKING,
                message,
                data = missingField?.let { listOf(mapOf("missingField" to it)) },
            )

        fun fail(functionName: String, message: String) =
            GleoCallback(functionName, GleoStatus.FAIL, message)
    }
}
