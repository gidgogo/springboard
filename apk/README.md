# apk/ — 제출용 APK 자리

공모전 GitHub 제출 규칙에 따라 **실행 가능한 release 빌드** APK를 이 폴더에 둡니다.

| 항목 | 규칙 |
|---|---|
| 위치 | 저장소 루트의 `apk/` 폴더 |
| 파일 이름 | `팀명_앱이름_release.apk` |
| 빌드 종류 | 서명된 release 빌드 (debug 빌드·서명 안 된 APK는 안 됨) |
| 용량이 클 때 | GitHub에 올라가지 않으면 GitHub **Releases**에 올리고, README에 Release 페이지 주소를 적음 |

## 만드는 방법

1. 릴리스 서명 키를 만들고 `keystore.properties`를 준비합니다 → [`docs/04-dev/build-and-release.md`](../docs/04-dev/build-and-release.md)
2. `gradle.properties`에서 `submission.teamName`, `submission.appName`을 정합니다. 실명이나 소속이 드러나는 이름은 쓰지 않습니다 (블라인드 심사).
3. 아래 명령을 실행합니다.

```bash
./gradlew :app:exportReleaseApk
```

`apk/<팀명>_<앱이름>_release.apk`가 생깁니다. `.gitignore`는 이 이름 패턴(`apk/*_release.apk`)만 커밋을 허용합니다.

## 올리기 전에 확인

- [ ] Pleos Connect Emulator에 `adb install -r apk/<파일>.apk`로 설치해 실행되는지 확인
- [ ] 앱 이름·아이콘·버전(versionName)이 기획서와 같은지 확인
- [ ] README의 "APK 위치" 항목이 이 파일을 가리키는지 확인
