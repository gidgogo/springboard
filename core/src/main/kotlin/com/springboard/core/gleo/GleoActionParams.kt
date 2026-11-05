package com.springboard.core.gleo

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Gleo → 앱으로 오는 Action 파라미터(Intent extra `ACTION_PARAM`, JSON 문자열)를 푼다.
 *
 * Pleos 문서 예시에서는 JSON 문자열이 바깥 따옴표로 한 번 더 감싸여 있다(`"{"title":"미팅"}"`).
 * 실제 형태가 어느 쪽이든 받을 수 있도록 두 경우를 모두 처리한다.
 */
object GleoActionParams {
    const val EXTRA_ACTION_PARAM = "ACTION_PARAM"

    private val json = Json { isLenient = true }

    fun parse(raw: String?): Map<String, String> {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return emptyMap()

        val element = parseElement(text)
            ?: text.unwrapQuotes()?.let(::parseElement)
            ?: return emptyMap()

        val obj = when {
            element is JsonObject -> element
            // "{\"title\":\"미팅\"}" 처럼 JSON 문자열 안에 JSON 이 들어 있는 경우
            element is JsonPrimitive && element.isString -> parseElement(element.content) as? JsonObject
            else -> null
        } ?: return emptyMap()

        return obj.mapValues { (_, value) ->
            if (value is JsonPrimitive) value.content else value.toString()
        }
    }

    private fun parseElement(text: String): JsonElement? =
        runCatching { json.parseToJsonElement(text) }.getOrNull()

    private fun String.unwrapQuotes(): String? =
        if (length >= 2 && first() == '"' && last() == '"') substring(1, length - 1) else null
}
