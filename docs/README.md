# 문서 지도

공모전 준비에 필요한 자료를 주제별로 나눠 정리했습니다. **처음이라면 굵게 표시한 문서부터** 읽으세요.

> 작성 기준일: 2026-11-05 (접수 마감 11-15 23:59까지 D-10)
> 공모전 공식 사이트(appdevchallenge.auto-hmg.io)와 Pleos 문서(document.pleos.ai)는 정리 당시 직접 열 수 없어서,
> 보도자료 원문 · Pleos 공식 GitHub 샘플 · 공식 문서를 옮겨 둔 공개 자료를 대조해 정리했습니다.
> **일정·규정·배점처럼 중요한 내용은 제출 전에 공식 페이지에서 한 번 더 확인하세요.** 각 문서 맨 위에 출처를 적었습니다.

## 00 공모전

| 문서 | 내용 |
|---|---|
| **[contest-overview.md](00-contest/contest-overview.md)** | 일정, 자격, 시상, 심사 기준, 제출물, GitHub 규칙, 실격 사유 — 공모전의 모든 규칙 |
| [briefing-video.md](00-contest/briefing-video.md) | 온라인 설명회 다시보기(유튜브) 구간 안내와 보면서 채울 확인 목록 |

## 01 기획

| 문서 | 내용 |
|---|---|
| **[schedule-and-roles.md](01-plan/schedule-and-roles.md)** | 2인 팀 10일 일정표(D-10 → 마감), 역할 분담, 2차 PT까지 계획 |
| **[idea-selection.md](01-plan/idea-selection.md)** | 아이디어 발굴 → 차량 데이터 매핑 → 채점표 → 확정 절차 |
| [competitor-map.md](01-plan/competitor-map.md) | 공개 저장소로 보이는 다른 참가팀 주제 지도 (겹침 피하기) |
| **[proposal-template.md](01-plan/proposal-template.md)** | 접수양식(기획서) 8개 항목 작성 틀과 예시 문장 |
| [business-metrics.md](01-plan/business-metrics.md) | 타깃·예상 MAU·Retention·확대 계획을 근거 있게 쓰는 방법 |

## 02 Pleos 플랫폼

| 문서 | 내용 |
|---|---|
| [pleos-overview.md](02-pleos/pleos-overview.md) | Pleos Connect · Playground · App Market · Gleo, 가입과 프로젝트 생성 |
| **[dev-environment.md](02-pleos/dev-environment.md)** | Android Studio, Pleos Connect Emulator 설치(GPU 요구사항 포함), 이 프로젝트 열기 |
| **[sdk-reference.md](02-pleos/sdk-reference.md)** | SDK 버전·좌표·권한·제공 상태, 도메인별 API, 주의할 변경점, AAOS API |
| [emulator-and-virtual-console.md](02-pleos/emulator-and-virtual-console.md) | Virtual Console 카드 ↔ API 대응표, 시연 가능/불가 목록, adb 명령 |
| **[driving-safety-and-ux.md](02-pleos/driving-safety-and-ux.md)** | 주행 규제(DO), 지역별 허용표, 디자인 수치, 화면 모델, CCP Back, 성능 기준 |
| [audio-and-voice.md](02-pleos/audio-and-voice.md) | 오디오 포커스, Suspend, TTS 선행 묵음, 음성 UX |
| [gleo-interface.md](02-pleos/gleo-interface.md) | Gleo AI 음성 연동(App Schema · Action · Callback) |
| [review-and-security.md](02-pleos/review-and-security.md) | App Market 심사 기준, 보안·개인정보 가이드 요점 |

## 03 참고 자료

| 문서 | 내용 |
|---|---|
| **[open-source.md](03-references/open-source.md)** | 공식 샘플, AAOS 샘플, 아키텍처 레퍼런스, 라이브러리(라이선스), 공개 참가 저장소 |
| [public-data-apis.md](03-references/public-data-apis.md) | 충전소·휴게소·교통·유가·날씨·관광 공공 API |
| [links.md](03-references/links.md) | 모든 링크 모음 |

## 04 개발

| 문서 | 내용 |
|---|---|
| **[architecture.md](04-dev/architecture.md)** | 모듈 구조, 데이터 흐름, 주행/주차 화면 전환, 기능 추가 순서 |
| **[build-and-release.md](04-dev/build-and-release.md)** | 빌드, 서명 키, 제출용 APK, 버전 올리기, CI |
| [testing-and-demo.md](04-dev/testing-and-demo.md) | 단위 테스트, 에뮬레이터 점검표, 데모 모드, 시연 시나리오 |

## 05 제출

| 문서 | 내용 |
|---|---|
| **[submission-checklist.md](05-submission/submission-checklist.md)** | 마감 전 최종 점검표 (이메일·서명·블라인드·GitHub·APK·영상) |
| [demo-video-guide.md](05-submission/demo-video-guide.md) | 시연 영상(가산점)·숏폼 챌린지 만드는 법 |

## 결정 기록 · 템플릿

- [decisions/](decisions/README.md) — 아이디어 확정, 범위 축소, 기술 선택 같은 결정 메모 (첫 기록: [초기 기술 구조](decisions/2026-11-05-initial-architecture.md))
- [templates/gleo-schema.example.json](templates/gleo-schema.example.json) — Gleo App Schema 예시 (core 단위 테스트가 규칙 검사를 함)

## 저장소 밖에서 할 일 (사람이 직접)

- [ ] Pleos Playground 가입·프로젝트 생성·팀원 초대 ([pleos-overview.md](02-pleos/pleos-overview.md))
- [ ] 설명회 다시보기 시청 후 [briefing-video.md](00-contest/briefing-video.md) 채우기
- [ ] GitHub `main` 브랜치 보호 규칙 설정 ([CONTRIBUTING.md](../CONTRIBUTING.md) 2절)
- [ ] 각자 커밋 작성자 이름·이메일 설정 ([CONTRIBUTING.md](../CONTRIBUTING.md) 1절)
- [ ] Public/Private 결정
