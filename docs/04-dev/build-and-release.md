# 빌드 · 서명 · 제출용 APK · CI

## 1. 자주 쓰는 명령

```bash
./gradlew :core:test                 # 순수 로직 단위 테스트 (에뮬레이터 불필요, 수 초)
./gradlew :app:assembleDebug         # debug APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug          # 연결된 에뮬레이터/기기에 설치
./gradlew :app:lintDebug             # lint → app/build/reports/lint-results-debug.{html,txt}
./gradlew :app:assembleRelease       # release APK (keystore.properties 있으면 서명됨)
./gradlew :app:exportReleaseApk      # 서명된 release APK 를 apk/<팀명>_<앱이름>_release.apk 로 복사
```

Windows는 `./gradlew` 대신 `gradlew.bat`.

## 2. 버전 조합 (바꿀 때 주의)

| 도구 | 버전 | 이유 |
|---|---|---|
| Gradle (래퍼) | 8.10.2 | Pleos 공식 샘플과 같음 |
| Android Gradle Plugin | 8.8.2 | 공식 샘플과 같음 |
| Kotlin | 2.0.21 | Compose 컴파일러 플러그인 포함 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 28 | minSdk 28은 Pleos 필수 |
| JDK | 17 | |
| Pleos SDK | Vehicle 2.3.2, NaviHelper 3.0.5, TTS/STT 3.3.1 | 2026-10-19 릴리스 노트 최신 |

### 버전 올리기 절차
1. [`gradle/libs.versions.toml`](../../gradle/libs.versions.toml)에서 한 줄만 바꾼다.
2. Pleos SDK는 **릴리스 노트의 Breaking change**를 먼저 읽는다 (예: NaviHelper 3.0.5 좌표 순서 변경).
3. `./gradlew :core:test :app:assembleDebug` → 에뮬레이터에서 점검 화면 확인 → PR.
4. 마감 1주 전(11-08) 이후에는 버전을 올리지 않는다.

## 3. 릴리스 서명 키 만들기 (한 번만, 한 사람이)

공모전 제출 APK는 **실행 가능한 release 빌드**여야 하고, Pleos 보안 가이드는 **정식 배포 서명**을 요구한다.

```bash
mkdir -p keystore
keytool -genkeypair -v \
  -keystore keystore/springboard-release.jks \
  -alias springboard \
  -keyalg RSA -keysize 2048 -validity 10000
```

- 이름·조직을 묻는 항목에 **실명·학교·회사를 넣지 않는다** (APK 서명 인증서에서 보인다 → 블라인드 심사). 팀명이나 `Unknown`을 쓴다.
- 저장소 루트에 `keystore.properties` 작성 (`.gitignore`가 `keystore.properties`, `*.jks`를 막는다):

```properties
storeFile=keystore/springboard-release.jks
storePassword=비밀번호
keyAlias=springboard
keyPassword=비밀번호
```

- **키와 비밀번호를 잃으면 같은 서명으로 업데이트할 수 없다.** 저장소 밖 두 곳 이상(예: 두 팀원 각자의 비밀 보관함)에 백업.
- 상대 팀원에게는 메신저가 아니라 안전한 방법(비밀번호 관리자 공유 등)으로 전달.
- 확인: `keytool -list -v -keystore keystore/springboard-release.jks` → 소유자(Owner)에 실명이 없는지.

## 4. 제출용 APK 만들기

1. `gradle.properties`의 `submission.teamName`, `submission.appName`을 정한다 (실명·소속 금지).
2. `app.versionName` / `app.versionCode`를 확인한다 (기획서와 같은 버전).
3. 실행:

```bash
./gradlew :app:exportReleaseApk
adb install -r apk/<팀명>_<앱이름>_release.apk     # 에뮬레이터에서 꼭 실행해 본다
```

4. `apk/`의 APK를 커밋한다 (`.gitignore`가 `apk/*_release.apk` 패턴만 허용).
5. GitHub에 올라가지 않을 만큼 크면 GitHub **Releases**에 올리고 README의 "APK 위치"에 Release 주소를 적는다.

## 5. 버전 규칙

- 사용자에게 나가는 빌드(제출본, PT 시연본)를 만들 때마다 `app.versionCode`를 1씩 올린다. Pleos App Market은 버전을 올리지 않은 업데이트를 반려한다.
- `app.versionName`: 1차 제출 `0.1.0`, 2차 PT `0.2.0`, 전시 `1.0.0`처럼 의미 있게.

## 6. CI (GitHub Actions)

[`.github/workflows/android.yml`](../../.github/workflows/android.yml) — 코드가 바뀐 push·PR마다 실행 (문서만 바뀌면 건너뜀).

| 단계 | 내용 |
|---|---|
| Set up Gradle | `gradle-wrapper.jar` 무결성 검사 + 캐시 |
| core 테스트 | `./gradlew :core:test` |
| debug 빌드 + app 테스트 | `:app:assembleDebug :app:testDebugUnitTest` |
| lint | `:app:lintDebug` → 결과를 로그에 출력 (빌드는 막지 않음) |
| release 빌드 | `:app:assembleRelease` (CI에는 키가 없어 서명되지 않은 APK) |
| 아티팩트 | `app-debug-apk`(바로 설치해 볼 수 있는 debug APK), `reports`(lint·테스트 리포트) — 14일 보관 |

- PR을 머지하기 전에 CI가 초록색인지 확인한다.
- CI에서 서명된 release를 만들고 싶다면: keystore를 base64로 GitHub Secrets에 넣고 워크플로에서 `keystore.properties`를 만드는 단계를 추가 (Public 저장소에서는 권장하지 않음).
- debug APK 받기: Actions → 해당 실행 → Artifacts → `app-debug-apk`.

## 7. 빌드가 안 될 때

| 증상 | 원인·해결 |
|---|---|
| `Could not resolve ai.pleos.playground:...` | `nexus-playground.pleos.ai` 접속 차단(학교·회사망) / 버전 오타 |
| `SDK location not found` | `local.properties`에 `sdk.dir` 지정 또는 Android Studio로 열기 |
| `Unsupported class file major version` | JDK 17이 아닌 다른 JDK로 Gradle 실행 → Android Studio Gradle JDK를 17로 |
| 한글 경로·파일명 오류 | 프로젝트를 영문 경로에 둔다. 테스트 함수 이름도 영어로 (설명은 주석) |
| release APK 설치 실패 | 서명되지 않은 `app-release-unsigned.apk`를 설치하려 함 → 3절 서명 |
