package com.springboard.core.gleo

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Gleo Interface App Schema(`assets/schema.json`) 작성 규칙 검사기.
 *
 * 규칙 출처: Pleos "App Schema Writing Guide" (document.pleos.ai → Gleo Interface)
 * - actionCalls 는 최대 3개
 * - function name 은 모두 대문자, 단어는 밑줄(_)로 구분, 서로 중복 금지
 * - 앱·function·parameter description 은 영어로 (예시 문장 넣지 않기)
 * - parameter type 은 string, number, boolean, array 중 하나 (배열로 여러 타입 지정 불가)
 * - enum 은 문자열만
 * - required 에는 필수 parameter 만
 * - component 는 "activity"(startActivity) 또는 "service"(sendBroadcast)
 *
 * 스키마는 Pleos 검토·승인 뒤에 활성화된다. 이 검사는 제출 전 실수를 줄이는 용도다.
 */
object GleoSchemaRules {
    const val MAX_ACTION_CALLS = 3

    /** Gleo는 7초 안에 콜백이 오지 않으면 Action을 취소한다. */
    const val CALLBACK_TIMEOUT_MS = 7_000L

    val ALLOWED_PARAMETER_TYPES = setOf("string", "number", "boolean", "array")
    val ALLOWED_COMPONENTS = setOf("activity", "service")

    private val FUNCTION_NAME = Regex("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$")
    private val HANGUL = Regex("[\\uAC00-\\uD7A3\\u3131-\\u318E]")
    private val json = Json { ignoreUnknownKeys = true }

    fun isValidFunctionName(name: String): Boolean = FUNCTION_NAME.matches(name)

    /** 스키마 JSON 문자열을 검사해 규칙 위반 목록을 돌려준다. 빈 목록이면 통과. */
    fun validate(schemaJson: String): List<String> {
        val root = runCatching { json.parseToJsonElement(schemaJson) }.getOrNull() as? JsonObject
            ?: return listOf("JSON 객체가 아니거나 형식이 깨졌습니다.")
        val problems = mutableListOf<String>()

        for (key in listOf("packageName", "appDescription", "version")) {
            if (root.string(key).isNullOrBlank()) problems += "$key 가 비어 있습니다."
        }
        root.string("appDescription")?.let { checkEnglish("appDescription", it, problems) }

        val calls = root["actionCalls"] as? JsonArray
        if (calls == null || calls.isEmpty()) {
            problems += "actionCalls 가 비어 있습니다."
            return problems
        }
        if (calls.size > MAX_ACTION_CALLS) {
            problems += "actionCalls 는 최대 ${MAX_ACTION_CALLS}개입니다 (현재 ${calls.size}개)."
        }

        val seen = mutableSetOf<String>()
        calls.forEachIndexed { index, call ->
            val where = "actionCalls[$index]"
            val callObj = call as? JsonObject
            if (callObj == null) {
                problems += "$where 가 객체가 아닙니다."
                return@forEachIndexed
            }
            if (callObj.string("type") != "function") problems += "$where.type 은 \"function\" 이어야 합니다."
            val function = callObj["function"] as? JsonObject
            if (function == null) {
                problems += "$where.function 이 없습니다."
                return@forEachIndexed
            }
            validateFunction(where, function, seen, problems)
        }
        return problems
    }

    private fun validateFunction(
        where: String,
        function: JsonObject,
        seen: MutableSet<String>,
        problems: MutableList<String>,
    ) {
        val name = function.string("name")
        when {
            name.isNullOrBlank() -> problems += "$where.function.name 이 없습니다."
            !isValidFunctionName(name) -> problems += "$where.function.name \"$name\" 은 대문자_밑줄 형식이어야 합니다."
            !seen.add(name) -> problems += "function name \"$name\" 이 중복됩니다."
        }

        val description = function.string("description")
        if (description.isNullOrBlank()) {
            problems += "$where.function.description 이 없습니다."
        } else {
            checkEnglish("$where.function.description", description, problems)
        }

        function.string("component")?.let { component ->
            if (component !in ALLOWED_COMPONENTS) {
                problems += "$where.function.component 는 activity 또는 service 여야 합니다."
            }
        }

        val parameters = function["parameters"] as? JsonObject
        if (parameters == null) {
            problems += "$where.function.parameters 가 없습니다."
            return
        }
        if (parameters.string("type") != "object") {
            problems += "$where.function.parameters.type 은 \"object\" 여야 합니다."
        }
        val properties = parameters["properties"] as? JsonObject ?: JsonObject(emptyMap())
        properties.forEach { (paramName, spec) -> validateParameter("$where.$paramName", spec, problems) }

        (parameters["required"] as? JsonArray)?.forEach { required ->
            val requiredName = (required as? JsonPrimitive)?.content
            if (requiredName == null || requiredName !in properties.keys) {
                problems += "$where.required 의 \"$requiredName\" 이 properties 에 없습니다."
            }
        }
    }

    private fun validateParameter(where: String, spec: JsonElement, problems: MutableList<String>) {
        val specObj = spec as? JsonObject
        if (specObj == null) {
            problems += "$where 정의가 객체가 아닙니다."
            return
        }
        val type = specObj["type"]
        when {
            type is JsonArray -> problems += "$where.type 에 여러 타입을 배열로 지정할 수 없습니다."
            (type as? JsonPrimitive)?.content !in ALLOWED_PARAMETER_TYPES ->
                problems += "$where.type 은 ${ALLOWED_PARAMETER_TYPES.joinToString()} 중 하나여야 합니다."
        }
        val description = specObj.string("description")
        if (description.isNullOrBlank()) {
            problems += "$where.description 이 없습니다."
        } else {
            checkEnglish("$where.description", description, problems)
        }
        (specObj["enum"] as? JsonArray)?.forEach { value ->
            val primitive = value as? JsonPrimitive
            if (primitive == null || !primitive.isString) problems += "$where.enum 에는 문자열만 쓸 수 있습니다."
        }
    }

    private fun checkEnglish(where: String, text: String, problems: MutableList<String>) {
        if (HANGUL.containsMatchIn(text)) problems += "$where 는 영어로 작성해야 합니다."
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
}
