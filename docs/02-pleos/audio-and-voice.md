# 오디오 · 음성 가이드

> 출처: Pleos 문서 "오디오 포커스 개발 가이드", "Suspend 모드 오디오 포커스 개발 가이드", "TTS/음성 안내 선행 묵음 가이드",
> "Music/Audio 앱 개발 가이드"(document.pleos.ai, 2026-10), AAOS 오디오 포커스 문서.
> 주행 중 기능은 대부분 "소리"로 전달된다. 소리가 다른 앱(내비·음악·통화·Gleo)과 부딪히지 않게 하는 규칙이다.

## 1. 오디오 포커스 기본

- 재생 전에 반드시 `AudioAttributes` + `AudioFocusRequest`로 포커스를 요청한다.
- 결과는 Pleos **Interaction Matrix**(현재 보유자 컨텍스트 × 들어오는 요청 컨텍스트)로 정해진다.
  - **E**(Exclusive): 새 요청이 가져가고 기존 보유자는 잃음
  - **C**(Concurrent): 함께 재생 (보통 duck = 볼륨 낮춤)
  - **R**(Reject): 새 요청 거부
  - 보유자가 여럿이면 가장 보수적인 결과 적용 (R > E > C)
- 포커스 변화 이벤트를 **반드시 처리**한다.

| 이벤트 | 앱이 할 일 |
|---|---|
| `AUDIOFOCUS_GAIN` | 정상 재생 |
| `AUDIOFOCUS_LOSS` | 즉시 정지 |
| `AUDIOFOCUS_LOSS_TRANSIENT` | 일시정지, 나중에 복귀 가능 |
| `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK` | 볼륨 낮춤(duck), 끝나면 원복 |

컨텍스트별로 기억할 것
- **NAVIGATION ↔ MUSIC**은 함께 재생(C). 함께 나오려면 안내 쪽이 `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`으로 요청.
- **CALL** 중 음악 요청은 거부(R), 내비 안내는 허용(C).
- **EMERGENCY / SAFETY**는 거의 모든 것보다 우선. 긴급음 중 음악·알림 요청은 실패해야 정상.
- **VOICE_COMMAND(Gleo)**가 불리면 앱은 `AUDIOFOCUS_LOSS_TRANSIENT`를 받는다 → **즉시 멈추고 필요하면 포커스 반납**.

AudioAttributes usage → 컨텍스트 (AAOS)

| 컨텍스트 | usage |
|---|---|
| MUSIC | `USAGE_MEDIA`, `USAGE_GAME`, `USAGE_UNKNOWN` |
| NAVIGATION | `USAGE_ASSISTANCE_NAVIGATION_GUIDANCE` |
| VOICE_COMMAND | `USAGE_ASSISTANT` |
| NOTIFICATION | `USAGE_NOTIFICATION*` |
| ALARM / EMERGENCY / SAFETY / VEHICLE_STATUS / ANNOUNCEMENT | 각 이름의 usage (Android 11+) |

> 우리 앱의 짧은 음성 안내를 어떤 usage로 낼지는 기능 성격으로 정한다. 운전 관련 짧은 안내라면 음악과 공존(duck)하도록
> `USAGE_ASSISTANCE_NAVIGATION_GUIDANCE` + `GAIN_TRANSIENT_MAY_DUCK`을 우선 검토하고, 에뮬레이터에서 음악 재생 중 실제 동작을 확인한다.
> Pleos TTS SDK에는 `setAudioAttributes(...)`가 있다.

## 2. Suspend(시동 OFF 후 저전력) — 필수 규칙

- 진입 시: **재생 즉시 중단 → 포커스 반납(abandon) → 재생 상태 저장 → 플레이어 등 자원 해제.** 진입 중 새 재생·포커스 요청 금지.
- Resume 시: 포커스는 **자동 복구되지 않는다.** 상태를 다시 보고 필요하면 재요청. 자동 재생 여부는 정책에 따름(기본은 자동 재생하지 않기).
- 백그라운드에서 소리를 계속 내거나, 다음을 위해 포커스를 쥐고 있지 않는다.

## 3. 음성 안내 앞 선행 묵음 150~200ms

- 모든 음성 안내 시작 전에 **150ms(최소)~200ms** 묵음을 넣어 첫 음절 잘림을 막는다. 200ms 초과는 체감 지연.
- 넣는 위치 우선순위: PCM/WAV 데이터 앞 패딩 > TTS 결과 후처리 > 플레이어 지연(비권장).
- 한 문장으로 이어지는 연속 음성에는 중간에 넣지 않는다. 새 세션이면 다시 넣는다.
- 묵음은 출력 경로 초기화 문제를 대신하지 않는다 — 포커스·세션·디코더 준비를 먼저 확인.

## 4. 음악·오디오 앱이라면 (Music/Audio 카테고리)

- `MediaBrowserService` + `MediaSession` **필수** (차량 미디어 화면·스티어링 휠 키 연동). 없으면 배포 제외 대상.
- 매니페스트 `<application android:appCategory="audio">` → 미디어 앱 라이브러리 연동.
- 하위 카테고리(음악/팟캐스트/오디오북/라디오)를 명확히. 연결 시 **자동 재생 금지**.
- `AUDIOFOCUS_LOSS`(예: 게임 실행)면 세션을 active로 두지 않는다.
- 참고: Media3 `MediaLibraryService`(androidx.media3) — https://developer.android.com/media/media3/session/background-playback

## 5. 주행 중 음성 UX 패턴

| 원칙 | 예 |
|---|---|
| 짧게, 한 번에 하나 | "배터리 30%예요. 다음 휴게소 충전소를 경유지로 넣을까요?" |
| 예/아니오로 끝나는 질문 | STT 결과를 `VoiceCommandMatcher`로 예/아니오/취소 판별 |
| 실패해도 안전 | 못 알아들으면 한 번만 다시 묻고, 그래도 안 되면 조용히 포기 (정차 후 화면으로) |
| 운전 중 긴 설명 금지 | 상세는 정차 후 화면에서 |
| 말하는 중 다른 소리 고려 | 내비 안내·통화·Gleo가 우선. 포커스를 잃으면 즉시 멈춤 |
| 다시 듣기 | "다시" 명령 지원 (이 저장소 기본 명령 REPEAT) |

이 저장소에서 쓰는 곳
- TTS: [`PleosTextToSpeech`](../../app/src/main/java/com/springboard/app/platform/speech/PleosTextToSpeech.kt) — 온디바이스, ko-KR
- STT: [`PleosSpeechToText`](../../app/src/main/java/com/springboard/app/platform/speech/PleosSpeechToText.kt) — 한 문장 듣기, 열림·듣기 시간 제한
- 해석: [`VoiceCommandMatcher`](../../core/src/main/kotlin/com/springboard/core/voice/VoiceCommandMatcher.kt) — 예/아니오/취소/키워드
- TODO: 오디오 포커스 요청과 선행 묵음은 기능이 정해지면 `PleosTextToSpeech`에 추가 (TTS 엔진 내부 처리 여부를 에뮬레이터에서 먼저 확인)

## 6. 검증 체크리스트

- [ ] 음악 재생 중 우리 안내 → 음악이 낮아졌다가 돌아온다 (C)
- [ ] 우리 안내 중 Gleo 호출 → 안내가 즉시 멈춘다
- [ ] 시동 OFF(Suspend) → 소리 멈춤, 다시 켜도 자동으로 말하지 않는다
- [ ] 첫 음절이 잘리지 않는다
