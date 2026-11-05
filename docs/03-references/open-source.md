# 참고할 오픈소스 · 레퍼런스

> 라이선스를 꼭 본다. **LICENSE 파일이 없는 저장소는 "모든 권리 보유"**라서 코드를 가져다 쓸 수 없다(읽고 배우는 것만).
> 가져다 쓴 오픈소스는 [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md)에 출처와 라이선스를 적는다 (공모전 이용약관 제7조 7항: 출처 표기 의무).

## 1. Pleos 공식 (가장 먼저 볼 것)

| 저장소 | 무엇을 배우나 | 라이선스 |
|---|---|---|
| [Pleos-Playground/pleos-sample-vehicle-app](https://github.com/Pleos-Playground/pleos-sample-vehicle-app) | Vehicle SDK 2.3.2. Door Position으로 본 **capability → read → set → register/unregister** 표준 흐름, 19개 도메인·442개 공개 메서드 카탈로그, Compose 대시보드, 실제 AVD 스크린샷 | 파일 없음 |
| [Pleos-Playground/pleos-sample-navi-app](https://github.com/Pleos-Playground/pleos-sample-navi-app) | NaviHelper 3.0.5 전체 API 호출과 `NaviHelperEventListener` 콜백, 경로·경유지 요청 예시 데이터(`NaviTestData.kt`), Hilt | 파일 없음 |
| [Pleos-Playground/pleos-sample-fusedlocation-app](https://github.com/Pleos-Playground/pleos-sample-fusedlocation-app) | FusedLocation 1.1.3, `isSupported` 확인, 근접 알림(지오펜스), 모의 위치 테스트 | 파일 없음 |
| [Pleos-Playground/pleos-playground-api](https://github.com/Pleos-Playground/pleos-playground-api) | 공식 기술지원 포럼 — **Discussions(Q&A·FAQ·공지)**, Wiki | — |

> 공식 샘플도 LICENSE 파일이 없다. 문서에 나온 **API 호출 방식을 따라 쓰는 것**은 괜찮지만, 샘플 파일을 통째로 복사하지 말고 우리 코드로 다시 쓴다.

## 2. Google / AOSP 차량 레퍼런스

| 자료 | 내용 |
|---|---|
| [android/car-samples](https://github.com/android/car-samples) (Apache-2.0) | Car App Library 샘플(내비·주차·충전 등 템플릿 앱)과 AAOS용 `car-lib` 샘플. ⚠️ Car App Library 템플릿 앱은 템플릿 호스트가 필요한데 Pleos(Google 서비스 없는 AAOS)에서의 지원은 확인되지 않음 → **일반 Activity 앱을 기본으로** |
| [android/car-codelabs](https://github.com/android/car-codelabs) (Apache-2.0) | Android for Cars 코드랩 예제 프로젝트 (주차 앱을 AAOS에 맞추기 등). 코드랩 문서와 함께 따라 하기 좋다 |
| [Add AAOS support to your parked app](https://developer.android.com/training/cars/parked/automotive-os) | 주차 앱 요구사항, `CarUxRestrictionsManager` 사용법, 주행 시뮬레이션 테스트, 인셋·컷아웃 |
| [Car app quality guidelines](https://developer.android.com/docs/quality-guidelines/car-app-quality) | DD(주의 분산)·AN(내비게이션)·화면 방향 등 품질 기준 |
| [Test with the AAOS emulator](https://developer.android.com/training/cars/testing/emulator) | 주행 시뮬레이션, 일반 AAOS 이미지 |
| [Android for Cars: media apps](https://developer.android.com/training/cars/media) | MediaBrowserService·MediaSession (오디오 앱이라면 필수) |
| [Driver distraction guidelines (AOSP)](https://source.android.com/docs/automotive/driver_distraction/guidelines) | Pleos가 준수를 요구하는 AAOS 주의 분산 가이드라인 |
| [Audio focus (AOSP)](https://source.android.com/docs/automotive/audio/audio-focus) | 차량 오디오 포커스 상호작용 |
| [VehiclePropertyIds](https://developer.android.com/reference/android/car/VehiclePropertyIds) · [CarPropertyManager](https://developer.android.com/reference/android/car/hardware/property/CarPropertyManager) · [CarUxRestrictions](https://developer.android.com/reference/android/car/drivingstate/CarUxRestrictions) | AAOS Car API 레퍼런스 |
| [Google Design for Driving](https://developers.google.com/cars/design) | 터치 영역 76dp, 간격, 대비, 글자 크기 등 차량 UI 원칙 |
| [android/skills — android-for-cars-parked-apps](https://github.com/android/skills/tree/main/cars/android-for-cars-parked-apps) | 기존 앱을 차량 주차 앱으로 바꾸는 체크리스트 |
| AOSP `packages/services/Car/tests/EmbeddedKitchenSinkApp` ([cs.android.com](https://cs.android.com/android/platform/superproject/main/+/main:packages/services/Car/tests/EmbeddedKitchenSinkApp/)) | Car API를 실제로 호출하는 테스트 앱 (Apache-2.0) |

## 3. Android 구조·UI 레퍼런스 (Apache-2.0)

| 자료 | 언제 보나 |
|---|---|
| [Guide to app architecture](https://developer.android.com/topic/architecture) | UI 계층 / 데이터 계층, 단방향 데이터 흐름 |
| [android/nowinandroid](https://github.com/android/nowinandroid) | 모듈 나누기, Compose, 테스트까지 갖춘 실전 앱 |
| [android/compose-samples](https://github.com/android/compose-samples) | Compose 화면 예제 |
| [android/architecture-samples](https://github.com/android/architecture-samples) | ViewModel·Repository 기본형 |
| [Material 3 for Compose](https://developer.android.com/develop/ui/compose/designsystems/material3) | 테마·색·글꼴 |
| [Compose 적응형 레이아웃](https://developer.android.com/develop/ui/compose/layouts/adaptive) | portrait/landscape·창 크기 대응 |

## 4. 라이브러리

### 이미 쓰는 것

| 라이브러리 | 용도 | 라이선스 |
|---|---|---|
| AndroidX Core / Lifecycle / Activity / Compose / Material3 | 기본 UI·생명주기 | Apache-2.0 |
| Kotlin Coroutines | 비동기·Flow | Apache-2.0 |
| kotlinx.serialization (JSON) | Gleo 스키마·콜백 JSON | Apache-2.0 |
| JUnit 4 | 단위 테스트 | EPL-1.0 |
| Pleos Connect SDK (Vehicle, NaviHelper, TTS, STT) | 차량·내비·음성 | Pleos Playground 이용약관 |

### 필요하면 쓸 만한 것

| 필요 | 라이브러리 | 라이선스 | 메모 |
|---|---|---|---|
| 로컬 DB | [Room](https://developer.android.com/training/data-storage/room) | Apache-2.0 | 민감 데이터면 암호화 필요 (보안 가이드) |
| 설정 저장 | [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) | Apache-2.0 | |
| 암호화 | Android Keystore + [Tink](https://github.com/tink-crypto/tink-java) | Apache-2.0 | AES-GCM, 키는 Keystore |
| 오디오 재생·미디어 세션 | [Media3](https://developer.android.com/media/media3) | Apache-2.0 | Music/Audio 카테고리면 사실상 필수 |
| 네트워크 | [OkHttp](https://github.com/square/okhttp) / [Retrofit](https://github.com/square/retrofit) | Apache-2.0 | TLS 1.2+, 타임아웃·재시도 |
| 이미지 | [Coil](https://github.com/coil-kt/coil) | Apache-2.0 | |
| DI | [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) | Apache-2.0 | 지금 규모에선 수동 주입으로 충분 |
| 로그 | [Timber](https://github.com/JakeWharton/timber) | Apache-2.0 | 개인정보 마스킹 |
| Flow 테스트 | [Turbine](https://github.com/cashapp/turbine) | Apache-2.0 | |
| 목 객체 | [MockK](https://github.com/mockk/mockk) | Apache-2.0 | |
| JVM에서 Android 테스트 | [Robolectric](https://github.com/robolectric/robolectric) | MIT | |
| 코드 스타일 | [ktlint](https://github.com/pinterest/ktlint) / [detekt](https://github.com/detekt/detekt) / [Spotless](https://github.com/diffplug/spotless) | MIT / Apache-2.0 / Apache-2.0 | |

쓰면 안 되거나 주의
- **Google Play 서비스 의존 라이브러리**(Firebase, Google Maps SDK, Fused Location Provider 등): Pleos는 Google 서비스가 없는 AAOS라 동작하지 않을 수 있다.
- 애니메이션 라이브러리(Lottie 등): 주행 중 화면에서는 쓰지 않는다. 주차 화면에서만.
- GPL 계열: 앱 전체 공개 의무가 생길 수 있다. 피한다.

## 5. 공개된 다른 참가팀 저장소 (읽기만)

라이선스가 없고, 같은 공모전 경쟁작이다. **코드·문구·아이디어를 가져오지 않는다** (표절은 심사 제외).
환경 설정이나 SDK 동작에 대한 사실 정보만 참고한다. 주제 분류는 [competitor-map.md](../01-plan/competitor-map.md).

| 저장소 | 참고할 만한 것 |
|---|---|
| [sj-park-kr/appdevchallenge](https://github.com/sj-park-kr/appdevchallenge) | 공모전 가이드 요약, Pleos 문서 원문 수집본(`research/raw/pleos-docs`), SDK API 목록 |
| [WhoisHOO/pleos-app-challenge](https://github.com/WhoisHOO/pleos-app-challenge) | Pleos 에뮬레이터에서 TTS·STT·NaviHelper·Vehicle을 실제로 돌린 기록, 제출 체크리스트 |
| [Mujin8623/chadak-pleos-app](https://github.com/Mujin8623/chadak-pleos-app) | `docs/feasibility.md` — SDK 기능별 가능/불가 판정과 근거 |
| [Univ-JH/pleos-app-2026](https://github.com/Univ-JH/pleos-app-2026) | 공식 샘플 기준 버전 고정(AGP 8.8.2, Gradle 8.10.2) |

## 6. 영상

| 영상 | 메모 |
|---|---|
| [공모전 온라인 설명회 다시보기](https://youtu.be/na6tSJ44pWc) | 반드시 시청 → [briefing-video.md](../00-contest/briefing-video.md) |
| Pleos 25 개발자 컨퍼런스 (2025-03-28, 코엑스) 세션 | 42dot·현대차그룹 채널. 다른 팀 조사 자료에 인용된 세션: https://youtu.be/RWFIEbB3lYA ("Connect SDK는 AAOS와 같은 기능은 제공하지 않으니 AAOS SDK를 쓰라" 부근 19:30) — 직접 확인 필요 |
| 신형 그랜저·Pleos Connect 미디어데이 관련 영상 | 다른 팀 조사 자료에 인용: https://youtu.be/1UCRCG4OVaw (서드파티 앱 확대 목표 언급), https://youtu.be/-cdg0oQfy4U (카테고리 확대 검토) — 직접 확인 필요 |
