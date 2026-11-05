# 제출 최종 점검표

> 마감: **2026-11-15(일) 23:59**. 목표 제출일: **11-13(금)** (마감 당일 제출 피하기).
> 규칙 원문 요약: [contest-overview.md](../00-contest/contest-overview.md)

## A. 기획서·동의서 (필수)

- [ ] 공식 접수양식 DOCX 최신본을 받아 그 양식에 작성 (https://appdevchallenge.auto-hmg.io/downloads/contest-application-consent.docx)
- [ ] 작품 소개 1~8번 모두 채움 ([proposal-template.md](../01-plan/proposal-template.md)의 마지막 점검까지)
- [ ] 개인정보 동의서: **팀원 각자** 동의 여부 · 서명 · 날짜
- [ ] 부문(대학생 / 일반) 맞게 표시

## B. 블라인드 심사 점검 (실격 위험 — 둘이 서로 한 번씩)

- [ ] 작품 소개(02장)·첨부·링크 어디에도 **이름·학교·회사·연락처·실명 계정**이 없다
- [ ] DOCX/PDF **파일 속성**(작성자·회사) 지움 — Word: 파일 > 정보 > 문서 검사
- [ ] GitHub: 저장소 주인 계정명, **커밋 작성자 이름·이메일**, README, 이슈·PR 작성자 확인
  - `git log --format='%an <%ae>' | sort -u` 로 작성자 목록 확인 ([CONTRIBUTING.md](../../CONTRIBUTING.md) 커밋 작성자 규칙)
- [ ] 앱 안 문구·아이콘·스플래시에 이름 없음
- [ ] APK 서명 인증서 소유자(Owner)에 실명 없음 (`keytool -list -v -keystore ...`)
- [ ] 영상·숏폼: 얼굴·목소리·계정명·메타데이터 확인 (`ffmpeg -map_metadata -1`)
- [ ] 영상 링크가 **로그인 없이** 열리고 소유자 이름에 실명이 안 보임

## C. GitHub 저장소 (가산점)

- [ ] 소스 + APK가 **한 저장소**에
- [ ] `apk/팀명_앱이름_release.apk` — **서명된 release 빌드**, 에뮬레이터에 설치·실행 확인 (`./gradlew :app:exportReleaseApk`)
- [ ] 너무 크면 GitHub Releases에 올리고 README에 Release 주소
- [ ] README 필수 4항목 최신: ① 앱 이름·한 줄 소개 ② 실행 방법 ③ 테스트 환경·필요 설정 ④ APK 위치 또는 Release 링크
- [ ] CI 초록색, `main`에 최신 코드
- [ ] Private로 낼 경우: 공지된 **심사용 계정을 Collaborator로 초대**하고 마감 전 접근 확인 (개인 계정 Private는 쓰기 권한까지 줌)
- [ ] 사용한 오픈소스와 라이선스가 [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md)에 있다 (출처 표기 의무)
- [ ] 키·토큰·`keystore.properties`·`.jks`가 커밋되지 않았다
- [ ] lint 리포트 오류 0 (CI 로그 "Lint 결과 요약")

## D. 시연 영상 (가산점)

- [ ] 60~90초, 실제 앱 화면(에뮬레이터) 중심 — 목업·프로토타입·화면 녹화 모두 인정
- [ ] 불편 장면 → 차량 신호에 반응 → 주행 중 음성 → 정차 후 상세 흐름이 보인다
- [ ] 자막 크게, 링크 또는 파일로 접수양식 7번에 첨부
- [ ] 만드는 법: [demo-video-guide.md](demo-video-guide.md)

## E. 숏폼 (선택, 가산점 없음 · 숏폼상 50만 원)

- [ ] 30초~1분 **세로** 영상, Instagram 릴스 또는 YouTube Shorts (팀 계정 권장)
- [ ] 해시태그 2~3개, **`#HMG차량용앱개발공모전` 필수**
- [ ] 링크를 접수양식 8번에

## F. 생성형 AI 사용

- [ ] 설명회(20:28~21:05) · FAQ의 생성형 AI 규정 확인 → [briefing-video.md](../00-contest/briefing-video.md)
- [ ] 이 저장소의 초기 틀·문서는 AI 코딩 도구로 만들었고, 해당 커밋에 `Co-Authored-By` 공동 작성자 표시가 있다.
      규정상 표기가 필요하면 기획서·README에 사용 범위를 밝히고, 허용되지 않는 범위가 있으면 그 부분을 직접 다시 만든다.

## G. 이메일 발송

- [ ] 받는 사람: `appdevchallenge@hyundai-partners.com`
- [ ] 제목: `[차량용 앱 개발 공모전] 팀명(개인은 성명)_대표자명`
- [ ] **팀원 전원 CC** (CC만으로 동의를 대신하지 않음 — 동의서 서명 필수)
- [ ] 첨부: 접수양식(기획서)+동의서 DOCX, (영상 파일 또는 링크), GitHub 주소는 기획서 7번에
- [ ] 첨부 용량·파일 열림 확인 (파일 오류·파손은 제출자 책임)
- [ ] 보낸 편지함에서 발송 확인, 스크린숏 보관

## H. 수정 제출이 필요하면 (마감 전까지)

- [ ] **처음 보낸 이메일 주소**로 다시 보낸다
- [ ] 제목: `[수정제출] [차량용 앱 개발 공모전] 팀명(개인은 성명)_대표자명`
- [ ] **전체 자료 + 서명 동의서 전부 다시 첨부 + 팀원 전원 CC** (빠지면 유효 접수 아님)
- [ ] 마지막 제출본이 최종

## I. 제출 후

- [ ] 11-23 1차 결과 확인
- [ ] 1차 제출물은 PT 전까지 고도화 가능 → [schedule-and-roles.md](../01-plan/schedule-and-roles.md) 4절
- [ ] 2차 PT(12-03~04) 장소·시간·준비물 공지 확인
