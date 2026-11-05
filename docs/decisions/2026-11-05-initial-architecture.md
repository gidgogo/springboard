# 초기 기술 구조 (2026-11-05)

- 상태: 확정 (아이디어 확정 후 다시 검토)
- 결정: Kotlin + Jetpack Compose 단일 앱, 순수 로직은 `core` 모듈로 분리, 주행 중 사용(DO 선언)을 기본으로 한다.

## 이유

| 선택 | 이유 |
|---|---|
| Kotlin + Compose | Pleos 공식 샘플 3종이 모두 Kotlin·Compose. 주차/주행 화면 전환과 portrait/landscape 대응이 쉽다 |
| AGP 8.8.2 · Gradle 8.10.2 · Kotlin 2.0.21 · compile/target 35 | 공식 샘플과 같은 조합 → Pleos SDK 빌드 호환이 검증됨 (CI 확인) |
| `core` 모듈 분리 | GPU 없이도 로직을 검증하고, 두 사람이 화면/연동을 나눠 작업하기 쉽다 |
| DO 선언 + `DrivingScreen` | 차량특화성(주행 맥락) 어필, 동시에 CarUxRestrictions를 따르는 안전 설계를 보여줄 수 있다 |
| 수동 DI (`AppContainer`) | 10일 일정에 Hilt 학습·설정 비용이 아깝다. 필요해지면 도입 |
| 온디바이스 TTS/STT | 콘솔 상태가 "준비 중"이어도 신청 없이 동작, 네트워크 없이 시연 가능 |
| FusedLocation 미사용 | 콘솔 "준비 중". 위치는 NaviHelper로 먼저 해결 |
| CCP(로터리) 미지원 선언 | 사용자 설치 앱은 선택 사항. 대신 터치만으로 모든 조작이 가능해야 함 |

## 검토했다 버린 것

- **Car App Library(템플릿 앱)**: Google 템플릿 호스트가 필요한데 Pleos(Google 서비스 없는 AAOS) 지원이 확인되지 않음.
- **최신 AGP 9.x**: lint가 새 버전을 알려 주지만, Pleos 샘플과 다른 조합은 검증 비용이 든다. 마감 후 검토.
- **XML View UI**: 샘플·자료가 Compose 중심.

## 영향

- 아이디어가 "주차 전용"이면 매니페스트의 `distractionOptimized`를 지운다 ([driving-safety-and-ux.md](../02-pleos/driving-safety-and-ux.md) 1절).
- 앱 이름·패키지 확정 시 [architecture.md](../04-dev/architecture.md) 5절대로 바꾼다.
