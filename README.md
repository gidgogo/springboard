# Springboard (가제)

현대자동차그룹 **차량용 앱 개발 공모전**(Pleos Connect · Pleos App Market) 출품을 준비하는 2인 팀 저장소입니다.

- 접수 마감 **2026-11-15(일) 23:59** · 1차 결과 11-23 · **2차 PT(실제 동작 시연) 12-03~04**
- 지금 상태: 아이디어 선정 중(11-07 확정 목표). 앱은 Pleos SDK 연동을 점검할 수 있는 **동작하는 뼈대**가 준비되어 있고, CI가 코드가 바뀔 때마다 빌드·테스트합니다.

---

## 공모전 제출용 README 필수 항목

> 공모전 GitHub 제출 규칙의 README 필수 4항목입니다. **제출 전에 실제 내용으로 바꿉니다.** (블라인드: 실명·소속·연락처 금지)

| 항목 | 내용 |
|---|---|
| **앱 이름 및 한 줄 소개** | _(미정 — 아이디어 확정 후 작성)_ |
| **실행 방법** | ① Pleos Connect Emulator(v2.1.0, API 34) 실행 ② `adb install -r apk/<팀명>_<앱이름>_release.apk` ③ 앱 목록에서 실행 — 소스로 실행할 때는 아래 "빠른 시작" |
| **테스트 환경 및 필요한 설정** | Pleos Connect Emulator v2.1.0 (Android 14 / API 34, Cold boot, RAM 4GB) · Virtual Console(`adb forward tcp:8088 tcp:8088` → `http://127.0.0.1:8088`)로 기어·차량 상태 조작 · 음성 기능은 에뮬레이터 Extended Controls > Microphone 허용 · 속도 표시는 앱 안에서 CAR_SPEED 권한 허용 |
| **APK 파일 위치** | `apk/<팀명>_<앱이름>_release.apk` _(제출 시 생성 — [apk/README.md](apk/README.md))_ |

---

## 빠른 시작

```bash
git clone https://github.com/gidgogo/springboard.git
cd springboard
./gradlew :core:test           # 순수 로직 테스트 (에뮬레이터 불필요)
./gradlew :app:installDebug    # 실행 중인 에뮬레이터에 설치
```

1. 개발 환경(Android Studio, Pleos Connect Emulator — **외장 GPU 필요**): [docs/02-pleos/dev-environment.md](docs/02-pleos/dev-environment.md)
2. 앱을 켜면 **SDK 점검 화면**이 뜹니다: 주행 규제·기어·속도 / Vehicle SDK 값 / TTS·STT / NaviHelper / 데모 시나리오
3. Virtual Console에서 기어를 **D**로 바꾸면 화면이 **주행 모드**(큰 글씨·최소 정보·음성 우선)로 바뀝니다
4. GPU가 없으면 일반 태블릿 에뮬레이터에서 debug 빌드의 **데모 시나리오**로 같은 흐름을 확인합니다

## 저장소 구조

```
springboard/
├── app/        Pleos Connect 안드로이드 앱 (Kotlin, Jetpack Compose)
│   ├── platform/   Car API·Pleos SDK 연동 (주행 규제, Vehicle, NaviHelper, TTS, STT, Gleo)
│   └── ui/         주행 화면(DrivingScreen) / 주차 화면(ParkedScreen) / 차량용 컴포넌트·테마
├── core/       안드로이드 없이 테스트하는 로직 (주행 정책, 데모 시나리오, 음성 명령, Gleo JSON)
├── apk/        제출용 release APK 자리 (팀명_앱이름_release.apk)
├── docs/       공모전·기획·Pleos·참고자료·개발·제출 문서, 결정 기록
└── .github/    CI(빌드·테스트·lint), PR·이슈 템플릿
```

자세한 구조와 기능 추가 순서: [docs/04-dev/architecture.md](docs/04-dev/architecture.md)

## 문서

**처음이라면 이 순서로**: ① [공모전 규칙](docs/00-contest/contest-overview.md) → ② [일정·역할](docs/01-plan/schedule-and-roles.md) → ③ [아이디어 고르기](docs/01-plan/idea-selection.md) → ④ [개발 환경](docs/02-pleos/dev-environment.md) → ⑤ [SDK 레퍼런스](docs/02-pleos/sdk-reference.md) → ⑥ [주행 안전·UX](docs/02-pleos/driving-safety-and-ux.md) → ⑦ [제출 점검표](docs/05-submission/submission-checklist.md)

전체 목록: [docs/README.md](docs/README.md)

## 기술 요약

| 항목 | 선택 |
|---|---|
| 언어·UI | Kotlin 2.0, Jetpack Compose (Material 3) |
| 빌드 | Gradle 8.10.2, AGP 8.8.2 (Pleos 공식 샘플과 같은 조합), Java 17 |
| SDK | minSdk 28 (Pleos 필수), compile/target 35 |
| Pleos SDK | Vehicle 2.3.2 · NaviHelper 3.0.5 · TextToSpeech 3.3.1 · SpeechToText 3.3.1 (온디바이스) |
| 주행 규제 | AAOS `CarUxRestrictionsManager` + `GEAR_SELECTION` + `PERF_VEHICLE_SPEED`, DO 선언 |
| 테스트 | `core` 단위 테스트 34개, GitHub Actions CI (빌드·테스트·lint) |

## 팀 규칙 (요약)

- `main`은 항상 빌드되는 상태. 작업은 브랜치 → PR → 상대 리뷰 → 머지.
- **블라인드 심사**: 커밋 작성자 이름·이메일, README, 앱 문구, 영상 어디에도 실명·소속·연락처를 넣지 않습니다.
- 키·토큰·서명 키(`keystore.properties`, `*.jks`)는 절대 커밋하지 않습니다.
- 자세히: [CONTRIBUTING.md](CONTRIBUTING.md)

> ⚠️ 이 저장소는 **Public**입니다. 아이디어가 정해지면 다른 팀에게 그대로 보이므로 Private 전환을 검토하세요.
> Private으로 제출하면 공지되는 심사용 계정을 Collaborator로 초대해야 합니다.

## 출처 · 라이선스

- 사용한 오픈소스와 SDK: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) (공모전 이용약관: 오픈소스 출처 표기 의무)
- 이 저장소의 라이선스는 정하지 않았습니다(모든 권리 보유). 공모전 지식재산권 규정(수상작 통상실시권·우선협상권)을 보고 팀이 정합니다.
