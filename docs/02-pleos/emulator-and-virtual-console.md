# 에뮬레이터 · Virtual Console로 시연하기

> 출처: Pleos 문서 "Virtual Console 셋업/사용 가이드", "폼 팩터 테스트 가이드", "ADB 명령 활용법", "Emulator 정식 릴리즈 v2.1.0(2026-10-19)".
> 2차 PT는 **실제로 동작하는 앱 시연 필수**다. 시연 장면은 전부 Virtual Console·adb로 재현할 수 있어야 한다.

## 1. Virtual Console 카드 ↔ 앱에서 읽는 API

접속: `adb forward tcp:8088 tcp:8088` → 브라우저 `http://127.0.0.1:8088/` → Emulation Controls

| 도메인 | 카드 | 설정 범위 | 앱에서 읽는 API (예) |
|---|---|---|---|
| Vehicle | Notification 1 | 표시/숨김 | (차량 알림 표시 상태) |
| **Motion** | **Gear Selection** | P, R, N, D | AAOS `GEAR_SELECTION` → 주행 규제 변화도 함께 확인 |
| Motion | Brake Pedal | 0~100% | `getCurrentBrakePedalStatus` |
| Motion | **Odometer** | 0~1,000,000 km (1km 단위) | `getCurrentDistanceDriven` |
| Motion | Steering Angle | -540~540° | `getCurrentSteeringWheelAngle` |
| Motion | Steering Wheel Heat | Off/Low/Medium/High | `getHvacSteeringWheelHeatState` |
| ADAS | Cruise Control State | Not ready / Ready / Longitudinal / Lateral / Full autonomous | `getCruiseControlState` |
| ADAS | Cruise Target Speed | 5.556~27.778 m/s | `getCruiseControlTargetSpeed` |
| Convenience | ETCS Power / ETCS Scenario | Off/On, 카드 삽입·제거, 요금소 진입·진출 | (하이패스 — 앱에서 읽는 API 확인 필요) |
| Convenience | **Door Position** | 앞·뒤 좌우 열림/닫힘 | `getCurrentDoorPosition` |
| Convenience | Door Lock | Unlock/Lock | `getDoorLockState` |
| Convenience | Trunk / Frunk Position | 열림/닫힘 | (도어 영역) |
| Convenience | Cabin Lights, Reading Lights | Off/On | 실내등·독서등 상태 |
| Convenience | Ambient Brightness / Color | 0~100%, Cyan/Blue/Purple/White | 앰비언트 조명 조회 |
| Convenience | Exterior Lights Mode, High Beam, Hazard, Stop Lights, Door Lights | 각 상태 | 외부등 상태 |
| Convenience | **Turn Signal State** | Off, 좌·우 Blink/Flash | `getCurrentTurnSignalState` |
| Convenience | Window Position / Window Lock | 0~100%, Lock | `getCurrentWindowPosition`, `getCurrentWindowLockState` |
| Convenience | **Seat Occupancy** | 운전석·동승석·뒷좌석 좌우 Empty/Occupied | `getCurrentSeatOccupancy` |
| Convenience | Windshield Wiper Switch | Off/Auto/Low/High | `getCurrentWiperSwitch` |
| Convenience | Glove Box | 열림/닫힘 | `getCurrentGloveBoxLockState` |
| Safety | Telltales | 주차브레이크·안전벨트·ABS·TPMS 등 Off/On/Blink | ⚠️ 앱에서 Telltale을 읽는 공개 API 없음 |
| **Energy** | Remaining Range | 0~1,000,000 m | (주행 가능 거리) |
| Energy | Battery Energy / Capacity | 0~200 kWh | |
| Energy | **Battery SOC** | 0~100% | `getEvBatteryStateOfCharge` |
| Energy | Charge Current Limit / Percent Limit / Switch / Port | 각 범위 | `getEvChargeSwitchState` 등 |
| **Climate** | HVAC Power, A/C, Auto, Dual, Recirculation, Fan Speed(0~8), Fan Direction, Defroster, Target Temperature(16~30℃), Seat Temperature, Air Cleaner | 각 범위 | `getHvac…` |

**문서상 카드가 없는 것**: 차량 속도, 엔진오일·브레이크액·워셔액 수준, 연료량, 위치(GPS). → 속도는 데모 시나리오나 화면 문구로 보완, 위치는 `adb emu geo fix`.

## 2. 자주 쓰는 adb 명령

```bash
# 연결 확인 / Virtual Console
adb devices
adb forward tcp:8088 tcp:8088

# 앱 설치·실행·종료·삭제
adb install -d -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.springboard.app -c android.intent.category.LAUNCHER 1
adb shell am force-stop com.springboard.app
adb uninstall com.springboard.app

# 주행 규제 켜기/끄기 (모든 차종 공통) — 기어를 안 바꾸고 주행 화면을 확인할 때
adb shell cmd hmg_car_service --service drivingstate driving_regulation on
adb shell cmd hmg_car_service --service drivingstate driving_regulation off

# 위치 (에뮬레이터만, 경도 먼저)
adb emu geo fix 127.0276 37.4979

# 화면 캡처·녹화 (시연 영상용)
adb exec-out screencap -p > screen_$(date +%Y%m%d_%H%M%S).png
adb exec-out screenrecord --output-format=h264 - > screen.h264      # Ctrl+C로 종료
ffmpeg -framerate 60 -i screen.h264 -c copy screen.mp4

# 텍스트 입력 (키보드 대신)
adb shell input text "test"

# 로그 보기 (이 프로젝트의 태그)
adb logcat -s DrivingState PleosVehicle PleosNavi PleosTts PleosStt Gleo
```

## 3. 폼 팩터 테스트 (portrait / landscape, 화면 크기별)

Pleos 앱은 창 1개(portrait) 또는 2개(landscape)로 뜨고, 차종마다 디스플레이 크기가 다르다. 제출 전 한 번은 확인한다.

준비: Android SDK Platform-Tools, **Go 1.22 이상**, 8095·8096 포트, 에뮬레이터를 **별도 창**으로 실행(Android Studio 내장 창·headless 불가).

1. `adb emu avd name`으로 AVD 이름 확인
2. Pleos 문서의 `host-display-bridge.zip`을 받아 압축 해제 → 그 폴더에서 `go run . --avd "<AVD 이름>"`
3. 에뮬레이터에서 **FormFactorConsole** 앱 실행 → `DISCONNECTED`·"DISPLAY2가 없습니다"가 보이면 준비 완료
4. Display preset(예: 12.9인치, 14.6인치) · Orientation(Portrait/Landscape) 선택 → dp·DPI·px 확인 → 가상 디스플레이 생성
5. `DISPLAY2` 창에서 앱을 실행해 확인: 잘림·겹침, 글자·아이콘 크기, 가로/세로 배치, 터치·화면 전환
6. 끝나면 가상 디스플레이 제거 → Bridge 종료(Ctrl+C) → `adb forward --remove tcp:8095`

## 4. 시연 시나리오 대본 만들기 (예시 틀)

2차 PT에서는 **같은 순서를 매번 똑같이** 재현할 수 있어야 한다. 아래 틀로 대본을 만들어 `docs/demo-script.md`에 둔다.

| # | Virtual Console / adb 조작 | 앱에서 보여줄 것 | 말할 것 (발표자) |
|---|---|---|---|
| 1 | Gear **P**, Seat Occupancy: 운전석·뒷좌석 Occupied | 주차 화면, 탑승자 인식 | "출발 전…" |
| 2 | Gear **D** | 자동으로 주행 화면 전환, 음성 안내 | "주행이 시작되면 화면 대신 음성으로…" |
| 3 | Battery SOC 30% / Turn Signal / Door 등 이벤트 | 이벤트에 맞는 음성 안내 | "차가 먼저 알려 줍니다" |
| 4 | (STT) "응" 대답 | 다음 동작 수행 | |
| 5 | Gear **P** | 상세 화면 | "정차 후에는 자세히…" |

실패 대비
- 에뮬레이터가 느려지면: 시연 직전 Cold boot, 다른 프로그램 종료
- 마이크가 안 되면: 버튼 탭으로 같은 흐름이 진행되게 대체 경로를 둔다
- 최후의 수단: 같은 시나리오 녹화 영상 (PT 규정 확인)

## 5. 데모 모드 (GPU 없는 팀원용)

debug 빌드의 주차 화면 "데모 시나리오" 카드에서 시나리오를 재생하면, 실제 차량 없이도 주차→주행→정차 상태가 흘러가고
화면이 자동으로 바뀐다. 시나리오는 [`DemoScenarios`](../../core/src/main/kotlin/com/springboard/core/demo/DemoScenario.kt)에 추가한다.
실제 차량(에뮬레이터)의 주행 규제가 켜지면 데모보다 실제 값이 우선한다.
