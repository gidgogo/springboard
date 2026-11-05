# Gleo Interface — Gleo AI 음성으로 우리 앱 기능 부르기

> 출처: Pleos 문서 "Gleo Interface — Core Concepts / Delivering the Schema / App Schema Writing Guide / Callback Schema Writing Guide /
> Code Details with Examples"(document.pleos.ai, 2026-10). LLM SDK는 2026-10-19 종료되었고 Gleo 연동은 이 인터페이스로 일원화됐다.
>
> 왜 중요한가: 주행 중 "말로 하는" 경험을 차량 AI에 직접 붙이는 방법이라 **기술 활용성·차량특화성** 양쪽에 강하다.
> 다만 **스키마는 Pleos 검토·승인 후 활성화**되고, 에뮬레이터에서 Gleo 호출이 되는지는 문서에 없다 → 설명회·문의로 확인.

## 1. 흐름

```
사용자: "오늘 운행 몇 km였어?"
  → Gleo AI(LLM)가 우리 앱의 App Schema 에서 맞는 function 을 고름
  → Intent(action = function 이름, extra "ACTION_PARAM" = 파라미터 JSON) 로 우리 앱 호출
       component = "activity" → startActivity   (화면이 필요할 때)
       component = "service"  → sendBroadcast   (화면 없이 처리할 때)
  → 우리 앱이 처리하고 7초 안에 Callback broadcast 로 결과를 돌려줌
  → Gleo 가 결과로 최종 답을 말하거나(voice) 효과음(beep) 또는 무음(silent)
```

## 2. App Schema (`app/src/main/assets/schema.json`)

- 파일 이름은 반드시 `schema.json`, 위치는 `assets/`.
- Gleo 앱은 **부팅 시 한 번**, 그리고 앱 설치/삭제 때 모든 앱의 `assets/schema.json`을 읽어 DB에 저장.
- 필드: `packageName`, `appDescription`(영어 한두 문장), `version`(App Market 등록 버전), `actionCalls`(**최대 3개**), (예시에 있는) `invokeWord`(한국어 호출어)
- function: `type`="function", `name`(**대문자_밑줄**, 고유), `description`(영어, **예시 문장 금지**, 한두 문장), `parameters`(type="object", properties, required), `component`("activity" 기본 / "service")
- parameter type은 `string` · `number` · `boolean` · `array` 중 **하나만**. `enum`은 문자열만. `required`에는 필수만.
- 다른 앱 사용을 막거나 Gleo 동작을 조작하려는 문구 금지.

예시 스키마: [`docs/templates/gleo-schema.example.json`](../templates/gleo-schema.example.json)
규칙 검사기: [`GleoSchemaRules.validate()`](../../core/src/main/kotlin/com/springboard/core/gleo/GleoSchemaRules.kt) — `./gradlew :core:test`가 예시 스키마를 검사한다.

## 3. Action 받기

매니페스트에 function 이름을 action으로 등록한다 (이 저장소는 예시가 주석으로 들어 있음).

```xml
<!-- component = "activity" -->
<activity android:name=".MainActivity" ...>
    <intent-filter>
        <action android:name="OPEN_TRIP_DETAIL" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>

<!-- component = "service" (broadcast) -->
<receiver android:name=".platform.gleo.GleoActionReceiver" android:exported="true">
    <intent-filter>
        <action android:name="GET_TRIP_SUMMARY" />
    </intent-filter>
</receiver>
```

- 파라미터: `intent.getStringExtra("ACTION_PARAM")` → JSON. 문서 예시처럼 바깥 따옴표가 한 번 더 붙어 올 수 있어
  [`GleoActionParams.parse()`](../../core/src/main/kotlin/com/springboard/core/gleo/GleoActionParams.kt)가 두 경우를 모두 처리한다.
- activity 방식은 [`MainViewModel.handleIntent`](../../app/src/main/java/com/springboard/app/MainViewModel.kt)(onCreate/onNewIntent), service 방식은 [`GleoActionReceiver`](../../app/src/main/java/com/springboard/app/platform/gleo/GleoActionReceiver.kt)에서 받는다.
- exported receiver는 외부 입력이다 → 파라미터 검증, 민감 기능 금지(보안 가이드).

## 4. Callback 보내기

broadcast action `ai.pleos.playground.intent.action.APP_ACTION_RESPONSE`, extra `packageName`, `response`(JSON 문자열).

| 필드 | 값 |
|---|---|
| `functionName` | 호출된 function 이름과 같게 |
| `status` | `success`(정상, 결과 없어도) · `asking`(필수 정보 부족·확인 필요) · `fail`(조건 실패·오류·시간 초과) |
| `message` | 완전한 **영어** 문장 (값은 다른 언어 가능) |
| `mode` | `voice`(기본) · `beep` · `silent`(앱이 직접 TTS/음악 재생할 때) |
| `data` | 결과를 **JSON 배열 문자열**로. Gleo가 답을 만드는 데 필요한 것만. 강력 권장 |

```kotlin
GleoBridge.respond(context, GleoCallback.success("GET_TRIP_SUMMARY", "The trip summary is ready.",
    data = listOf(mapOf("distanceKm" to "12.4", "minutes" to "31"))))
GleoBridge.respond(context, GleoCallback.asking("SET_ALERT_LEVEL", "The alert level is required.", missingField = "level"))
GleoBridge.respond(context, GleoCallback.fail("OPEN_TRIP_DETAIL", "No trip was found for that date."))
```

- **Gleo는 7초 후 Action을 취소한다.** 느린 작업(네트워크)은 먼저 `asking`/`fail`로 답하거나 캐시를 쓴다.

## 5. 에뮬레이터에서 직접 쏴 보기 (Gleo 없이)

기기 쪽 셸이 따옴표를 한 번 더 해석하므로 명령 전체를 큰따옴표로 감싸고, JSON의 큰따옴표는 `\"`로 쓴다.

```bash
# activity 방식 (컴포넌트를 직접 지정하므로 intent-filter 주석을 풀지 않아도 된다)
adb shell "am start -n com.springboard.app/.MainActivity -a OPEN_TRIP_DETAIL --es ACTION_PARAM '{\"date\":\"2026-11-05\"}'"

# service(broadcast) 방식 — 매니페스트의 <receiver> 주석을 푼 뒤
adb shell "am broadcast -n com.springboard.app/.platform.gleo.GleoActionReceiver -a GET_TRIP_SUMMARY --es ACTION_PARAM '{}'"

# 앱 로그
adb logcat -s Gleo GleoReceiver
```

## 6. Gleo 화면·상태 (디자인 참고)

- 상태: Sleep → Listening_Passive → Listening_Active → Thinking → (Asking) → Speaking → Standby(응답 화면 있으면 5초 유지) → Sleep
- Gleo 패널은 GNB 바로 아래 레이어 → 다른 앱 화면을 잠시 가릴 수 있다.
- 패널: Bubble(발화·응답), Template(list/card), Chips(선택지 버튼).

## 7. 기획서에 쓰는 법

- 5번(개발 계획) SDK 표에 "Gleo Interface — App Schema 3개 function"을 넣고, 각 function의 음성 예시와 Callback 결과를 적는다.
- "스키마 승인 후 활성화" 조건과, 승인 전에도 앱 안 STT 버튼으로 같은 기능을 쓸 수 있는 **대체 경로**를 함께 적는다.
