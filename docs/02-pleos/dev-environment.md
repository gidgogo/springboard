# 개발 환경 만들기

> 출처: Pleos 문서 "Pleos Connect 앱 개발 시작하기", "Pleos Connect Emulator 설치 및 실행", "Pleos Connect Emulator 설정",
> "Virtual Console 셋업 가이드" (document.pleos.ai, 2026-10-19 개편본) · Pleos 공식 샘플 README.

## 0. 먼저 확인: 노트북 사양 (중요)

Pleos Connect Emulator는 **외장 그래픽카드가 필수**입니다.

| 구분 | 사양 |
|---|---|
| 권장 | NVIDIA RTX 4060/5060 Laptop 8GB 이상, 또는 AMD Radeon RX 7700S / RX 7600M XT 8GB급 |
| 최소 | NVIDIA GeForce RTX 3060 Laptop 6GB 이상, 또는 AMD Radeon RX 6600M 8GB / RX 6700S 8GB |
| Mac | Apple Silicon용 별도 이미지 주소 제공(arm64) — 실제 성능은 직접 확인 |

- 두 사람 중 GPU가 있는 사람이 에뮬레이터 담당. 없으면 **오늘 확보 방법을 찾는다** (2차 PT는 실제 동작 시연 필수).
- GPU가 없는 사람은 일반 폰/태블릿 에뮬레이터에서 **데모 모드**로 화면을 개발한다 ([testing-and-demo.md](../04-dev/testing-and-demo.md)).

## 1. 소프트웨어 요구사항

| 항목 | 값 | 비고 |
|---|---|---|
| Android Studio | 최신 안정판 | Automotive 템플릿·Device Manager 사용 |
| JDK | 17 (Android Studio 내장 JBR 사용 가능) | 이 프로젝트는 Java 17 타깃 |
| Android SDK | Platform 35 (compileSdk), 에뮬레이터 이미지는 API 34 | |
| minSdk | **28 이상 필수** | 차량 사이버보안 요건(서명 키 로테이션·리니지) |
| Gradle | 8.0 이상 (이 프로젝트: 래퍼 8.10.2, AGP 8.8.2) | |
| 언어 | Kotlin 또는 Java (이 프로젝트: Kotlin + Jetpack Compose) | |
| Pleos Connect Emulator | **v2.1.0** (Virtual Console 지원 최소 버전) | |

## 2. Pleos Connect Emulator 설치

> 이전 버전이 있으면 먼저 지운다.

1. Android Studio → **Tools > SDK Manager > SDK Update Sites** → `+`
2. 이름 `Pleos Connect System Image`, URL은 운영체제에 맞게:
   - Windows / Linux: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml`
   - Mac: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml`
3. **SDK Platforms** 탭 → `Show Package Details` 체크 → **Android API 34의 Pleos Connect system image** 체크 → OK → 설치 → **Android Studio 재시작**

## 3. AVD 만들기

1. **Tools > Device Manager > Create device**
2. Form Factor **Automotive** → **Pleos Connect v2.1.0** → Next
3. **Advanced Settings**
   - Startup > Default boot: **Cold** (Quick boot면 VHAL을 못 씀 — 차량 데이터가 안 나옴)
   - Emulated Performance > RAM: **4 GB**
   - (구버전 Studio) Graphics: **Hardware**
4. System image: API 34 → **Pleos Connect v2.1.0 APIs** → Finish
5. Device Manager에서 실행

에뮬레이터 설정
- 기본 언어는 한국어. 영어: 차량 설정 > 일반 설정 > 언어/Language
- **마이크(STT 테스트)**: 에뮬레이터 패널 `...`(Extended Controls) > **Microphone** > 호스트 오디오 입력 허용
- Bluetooth 동글 연동은 지원하지 않음

## 4. Virtual Console (차량 상태 조작)

```bash
adb devices                      # emulator-5554 device 가 보여야 함
adb forward tcp:8088 tcp:8088    # 에뮬레이터 8088 포트를 PC로
```

PC 브라우저에서 `http://127.0.0.1:8088/` → Emulation Controls → 도메인별 카드로 기어·도어·배터리 등을 바꾼다.
카드 목록과 API 대응: [emulator-and-virtual-console.md](emulator-and-virtual-console.md)

안 될 때: 에뮬레이터 실행 여부 → v2.1.0 이미지인지 → `adb devices` → forward 재실행 → 8088 포트 충돌 → 브라우저 재접속

## 5. 이 프로젝트 열기

```bash
git clone https://github.com/gidgogo/springboard.git
cd springboard
```

1. Android Studio → Open → `springboard` 폴더 → Gradle Sync
   - Android SDK 경로를 못 찾으면 루트에 `local.properties` 생성: `sdk.dir=/경로/Android/sdk` (커밋 금지, .gitignore 처리됨)
   - Pleos SDK는 `https://nexus-playground.pleos.ai/repository/maven-releases/`에서 받는다 (토큰 불필요). 회사·학교 방화벽이 막으면 Sync가 실패한다.
2. 실행 대상에 Pleos 에뮬레이터를 고르고 ▶ Run (`app` 구성)
3. 앱 첫 화면(점검 화면)에서 확인
   - "AAOS 기기", "Pleos 차량 서비스 있음"이 초록(강조)인지
   - **차량 값 읽기** → 누적 주행거리 값이 나오는지 (Virtual Console Odometer 카드 값과 비교)
   - Virtual Console에서 기어를 **D**로 → 화면이 **주행 중** 화면으로 바뀌는지

명령줄로 할 때:

```bash
./gradlew :core:test            # 순수 로직 테스트 (에뮬레이터 불필요)
./gradlew :app:installDebug     # 연결된 에뮬레이터에 설치
adb shell monkey -p com.springboard.app -c android.intent.category.LAUNCHER 1   # 실행
```

## 6. 자주 막히는 곳

| 증상 | 확인 |
|---|---|
| Gradle Sync에서 `ai.pleos.playground:*` 를 못 찾음 | 네트워크가 `nexus-playground.pleos.ai`를 막는지, 버전 오타 |
| 차량 값이 "응답 없음" | Cold boot로 만든 AVD인지, 에뮬레이터 부팅이 끝났는지, 권한 선언(CAR_DRIVE 등) |
| "Pleos 차량 서비스 없음" | Pleos 이미지가 아닌 일반 Automotive 이미지로 띄운 경우 |
| STT가 아무 말도 못 들음 | Extended Controls > Microphone 설정, 첫 마이크 열림은 수 초 걸림 |
| 기어를 바꿔도 화면이 안 바뀜 | 로그캣 `DrivingState` 태그 확인. 규제는 `adb shell cmd hmg_car_service --service drivingstate driving_regulation on`으로도 켤 수 있음 |
| 에뮬레이터가 매우 느림/검은 화면 | GPU 사양, Graphics=Hardware, RAM 4GB, 다른 AVD 종료 |

참고 링크
- Android Emulator 문제 해결: https://developer.android.com/studio/run/emulator-troubleshooting
- Android Studio·AGP 알려진 문제: https://developer.android.com/studio/known-issues
