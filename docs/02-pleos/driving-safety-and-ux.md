# 주행 안전 · UX 설계 기준

> 출처: Pleos 문서 "지역별 주행 규제 정책", "주행 규제 개발 가이드", "주행 안전을 고려한 설계", "앱 구조 및 동작 모델",
> "성능 및 리소스 효율", "CCP 개발 가이드", "Pleos Connect UI Design Guide", "App Review Guidelines"(document.pleos.ai, 2026-10),
> Google "Add support for Android Automotive OS to your parked app", Google Design for Driving.
> 차량특화성 심사와 2차 PT 시연에서 **가장 먼저 보이는 부분**이다.

## 1. 먼저 정할 것: 주차 전용 앱인가, 주행 중에도 쓰는 앱인가

| | 주차 전용 앱 (DO 미선언) | 주행 중에도 쓰는 앱 (DO 선언) |
|---|---|---|
| 매니페스트 | `distractionOptimized` meta-data **없음** | 액티비티에 `<meta-data android:name="distractionOptimized" android:value="true"/>` |
| 주행 중 | OS가 앱을 가린다 (Pleos: Parking 사용 가능 / Drive 사용 불가) | 앱이 계속 보인다 → **앱이 CarUxRestrictions를 직접 따라야** 한다 |
| 앱이 할 일 | `onPause`에서 영상·소리 멈추고, 규제 중 재생 재개 금지 | 규제 플래그별로 키보드·영상·설정·긴 글·목록을 줄이거나 막는다 |
| 어울리는 아이디어 | 충전·정차 대기 시간 활용, 상세 기록 조회, 엔터테인먼트 | 출발·도착·주행 이벤트에 반응, 음성 안내, 동승자 케어 |
| 심사 관점 | 안전하지만 "주행 맥락" 어필이 약할 수 있음 | 차량특화성 어필에 유리, 대신 안전 설계를 보여줘야 함 |

**이 저장소의 기본값은 DO 선언**이다. [`SpringboardRoot`](../../app/src/main/java/com/springboard/app/ui/SpringboardRoot.kt)가 규제 중에는 화면 전체를 `DrivingScreen`(큰 글씨·정보 1~2개·음성 우선)으로 바꾼다.
주차 전용으로 가려면 [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml)의 meta-data를 지운다. (실제 App Market 출시 때 DO 허용 범위는 카테고리·심사로 결정 — 설명회·문의로 확인)

## 2. 주행 규제는 어떻게 켜지고 꺼지나

- 플랫폼이 속도·기어·브레이크 등 하드웨어 신호와 **지역별 법규**로 판단한다. 앱은 판단하지 않고 **결과만 받는다**.
- 한국(북미 외 지역) 해제 조건 (Pleos InfoLINK 보충 문서):
  - 시동(IGN1) 또는 구동 준비가 꺼짐, 또는
  - 속도 5 km/h 이하이면서 **기어 P** / **주차 브레이크 ON** / **오토홀드로 정차 유지** 중 하나
  - 그 외(5 km/h 초과 등)에는 규제 ON
- 북미는 더 엄격 (오토홀드를 정차로 보지 않음, P 또는 N+주차 브레이크).
- 앱은 `CarUxRestrictionsManager`에서 `CarUxRestrictions`를 받아 **`isRequiresDistractionOptimization()`**으로 규제 여부를, **`getActiveRestrictions()`** 플래그로 기능별 제한을 판단한다.
  기어나 속도로 규제를 추정하지 않는다 (디스플레이마다 다를 수 있음).
- 테스트: Virtual Console에서 기어 D, 또는 `adb shell cmd hmg_car_service --service drivingstate driving_regulation on`.

## 3. 지역별 기능 허용 (주행 규제 상태, Driving 행)

✅ 사용 가능 · ⛔ 제한 · 음성한정. 괄호는 `CarUxRestrictions` 플래그 값.

| 기능 | 플래그 | 북미 | 유럽 | **한국(그외 지역)** |
|---|---|---|---|---|
| 키보드 | NO_KEYBOARD(8) | ⛔ | ✅ | ✅ |
| 다이얼패드 | NO_DIALPAD(1) | ⛔ | ✅ | ✅ |
| 검색(차량 통합 검색) | NO_KEYBOARD(8), NO_FILTERING(2) | 음성한정 | ✅ | ✅ |
| 미디어 앱 로그인·설정·검색 | NO_SETUP(64), NO_KEYBOARD, NO_FILTERING | ⛔ | ⛔ | ✅ |
| 디지털키·도어·시트·차량 설정 | NO_SETUP(64) | ⛔ | ⛔ | ⛔ |
| 동영상 시청 | NO_VIDEO(16) | ⛔ | ⛔ | ⛔ |
| 내비게이션 검색 | NO_KEYBOARD, NO_FILTERING | ⛔ | ✅ | ✅ |
| QR 로그인 | NO_SETUP(64) | ⛔ | ⛔ | ⛔ |
| 매뉴얼(대용량 텍스트) | LIMIT_CONTENT(32), LIMIT_STRING_LENGTH(4) | ⛔ | ⛔ | ⛔ |

Parking 행은 모든 지역에서 전부 ✅. 플랫폼이 지역에 맞는 플래그를 내려 주므로 **앱은 받은 플래그만 반영**하면 된다.
이 저장소에서는 [`DrivingUiPolicy`](../../core/src/main/kotlin/com/springboard/core/driving/DrivingUiPolicy.kt)가 플래그 → "키보드 허용? 목록 몇 개? 글자 몇 자?"로 바꿔 준다.

## 4. 주의 분산 방지 원칙 (UX)

- **1~2초 안에 파악**: 핵심 정보만. 장식·반복 애니메이션·자동 스크롤·움직이는 이미지 금지(주행 중).
- **한 손, 최소 조작**: 자주 쓰는 기능은 첫 화면 위쪽. 드롭다운·다단 메뉴 남용 금지.
- **입력 최소화**: 키보드보다 프리셋·최근 항목·음성.
- **응답 0.25초**: 입력 후 0.25초 안에 반응. 늦어지면 처리 중 표시. 버튼은 누르면 **95%로 줄어드는** 효과(이 저장소 `CarButton`에 적용).
- **게임·인터넷 서핑·피트니스 등 주의 분산 활동을 유도하지 않는다.** 미디어 앱(영상·게임·브라우저)은 주행 시작 시 일시정지·UI 숨김, 주행 중 재개 불가.
- **알림**: 운전과 관련 있을 때만, 짧게. 알림으로 광고 금지. 미디어 앱은 헤드업 알림 금지.
- **음성 인터랙션 강화**: Gleo·TTS/STT 활용 검토. 대화 시작·종료·중단은 수동으로도 가능해야 한다.

## 5. 숫자로 정리한 디자인 기준

| 항목 | 기준 | 출처 |
|---|---|---|
| 대비 | 아이콘·텍스트·이미지 **4.5:1 이상** | Pleos UI 가이드 |
| 글자 높이 | 주요 정보 **5mm 이상**, 보조 **3mm 이상** | Pleos UI 가이드 |
| 최소 글자 | **12sp** 이상 (실제 픽셀 18px 이상 권장) | Pleos Typography |
| 메시지 길이 | 단일 메시지 **120자(영문) 또는 24단어 이하**, 한 줄 **80자 이하**, 문단 **3줄 이하** | Pleos UI 가이드 |
| 응답 | 입력 후 **0.25초 이내** | Pleos UI 가이드 / 심사 |
| 터치 영역 | **76×76dp 이상**, 간격 **23dp** 권장 | Google Design for Driving |
| 미디어 커스텀 화면 글자 | 주요 32dp, 보조 24dp 이상 | Google 가이드 |
| 폰트 | 시스템 UI는 Asta Sans (한국어·영어) | Pleos Typography |
| 주·야간 | 조명 환경에 따라 밝기·대비 자동 조정 | Pleos UI 가이드 |

이 저장소의 [`Theme.kt`](../../app/src/main/java/com/springboard/app/ui/theme/Theme.kt)는 어두운 배경, 큰 글자, `CarDimens.MinTouchTarget = 76.dp`로 시작한다.

## 6. 화면 모델: Driving view + 앱 창 1~2개

- 화면 = Status bar + **Driving view**(왼쪽 고정, 앱이 가릴 수 없음) + **GNB** + **앱 창**(1개 또는 2개).
- `screenOrientation`을 지정하지 않으면 **portrait**(창 1개), `landscape`로 지정하면 창 2개를 쓴다.
- **portrait와 landscape를 둘 다 설계**한다. portrait = 핵심 기능, landscape = 확장 정보.
- 새 앱은 기존 앱 방향을 따라 뜨고, 화면에서 사라진 앱은 **종료되지 않고 백그라운드**에 남는다 → 다시 앞으로 올 때 **상태 복원** 필수.
- 사용자가 창 위치·크기를 바꾼다(창 바, 3손가락 스와이프) → 반응형 레이아웃.
- 레터박스 없이 화면을 채우고, 해상도가 바뀌어도 상태·기능이 유지돼야 한다(심사 "디스플레이 호환성").
- 디스플레이: Connect-S(17·14.6·12.9인치), Connect-W(와이드, 확장형), Connect-WF(워터폴).
- 폼팩터 확인 방법: [emulator-and-virtual-console.md](emulator-and-virtual-console.md) 3절

## 7. 생명주기·성능 기준

| 항목 | 기준 |
|---|---|
| CPU | 내비 앱 주행 중 15% 이하 / **그 외 앱 주행 중 5% 미만** / 정차 중 25% 이하 |
| RAM | **400MB 이하** |
| 실행 | 10초 이내 실행, 10초 이내 콘텐츠 로드, 버튼은 2초 이내 반응(아니면 진행 표시) |
| 시작 | Cold start는 최소 초기화 + 지연 로딩 |
| 백그라운드 | 화면 갱신·애니메이션·불필요한 센서 수집 중지. 타이머·폴링 최소화 |
| 복귀 | 몇 분 안에 돌아오면 마지막 상태 그대로. 몇 시간 이상이면 기본 상태 가능 |
| 시동 OFF | **Suspend**(저전력) 진입 → 오디오 중단·포커스 반납·상태 저장 ([audio-and-voice.md](audio-and-voice.md)) |

## 8. CCP(물리 다이얼·버튼)와 Back 키 — 필수

- 사용자 설치 앱은 CCP(로터리) 지원이 **선택**. 미지원이면 매니페스트에 `com.hmg.rotary.ROTARY_SUPPORTED=false`를 선언하고 **모든 필수 조작을 터치로** 끝낼 수 있어야 한다 (이 저장소는 미지원 선언).
- **최상위 화면에서 Back 키 → 앱을 종료하지 말고 숨긴다**: `moveTaskToBack(true)`. `finish()`로 끝내면 심사 반려 대상.
  하위 화면·팝업에서는 원래대로 이전 화면/팝업 닫기. 이 저장소의 [`MainActivity`](../../app/src/main/java/com/springboard/app/MainActivity.kt)에 구현.

## 9. 키보드(IME)가 뜰 때

- 입력창이 있는 창은 IME가 올라오면 화면이 위로 밀리는 게 사양.
- 입력창이 없는 창(다이얼로그 등)은 IME에 밀리지 않게 `setFitInsetsTypes(statusBars | navigationBars)`로 IME insets를 제외.

## 10. 카테고리별 주행 중 허용 예시

- **Music/Audio**: 앨범 아트 등 단순 시각 정보, 재생/일시정지/곡 넘김, 라디오 채널 변경. MediaBrowserService·MediaSession 필수, 스티어링 휠 키 지원 필수, 자동 재생 금지.
- **Maps/Navigation**: 경로 안내, 간결한 지도, TBT 음성. **하나의 내비만 Active Route** (다른 앱의 길 안내 음성 금지).
- **Mobility Services**: 주행과 밀접한 차량 관리, 현재 위치 기반 즉시 정보, 충전소·주유소·주차장 찾기와 경유지 설정.

## 11. 체크리스트 (PR마다)

- [ ] 주행 화면에 정보가 1~2개뿐이고 1~2초 안에 읽힌다
- [ ] 주행 중 키보드·긴 글·영상·자동 애니메이션이 없다
- [ ] 모든 버튼이 76dp 이상, 대비 4.5:1 이상
- [ ] portrait / landscape 둘 다 깨지지 않는다
- [ ] 백그라운드 갔다 와도 상태가 그대로다
- [ ] 최상위 Back에서 앱이 종료되지 않고 숨겨진다
