# 테스트 · 데모 · 시연 준비

## 1. 세 단계 검증

| 단계 | 무엇을 | 어디서 | 얼마나 자주 |
|---|---|---|---|
| ① 단위 테스트 | `core`의 판단 로직 (주행 정책, 시나리오, 음성 명령, Gleo JSON, 우리 기능 로직) | 누구 PC든, CI | 커밋마다 (`./gradlew :core:test`) |
| ② 데모 모드 | 화면 흐름, 주차↔주행 전환, 문구·글자 크기 | 일반 폰/태블릿 에뮬레이터, Android Studio Preview | 화면 바꿀 때마다 |
| ③ Pleos 에뮬레이터 | 실제 SDK 값, 주행 규제, 음성, 내비, 성능 | GPU 있는 PC | 기능 하나 끝날 때마다, 제출 전, PT 전 |

## 2. 단위 테스트 쓰는 법 (core)

- 위치: `core/src/test/kotlin/...` · JUnit 4 + kotlinx-coroutines-test
- **테스트 함수 이름은 영어**로 쓰고 한국어 설명은 KDoc 주석으로 단다. (한글 함수명은 일부 환경에서 클래스 파일 이름 인코딩 문제로 컴파일이 실패한다)
- 시간 흐름(데모·타이머)은 `runTest` + `advanceTimeBy`로 가상 시간에서 검증 (`DemoScenarioPlayerTest` 참고).
- 예시: 기능 로직 `ChargeAdvisor`를 만들면 경계값(29%, 30%, 31%), 주행/주차, 데이터 없음(null)을 각각 테스트.

```kotlin
/** 배터리 30% 미만이고 주행 중이면 충전 제안 */
@Test
fun suggestsChargingBelowThresholdWhileDriving() { ... }
```

## 3. 데모 모드 (debug 빌드)

GPU가 없어 Pleos 에뮬레이터를 못 띄우는 팀원도 화면을 만들 수 있게 한 장치다.

1. 일반 폰/태블릿 에뮬레이터(가로가 넓은 태블릿 권장)에 debug 빌드 설치
2. 주차 화면 → **데모 시나리오** 카드 → 시나리오 선택
3. 장면이 바뀌면서(주차 → 출발 → 주행 → 정차) 화면이 `DrivingScreen` ↔ `ParkedScreen`으로 전환된다
4. 주행 화면의 **데모 종료**로 멈춘다

- 시나리오 추가: [`DemoScenarios`](../../core/src/main/kotlin/com/springboard/core/demo/DemoScenario.kt)에 `DemoStep` 목록을 추가 (`ALL`에도 등록)
- 데모는 `BuildConfig.DEBUG`일 때만 동작한다 → release(제출본)에는 나타나지 않는다 (App Market 심사: 더미 데이터 금지)
- 실제 차량의 주행 규제가 켜지면 데모보다 실제 값이 우선한다

Android Studio **Preview**: [`ui/Previews.kt`](../../app/src/main/java/com/springboard/app/ui/Previews.kt)에 주차 가로/세로, 주행 화면 미리보기가 있다.

## 4. Pleos 에뮬레이터 점검표 (기능마다)

- [ ] 앱 첫 실행 10초 안에 화면이 뜬다
- [ ] Virtual Console 기어 P → D → P에 따라 주행/주차 화면이 바뀐다 (`adb ... driving_regulation on/off`로도 확인)
- [ ] 우리 기능이 쓰는 차량 값이 Virtual Console 값과 같다
- [ ] 서비스가 응답하지 않을 때 화면이 멈추지 않는다 (시간 초과 표시)
- [ ] 음성: 말하기·듣기 정상, 첫 음절 잘림 없음, 음악 재생 중 안내 시 음악이 낮아졌다 돌아옴
- [ ] 백그라운드 갔다 돌아오면 상태 유지, 최상위 Back은 앱을 숨김(종료 아님)
- [ ] portrait / landscape (폼 팩터 테스트) 잘림·겹침 없음
- [ ] 로그캣에 Exception·ANR 없음: `adb logcat -s DrivingState PleosVehicle PleosNavi PleosTts PleosStt AndroidRuntime`

성능 측정 (기준: 주행 중 CPU 5% 미만, RAM 400MB 이하)

```bash
adb shell top -n 1 | grep com.springboard.app            # CPU
adb shell dumpsys meminfo com.springboard.app | head -30   # 메모리 (TOTAL PSS)
```

## 5. 2차 PT 시연 준비

1. **시연 대본**을 `docs/demo-script.md`로 만든다 (틀: [emulator-and-virtual-console.md](../02-pleos/emulator-and-virtual-console.md) 4절). Virtual Console 조작·말할 내용·예상 화면을 한 줄씩.
2. 대본대로 **3번 연속 성공**할 때까지 고친다.
3. **실패 대비**
   - 음성 인식 실패 → 같은 흐름을 버튼으로도 진행할 수 있게
   - 네트워크 없음 → 시연 데이터는 앱에 내장
   - 에뮬레이터 느림 → 시연 직전 Cold boot, 다른 프로그램 종료, 노트북 전원 연결
   - 최후 수단 → 같은 시나리오 녹화 영상 (사용 가능 여부는 PT 공지 확인)
4. 장비: GPU 노트북(충전기), HDMI 등 출력 젠더, 마우스, 백업 노트북 또는 백업 영상, 인터넷 없는 상황 가정.
5. 발표 시간 안에 "불편한 장면 → 앱이 차 신호로 먼저 반응 → 주행 중 음성 → 정차 후 상세 → 사업성 숫자" 순서가 들어가게.

## 6. 화면 녹화 (시연 영상·숏폼)

```bash
adb exec-out screenrecord --output-format=h264 - > demo.h264    # Ctrl+C 로 종료 (최대 3분)
ffmpeg -framerate 60 -i demo.h264 -c copy demo.mp4
ffmpeg -i demo.mp4 -map_metadata -1 -c copy demo_clean.mp4     # 메타데이터 제거 (블라인드)
```

영상 구성: [demo-video-guide.md](../05-submission/demo-video-guide.md)
