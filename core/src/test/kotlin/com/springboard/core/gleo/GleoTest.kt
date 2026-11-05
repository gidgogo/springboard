package com.springboard.core.gleo

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GleoTest {

    // ---------- Callback ----------

    /** success 콜백은 data 를 JSON 배열 문자열로 담는다 */
    @Test
    fun successCallbackSerializesDataAsJsonArrayString() {
        val callback = GleoCallback.success(
            functionName = "GET_TRIP_SUMMARY",
            message = "The trip summary is ready.",
            data = listOf(mapOf("distanceKm" to "12.4", "minutes" to "31")),
        )

        val json = Json.parseToJsonElement(callback.toJson()).jsonObject

        assertEquals("GET_TRIP_SUMMARY", json["functionName"]!!.jsonPrimitive.content)
        assertEquals("success", json["status"]!!.jsonPrimitive.content)
        assertNull(json["mode"])
        // data 는 "문자열"이고, 그 안이 JSON 배열이다 (Pleos Callback Schema).
        val data = Json.parseToJsonElement(json["data"]!!.jsonPrimitive.content).jsonArray
        assertEquals("12.4", data[0].jsonObject["distanceKm"]!!.jsonPrimitive.content)
    }

    /** asking 콜백은 빠진 필드를 data 로 알려준다 */
    @Test
    fun askingCallbackReportsMissingField() {
        val json = Json.parseToJsonElement(
            GleoCallback.asking("SET_ALERT_LEVEL", "The alert level is required.", "level").toJson(),
        ).jsonObject

        assertEquals("asking", json["status"]!!.jsonPrimitive.content)
        assertTrue(json["data"]!!.jsonPrimitive.content.contains("missingField"))
    }

    /** silent 모드는 mode 필드를 넣는다 */
    @Test
    fun silentModeIsSerialized() {
        val json = GleoCallback(
            functionName = "PLAY_GUIDE",
            status = GleoStatus.SUCCESS,
            message = "Starting the guide now.",
            mode = GleoMode.SILENT,
        ).toJson()

        assertEquals("silent", (Json.parseToJsonElement(json) as JsonObject)["mode"]!!.jsonPrimitive.content)
    }

    /** function 이름이 규칙에 어긋나면 만들 수 없다 */
    @Test(expected = IllegalArgumentException::class)
    fun invalidFunctionNameIsRejected() {
        GleoCallback.fail("getTripSummary", "Invalid.")
    }

    // ---------- Action params ----------

    /** 일반 JSON 객체 파라미터 */
    @Test
    fun parsesPlainJsonObject() {
        assertEquals(
            mapOf("title" to "미팅", "dateTime" to "2025-02-20T06:00:00Z"),
            GleoActionParams.parse("""{"title":"미팅","dateTime":"2025-02-20T06:00:00Z"}"""),
        )
    }

    /** 문서 예시처럼 바깥 따옴표로 한 번 더 감싼 파라미터 */
    @Test
    fun parsesQuoteWrappedObjectFromDocs() {
        assertEquals(
            mapOf("title" to "미팅", "dateTime" to "2025-02-20T06:00:00Z"),
            GleoActionParams.parse("\"{\"title\":\"미팅\", \"dateTime\":\"2025-02-20T06:00:00Z\"}\""),
        )
    }

    /** JSON 문자열 안에 JSON 이 들어 있는 파라미터 */
    @Test
    fun parsesJsonEncodedAsString() {
        assertEquals(mapOf("level" to "high"), GleoActionParams.parse("\"{\\\"level\\\":\\\"high\\\"}\""))
    }

    /** 숫자·불리언·배열 값도 문자열로 받는다 */
    @Test
    fun nonStringValuesBecomeStrings() {
        assertEquals(
            mapOf("count" to "3", "urgent" to "true", "tags" to "[\"a\",\"b\"]"),
            GleoActionParams.parse("""{"count":3,"urgent":true,"tags":["a","b"]}"""),
        )
    }

    /** 비었거나 깨진 파라미터는 빈 맵 */
    @Test
    fun emptyOrBrokenParamsGiveEmptyMap() {
        assertTrue(GleoActionParams.parse(null).isEmpty())
        assertTrue(GleoActionParams.parse("").isEmpty())
        assertTrue(GleoActionParams.parse("not json").isEmpty())
    }

    // ---------- Schema rules ----------

    /** function 이름 규칙 */
    @Test
    fun functionNameRule() {
        assertTrue(GleoSchemaRules.isValidFunctionName("ADD_REMINDER"))
        assertTrue(GleoSchemaRules.isValidFunctionName("PLAY_FM2"))
        assertFalse(GleoSchemaRules.isValidFunctionName("addReminder"))
        assertFalse(GleoSchemaRules.isValidFunctionName("ADD__REMINDER"))
        assertFalse(GleoSchemaRules.isValidFunctionName("_ADD"))
    }

    /** 저장소의 예시 스키마(docs/templates)는 규칙을 통과한다 */
    @Test
    fun repositoryExampleSchemaPasses() {
        val relative = "docs/templates/gleo-schema.example.json"
        val template = listOfNotNull(
            System.getProperty("springboard.rootDir")?.let { File(it, relative) },
            File("..", relative), // 테스트 작업 디렉터리 = core/
        ).first { it.exists() }

        assertEquals(emptyList<String>(), GleoSchemaRules.validate(template.readText()))
    }

    /** 규칙 위반을 모두 찾아낸다 */
    @Test
    fun findsAllRuleViolations() {
        val schema = """
            {
              "packageName": "com.example",
              "appDescription": "한국어 설명",
              "version": "1.0.0",
              "actionCalls": [
                { "type": "function", "function": { "name": "do_it", "description": "Does it.",
                  "parameters": { "type": "object", "properties": {
                    "when": { "type": ["string", "null"], "description": "When." },
                    "kind": { "type": "string", "description": "Kind.", "enum": [1, 2] }
                  }, "required": ["missing"] } } },
                { "type": "function", "function": { "name": "A", "description": "A.",
                  "parameters": { "type": "object", "properties": {} }, "component": "receiver" } },
                { "type": "function", "function": { "name": "A", "description": "Again.",
                  "parameters": { "type": "object", "properties": {} } } },
                { "type": "function", "function": { "name": "B", "description": "B.",
                  "parameters": { "type": "object", "properties": {} } } }
              ]
            }
        """.trimIndent()

        val problems = GleoSchemaRules.validate(schema)

        val expectedFragments = listOf(
            "appDescription 는 영어로",
            "최대 3개",
            "\"do_it\" 은 대문자_밑줄",
            "여러 타입을 배열로",
            "enum 에는 문자열만",
            "\"missing\" 이 properties 에 없습니다",
            "activity 또는 service",
            "\"A\" 이 중복",
        )
        expectedFragments.forEach { fragment ->
            assertTrue("'$fragment' 위반을 찾지 못함: $problems", problems.any { it.contains(fragment) })
        }
    }

    /** JSON 객체가 아니면 한 줄 오류 */
    @Test
    fun nonObjectJsonIsOneProblem() {
        assertEquals(1, GleoSchemaRules.validate("[1,2]").size)
    }
}
