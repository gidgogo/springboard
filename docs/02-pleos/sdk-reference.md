# Pleos Connect SDK 레퍼런스 (요약)

> 출처: Pleos SDK 릴리스 노트(2026-10-19), Pleos 공식 샘플 3종(github.com/Pleos-Playground), Playground 콘솔 SDK 탭의 API 목록,
> 공식 문서 "시작하기·FAQ·주행 규제 개발 가이드". 이 저장소의 CI가 아래 버전 조합으로 실제 빌드되는 것을 확인했다(2026-11-05).
> **API 이름·인자는 공식 API 레퍼런스(document.pleos.ai)가 최종 기준**이다. 여기 없는 API를 쓰기 전에 레퍼런스를 확인한다.

## 1. 버전 · 좌표 · 권한 · 상태

Maven 저장소 (토큰 불필요): `https://nexus-playground.pleos.ai/repository/maven-releases/`

| SDK | 좌표 | 버전 | 권한 | 콘솔 상태 | 이 프로젝트 |
|---|---|---|---|---|---|
| Vehicle | `ai.pleos.playground:Vehicle` | 2.3.2 | `pleos.car.permission.CAR_*` (조회), `CONTROL_CAR_*` (제어) — normal | 조회 ✅ / 제어 ⏳ | 사용 |
| NaviHelper | `ai.pleos.playground:NaviHelper` | 3.0.5 | `pleos.car.permission.NAVI_ROUTE`, `NAVI_ROUTE_SEARCH` (+ 샘플: `NAVI_CUSTOM_MAP`, `NAVI_CUSTOM_ROUTE`, `NAVI_CUSTOM_ETC`) | ✅ | 사용 |
| TextToSpeech | `ai.pleos.playground:TextToSpeech` | 3.3.1 | `pleos.car.permission.TTS_SERVICE` | ⏳ (온디바이스는 동작) | 사용 |
| SpeechToText | `ai.pleos.playground:SpeechToText` | 3.3.1 | `pleos.car.permission.STT_SERVICE` | ⏳ (온디바이스는 동작) | 사용 |
| FusedLocation | `ai.pleos.playground:FusedLocation` | 1.1.3 | `pleos.car.permission.FUSED_LOCATION` (런타임 요청) | ⏳ | 카탈로그에만 등록 |

- 버전은 [`gradle/libs.versions.toml`](../../gradle/libs.versions.toml) 한곳에서 바꾼다.
- 권한은 **쓰는 것만** 선언한다(보안 가이드). App Market 출시 때 Vehicle SDK 권한은 사전 협의·승인 대상이다.
- 온디바이스 TTS/STT: 공개 참가팀들의 기록에 따르면 Pleos 에뮬레이터에서 신청 없이 동작했다. 직접 확인할 것.

## 2. 공통 사용 패턴

```kotlin
val vehicle = Vehicle(context.applicationContext)
vehicle.initialize()                       // 서비스 연결. 끝나기 전 호출은 응답이 늦거나 안 올 수 있다
val door = vehicle.getDoor()               // 도메인 객체
door.checkDoorPositionCapabilities(        // 1) 지원 여부·접근 방식·값 범위 확인 (차종마다 다름)
    onComplete = { config, areaBounds -> /* config.accessType, areaBounds[area]?.minValue..maxValue */ },
    onFailed = { e -> },
)
door.getCurrentDoorPosition(               // 2) 읽기 — 비동기 콜백
    area = DoorArea(DoorValue.ROW_1_LEFT),
    onComplete = { pos: Int? -> },
    onFailed = { e: Exception -> },
)
door.registerDoorPosition(area, listener)  // 3) 변경 구독 — 같은 listener 인스턴스로 해제
door.unregisterDoorPosition(area, listener)
vehicle.release()                          // 4) 더 이상 안 쓸 때 (onDestroy 등)
```

규칙
- 콜백은 비동기다. 다음 단계는 이전 콜백 안에서(또는 코루틴으로) 이어서 부른다.
- **리스너는 등록한 Vehicle 세션보다 오래 살면 안 된다.** release 전에 해제.
- 값 범위를 가정하지 않는다. capability가 주는 범위(예: 도어 0..1, 창문 0..10)를 쓴다.
- 이 프로젝트의 래퍼: [`PleosVehicleClient`](../../app/src/main/java/com/springboard/app/platform/vehicle/PleosVehicleClient.kt) — 호출마다 3초 제한, 실패·시간초과·서비스 없음 구분.

## 3. Vehicle SDK (조회)

공식 샘플은 19개 도메인을 다룬다: Brake, Car info, Display, Door, Driving mode, EV battery, Fuel, HVAC, Light, Odometer, Safety, Seat, Side mirror, Steering wheel, Tire, Toll, Turn signal, Window, Wiper.
아래는 콘솔 권한 그룹별 대표 조회 API다 (`get…`은 현재 값, 대부분 `register…`/`unregister…` 구독 API와 `check…Capabilities`가 짝으로 있다).

| 권한 그룹 | 대표 API | 쓸모 |
|---|---|---|
| `CAR_INFO` | `getVINInfo`, `getCurrentTemperatureDisplayUnit` | 차량 식별(개인정보 주의), 단위 |
| `CAR_DRIVE` | `getCurrentDistanceDriven`(누적 주행거리 km), `getTripCurrentDrivingDistance` / `getTripCurrentDrivingTime`, `getTripCumulative…`, `getTripSinceRefill…`, `getCurrentDrivingModeState`(에코·스포츠 — **기어 아님**), `getCruiseControlState` / `getCruiseControlTargetSpeed`, `getCurrentBrakePedalStatus`, `getCurrentSteeringWheelAngle` | 운행 기록, 주행 맥락 |
| `CAR_DOORS` | `getCurrentDoorPosition`, `getDoorLockState`, `getCurrentGloveBoxLockState` | 승하차 감지 |
| `CAR_ENERGY` | `getEvBatteryStateOfCharge`(SOC), `getFuelPercent`, `get…EvEfficiency`(누적·현재·순간·충전 후 전비), `get…FuelEconomy`, `getEvChargeSwitchState`, `getEvChargeCurrentDrawLimit` | 충전·연비 |
| `CAR_CLIMATE` | `getHvacPowerState`, `getHvacAutoState`, `getHvacAcState`, `getHvacTemperature`(설정), `getHvacTemperatureCurrent`(현재 °C), `getHvacFanSpeed`, `getHvacFanDirection`, `getHvacRecirculationState`, `getHvacAirCleanerState`, `getHvacSteeringWheelHeatState` … | 쾌적·건강 |
| `CAR_LIGHTS` | `getCurrentTurnSignalState`(방향지시등), 앰비언트 조명(색·밝기, 조회만), 실내등·독서등, 전조등·안개등·상향등·미등 상태 | 주행·야간 맥락 |
| `CAR_SAFETY` | `getEngineOilLevelState`, `getBrakeFluidLevelState`, `getTransmissionOilLevelState`, `hasLowWasherFluidWarning`, `hasPowerDownWarningIndicator`, `hasSeatBeltNotBuckledWarning`, `hasSeatAirbagEnabled` / `hasSeatAirbagBeenDeployed`, `getLowTirePressure`(저압 경고 여부만) | 정비·안전 |
| `CAR_SEATS` | `getCurrentSeatOccupancy`(탑승), `getCurrentSeatBeltBuckledState`, 시트 위치·머리받침·허리받침, `getHvacSeatTemperature`, `getHvacSeatVentilationState` | 탑승자 맞춤 |
| `CAR_WINDOWS` | `getCurrentWindowPosition`, `getCurrentWindowLockState`, `getCurrentWiperState` / `getCurrentWiperSwitch`, 스마트글래스·블라인드 | 날씨 맥락 |
| `CAR_MIRRORS` | `getHvacSideMirrorHeatState` | |
| `CAR_TOLL` | `getCurrentEtcsVoiceGuidanceVolume`(하이패스 안내 음량만 — **통행 기록·요금 없음**) | |

샘플 매니페스트에 있는 조회 권한 전체: `CAR_CLIMATE, CAR_CONNECT_VENDOR_CONTROL, CAR_DOORS, CAR_DRIVE, CAR_ENERGY, CAR_INFO, CAR_LIGHTS, CAR_MILEAGE, CAR_MIRRORS, CAR_POWERTRAIN, CAR_SAFETY, CAR_SEATS, CAR_SPEED, CAR_TOLL, CAR_TIRES, CAR_WINDOWS, READ_CAR_DISPLAY_UNITS, READ_CAR_STEERING, READ_ADAS_STATES, READ_WINDSHIELD_WIPERS` (모두 `pleos.car.permission.` 접두사). API별 정확한 권한은 레퍼런스에서 확인.

### Vehicle Control (제어) — ⏳ 준비 중

도어·창문·와이퍼·조명·공조·시트·충전 설정 등 `set…`/`move…` 47개. 상당수는 **주행 중 호출 제한**이 걸린다.
지금은 핵심 기능으로 삼지 말고, 기획서에는 "제어 SDK가 열리면 추가" 로드맵으로만 쓴다.

### 없는 것 / 바뀐 것 (설계 전에 꼭 확인)

| 필요 | 상황 | 대안 |
|---|---|---|
| 기어(P/R/N/D) | Vehicle SDK 공개 API에 없음 (숨김·deprecated 상태) | AAOS `GEAR_SELECTION` (권한 `android.car.permission.CAR_POWERTRAIN`) |
| 차량 속도 | `VehicleSpeedListener`가 **2.3.2에서 삭제** | AAOS `PERF_VEHICLE_SPEED` (권한 `android.car.permission.CAR_SPEED`, dangerous) — 클러스터와 같은 값 (Pleos FAQ) |
| 경고등(Telltale)·타이어 공기압 값 | 비공개 권한(`PRIVATE_CAR_SAFETY`) | `getLowTirePressure`(경고 여부만), 기타 Safety 조회 |
| 주행 규제 상태 | SDK 아님 | AAOS `CarUxRestrictionsManager` ([driving-safety-and-ux.md](driving-safety-and-ux.md)) |
| 내비 탑재 여부 확인 | 공개 안 됨 | partnership@pleos.ai 문의 |

### 2.3.2 주요 변경 (예전 예제 코드를 볼 때 주의)

- `CruiseControl` 상태 상수 변경 (OFF/READY/ACTIVE 제거 → OTHER, ENABLED, ACTIVATED, USER_OVERRIDE, SUSPENDED, FORCED_DEACTIVATION_WARNING)
- `checkHvacFanSpeedCapabilities` 콜백 값 타입: `List<Int>` → `AreaBounds<Int>`
- `FanDirectionType` 3개 → 8개로 확장, 순서 변경 (ordinal 저장 시 주의)
- `DoorArea` 등 Area 클래스가 내용 기반 `equals`로 변경
- 새 API: `getVINInfo`, `getCurrentEvEfficiency`, `registerEvBatteryStateOfCharge`, 스마트글래스 등 / 새 모델 `CapabilityConfig`, `TripStatistics`, `TelltaleData`
- deprecated: `getCRNInfo` → `getVINInfo`
- 삭제: `getHvacTemperatureValueSuggestion`, `VehicleSpeedListener`

## 4. NaviHelper SDK 3.0.5

차량의 **기본 내비 앱**에 요청을 보내고, 결과는 `NaviHelperEventListener` 콜백으로 받는다 (`get…`은 반환값이 아니라 콜백 `on…`으로 온다).

```kotlin
val helper = NaviHelper(context)
helper.initialize()                 // 3.0.5: initialize(onConnected, onFailed) 오버로드도 추가됨
helper.addListener(listener)
helper.getCurrentLocationInfo()     // → listener.onCurrentLocationInfo(CurrentLocationInfo)
helper.removeListener(listener); helper.release()
```

| 종류 | API | 콜백 |
|---|---|---|
| 조회 | `getCurrentLocationInfo`, `getDestinationInfo`(남은 거리·시간, 예상 통행료), `getWaypointInfo`, `getRouteStateInfo`, `getTBTInfo`, `getRecentDestinationInfo`, `getBookmarkInfo`, `getChargerOperatorInfo` | `onCurrentLocationInfo`, `onDestinationInfo`, `onWayPointInfo`, `onRouteStateInfo`, `onTBTInfo`, `onRecentDestinationInfo`, `onBookmarkInfo`, `onChargerOperatorInfo` |
| 경로 | `requestRoute(RouteInfo)`, `requestRouteWithWaypoints(RouteWithWaypointsRequest)`, `cancelRoute`, `requestReRoute`, `changeRouteOption(RouteOption)`, `addWaypoint(RequestWaypointInfo)`, `removeWaypoint(WaypointIndex)` | `onRouteStarted`, `onRoutingCompleted`, `onReRouteEnded`, `onRouteCancelled`, `onRouteOptionChanged`, `onWaypointChanged` |
| 화면·트립 | `requestNavigationScreen(NavigationScreenRequest)`, `requestTrip(TripRequest)` | `onTripInfo` |
| 주행 이벤트 | — | `onDestinationArrived`, `onWayPointArrived`, `onCurrentRoadSpeedLimit`, `onCameraAlert`, `onCautionPointInfo`, `onDrivingInfo`, `onNavigationStatus`, `onDestinationChanged`, `onBookMarkChanged`, `onError(NaviErrorCode)` |

주의
- **3.0.5 좌표 인자 순서가 (latitude, longitude)로 바뀌었다.** 예전 예제의 (longitude, latitude)를 그대로 쓰면 엉뚱한 곳으로 간다.
- `DestinationInfo`/`WaypointInfo`의 `totalDistance/totalTime` → `remainDistance/remainTime`.
- `onRouteEnded`는 deprecated → `onRoutingCompleted`.
- 경유지는 `WaypointIndex.FIRST/SECOND` (최대 2개로 보임).
- **POI(장소) 검색 API는 없다.** 목적지 좌표·POI 정보는 앱이 가지고 있어야 한다 (공공 API 등).
- 경로 요청 예시 데이터: 공식 샘플 `NaviTestData.kt` (`RouteInfo(latitude, longitude, poiId, poiName, poiSubId, address)`).
- Pleos 정책: **하나의 내비 앱만 경로 안내(Active Route)** 가능 — 우리 앱이 따로 길 안내 음성을 내지 않는다.

## 5. TextToSpeech / SpeechToText 3.3.1

```kotlin
// TTS
val tts = TextToSpeech(context, Mode.ON_DEVICE)        // ai.pleos.playground.tts.*
tts.initialize(object : OnInitializeListener {
    override fun onSuccess() { tts.addEventListener(listener); tts.setLocale(Locale.KOREA) }
    override fun onFailure(errorType: ErrorType) { }
})
tts.speak("안내 문장", true)    // EventListener: onReady, onStart, onDone, onStop, onUpdatedRms, onError(ErrorType)
tts.stop(); tts.release()

// STT
val stt = SpeechToText(context, Mode.ON_DEVICE)        // ai.pleos.playground.stt.*
stt.addListener(resultListener)  // onReady, onStartedRecognition, onUpdatedRms, onUpdatedEpdData,
                                 // onUpdated(stt: String, completed: Boolean), onEndedRecognition, onError(ErrorType)
stt.initialize(onInitializeListener); stt.setLocale(Locale.KOREA)
stt.request()                    // 마이크로 듣기 시작 (차량 음성 서비스가 마이크를 연다 → RECORD_AUDIO 불필요)
stt.sendAudio(pcm); stt.completeAudioSend()   // 녹음 데이터(16kHz 16bit mono PCM)로 테스트할 때
stt.stop(); stt.release()
```

- 3.3.1 변경: 오류 콜백 인자 `String` → `ErrorType`, `initialize(listener)` 결과 콜백, `version` 속성, STT `InferenceSource`(ON_DEVICE, SERVER).
- STT는 받아쓰기만 한다 → 의미 해석은 앱이 직접 ([`VoiceCommandMatcher`](../../core/src/main/kotlin/com/springboard/core/voice/VoiceCommandMatcher.kt)).
- 이벤트에 발화 ID가 없다 → 이전 발화의 onDone/onStop이 늦게 올 수 있음을 감안.
- 에뮬레이터에서 처음 마이크가 열리기까지 수 초 걸릴 수 있다.
- 음성 안내 앞 **150~200ms 선행 묵음** 권장, 오디오 포커스 정책 준수 → [audio-and-voice.md](audio-and-voice.md)
- 이 프로젝트의 래퍼: [`PleosTextToSpeech`](../../app/src/main/java/com/springboard/app/platform/speech/PleosTextToSpeech.kt), [`PleosSpeechToText`](../../app/src/main/java/com/springboard/app/platform/speech/PleosSpeechToText.kt)

## 6. FusedLocation 1.1.3 (필요할 때만)

- API: `isSupported()`(먼저 확인), `initialize()`, `registerFusedLocationCallback(FusedLocationListener)` / `unregister…`, `getLastKnownLocation`, `addProximityAlert` / `removeProximityAlert`(지오펜스 ENTER/EXIT), `showLocationAccessBlockedPopup`, `release()`
- `FusedLocationListener.onChanged(LocationData?)`, `onError(Exception)` (1.1.3에서 필수)
- 권한 `pleos.car.permission.FUSED_LOCATION`은 런타임 요청. 추가하면 매니페스트 `uses-feature automotive`에 `tools:replace="android:required"` 필요
- 에뮬레이터 위치 주기: `adb emu geo fix <경도> <위도>` (경도가 먼저)
- 공식 샘플: https://github.com/Pleos-Playground/pleos-sample-fusedlocation-app

## 7. AAOS 표준 Car API (Pleos가 "표준 우선"으로 권장)

`build.gradle.kts`에 `useLibrary("android.car")`, 매니페스트에 `<uses-library android:name="android.car" android:required="false"/>`.

| 무엇 | API | 권한 |
|---|---|---|
| 주행 규제 | `Car.createCar(...)` → `CarUxRestrictionsManager.getCurrentCarUxRestrictions()` / `registerListener` | 없음 |
| 기어 | `CarPropertyManager` + `VehiclePropertyIds.GEAR_SELECTION` (`VehicleGear.GEAR_PARK` …) | `android.car.permission.CAR_POWERTRAIN` (normal) |
| 속도 | `VehiclePropertyIds.PERF_VEHICLE_SPEED` (m/s, float) | `android.car.permission.CAR_SPEED` (**dangerous** → 런타임 요청) |
| 주차 브레이크, 시동 상태 | `PARKING_BRAKE_ON`, `IGNITION_STATE` | `CAR_POWERTRAIN` |
| 야간 모드, 외기 온도 | `NIGHT_MODE`, `ENV_OUTSIDE_TEMPERATURE` | `android.car.permission.CAR_EXTERIOR_ENVIRONMENT` |

- 지원 여부는 차종마다 다르다 → `CarPropertyManager.getCarPropertyConfig(id)` 등으로 확인. 모델명 분기 금지.
- Pleos Connect는 Android 14(API 34)다. API 35에 생긴 `subscribePropertyEvents`가 없으므로 **`registerCallback`(deprecated)을 쓴다.**
- 이 프로젝트의 구현: [`CarDrivingStateMonitor`](../../app/src/main/java/com/springboard/app/platform/car/CarDrivingStateMonitor.kt)
- 레퍼런스: https://developer.android.com/reference/android/car/VehiclePropertyIds , https://developer.android.com/reference/android/car/hardware/property/CarPropertyManager

## 8. 공개 프로젝트들에서 배운 실전 팁

- `Car.createCar(context, null, CAR_WAIT_TIMEOUT_DO_NOT_WAIT) { car, ready -> }` 형태로 연결하면 서비스 재시작에도 다시 붙는다.
- `CarPropertyManager.registerCallback`은 설정을 받아오느라 오래 걸릴 수 있어 **메인 스레드 밖에서** 부른다 (에뮬레이터 ANR 사례).
- Vehicle SDK 호출은 서비스 연결 전이면 **응답이 영영 안 올 수 있다** → 호출마다 시간 제한.
- Vehicle SDK의 일부 하위 아티팩트가 Hilt(Dagger)를 의존성으로 선언한다. Hilt를 쓰지 않으면 `exclude(group = "com.google.dagger")`로 APK를 줄일 수 있다(적용 후 에뮬레이터 재확인 필수).
- NaviHelper는 기본 내비 앱이 있어야 응답한다. 응답이 없으면 내비 앱 상태부터 본다.
