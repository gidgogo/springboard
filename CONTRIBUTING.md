# 협업 규칙 (2인 팀)

목표는 하나다: **마감까지 `main`이 항상 빌드되고, 두 사람이 서로의 작업을 막지 않는 것.**

## 1. 처음 한 번: 커밋 작성자 설정 (블라인드 심사)

공모전은 블라인드 심사다. **커밋 작성자 이름·이메일도 심사위원에게 보인다.** 이 저장소에서만 쓰는 값으로 설정한다.

```bash
cd springboard
git config user.name  "springboard-a"     # 다른 팀원은 springboard-b (실명·학번 금지)
git config user.email "<GitHub 숫자ID>+<GitHub아이디>@users.noreply.github.com"
```

- GitHub noreply 주소: GitHub → Settings → Emails → "Keep my email addresses private"를 켜면 보인다.
- GitHub 계정 아이디 자체가 실명이면 제출 방식을 따로 의논한다 (팀 계정·조직 계정 등).
- 확인: `git log --format='%an <%ae>' | sort -u`
- 이미 실명으로 커밋했다면 푸시 전에 알려 달라 (공개 저장소라 푸시 후에는 기록이 남는다).
- 커밋 메시지 `scope`에도 이름을 쓰지 않는다 (`feat(core): …` 처럼 영역을 쓴다).

## 2. 브랜치

| 브랜치 | 용도 |
|---|---|
| `main` | 항상 빌드·실행되는 상태. 제출본 기준. 직접 push 하지 않는다 |
| `feat/<짧은-설명>` | 기능 (예: `feat/charge-advisor`) |
| `fix/<설명>` | 버그 |
| `docs/<설명>` | 문서 |
| `chore/<설명>` | 빌드·설정 |

- 작업 단위 = **Issue 1개 = 브랜치 1개 = PR 1개**. 하루 안에 끝나는 크기로 자른다.
- 시작 전에 `git pull origin main`, 하루 한 번 이상 main을 받아 충돌을 작게 유지한다.

권장 GitHub 설정 (저장소 주인이 한 번): Settings → Branches → `main` 규칙 추가 →
"Require a pull request before merging"(승인 1), "Require status checks to pass"(`Build & test`).

## 3. 커밋 메시지

[Conventional Commits](https://www.conventionalcommits.org/ko/) 형식, 설명은 한국어.

```
<type>(<scope>): <무엇을 했는지 한 줄>

<왜 했는지, 필요하면 몇 줄>
```

| type | 언제 |
|---|---|
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서만 |
| `refactor` | 동작 변화 없는 구조 개선 |
| `test` | 테스트 |
| `build` / `ci` / `chore` | 빌드·CI·잡일 |

scope 예: `core`, `app`, `ui`, `platform`, `voice`, `navi`, `docs`

## 4. Pull Request

- PR 템플릿을 채운다 (확인 방법, 주행 안전 체크, 권한·의존성 변경, 블라인드 체크).
- **CI(`Build & test`)가 초록색이어야** 머지한다.
- 상대가 보고 머지한다. 급하면 셀프 머지 가능하지만 PR 설명에 "셀프 머지 사유"를 남긴다.
- 머지는 **Squash and merge** (main 기록을 깔끔하게).
- 리뷰는 24시간 안에, 큰 의견보다 "막히는 것"부터.

리뷰 기준
1. 실행해 봤는가 (에뮬레이터 또는 데모 모드)
2. 주행 중 화면 원칙을 지키는가 ([driving-safety-and-ux.md](docs/02-pleos/driving-safety-and-ux.md) 11절 체크리스트)
3. 새 권한·라이브러리에 이유가 있는가, [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)를 갱신했는가
4. 로직이 `core`에 있고 테스트가 있는가

## 5. 코드 규칙

- 스타일: `.editorconfig` + Kotlin 공식 스타일 (Android Studio 기본 포매터).
- **SDK·Car API 코드는 `app/.../platform/` 안에만.** 화면·ViewModel은 `core` 타입만 쓴다.
- **판단 로직은 `core`에, 테스트와 함께.** 테스트 함수 이름은 영어, 설명은 주석.
- 주행 중에 보이는 화면은 `DrivingUiPolicy`를 따른다 (글자 수·목록 수·키보드).
- 비밀값(키·토큰·비밀번호)은 코드·리소스·설정 파일에 넣지 않는다. `secrets.properties`·`keystore.properties`는 커밋 금지.
- 로그에 위치·음성 원문·VIN 같은 개인정보를 찍지 않는다.
- 데모·테스트 데이터는 `BuildConfig.DEBUG`일 때만.

## 6. 결정 기록

아이디어 확정, 범위 축소, 기술 선택처럼 나중에 "왜 이렇게 했지?"가 될 일은
[`docs/decisions/`](docs/decisions/)에 한 쪽짜리 메모로 남긴다. (형식은 그 폴더의 README)

## 7. 매일 10분 점검

저녁에 이슈 보드(또는 메신저)에 각자 세 줄:
1. 어제 한 것  2. 오늘 할 것  3. 막힌 것 (막힌 것은 바로 같이 본다)

일정표: [docs/01-plan/schedule-and-roles.md](docs/01-plan/schedule-and-roles.md)
