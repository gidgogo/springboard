# Pleos 플랫폼 한눈에 보기

> 출처: Pleos Playground 문서(document.pleos.ai, 2026-10-19 개편본), Pleos 공식 GitHub(github.com/Pleos-Playground), 언론 보도.

## 1. 이름 정리

| 이름 | 무엇인가 |
|---|---|
| **Pleos** | 현대차그룹(42dot)의 차량 소프트웨어 브랜드 |
| **Pleos Connect** | Android Automotive OS(AAOS) 기반 차량 인포테인먼트 플랫폼. 차량 데이터·상태를 API로 제공. 멀티 윈도(Driving view + 앱 창 1~2개) |
| **Pleos Playground** | 개발자 포털. 가입, 프로젝트 생성, SDK 사용 신청, 문서 (https://pleos.ai/playground) |
| **Pleos Connect SDK** | Vehicle, NaviHelper, TextToSpeech, SpeechToText, FusedLocation (+ 준비 중인 Vehicle Control) |
| **Pleos Connect Emulator** | Android Studio에서 쓰는 AVD 시스템 이미지 (현재 v2.1.0, Android 14 / API 34) |
| **Pleos Connect Virtual Console** | 에뮬레이터의 차량 상태(기어, 도어, 배터리, 공조…)를 브라우저에서 바꾸는 도구 |
| **Gleo AI** | LLM 기반 차량 음성 비서. 앱은 Gleo Interface(App Schema)로 연동 |
| **Pleos App Market** | 차량용 앱 마켓. 심사 후 배포 (공모전 수상작은 탑재 준비 지원) |
| **Connect-S / Connect-W / Connect-WF** | 디스플레이 형태: Standard(17·14.6·12.9인치), Wide, Waterfall |

## 2. 계정과 프로젝트 (D-10 첫날에 할 일)

1. **Pleos Playground 가입**: https://pleos.ai/playground → 로그인 → "Pleos 계정 만들기"
2. **프로젝트 생성**: 상단 `Workspace > My Project` → 프로젝트 생성
   - 프로젝트 이름(나중에 변경 가능), 개발사명, **카테고리**(아래 표), 앱 설명
   - 블라인드 심사와는 별개로 Playground 계정은 실명이어도 되지만, **이 정보를 저장소에 커밋하지 않는다**
3. **팀원 추가**: 프로젝트의 팀 관리에서 상대를 초대
4. **SDK 상태 확인**: 프로젝트 → SDK 메뉴

| SDK | 콘솔 상태 (2026-10 기준) | 의미 |
|---|---|---|
| Vehicle State (조회) | ✅ 사용 가능 | 신청 없이 바로 사용 |
| NaviHelper | ✅ 사용 가능 | 신청 없이 바로 사용 |
| TextToSpeech / SpeechToText | ⏳ 준비 중 (Coming Soon) | 서버 모드(클라이언트 ID·시크릿 필요)는 아직. **온디바이스(Mode.ON_DEVICE)는 신청 없이 동작** |
| FusedLocation | ⏳ 준비 중 | 위치는 NaviHelper `getCurrentLocationInfo`로 대신 얻을 수 있음 |
| Vehicle Control (제어) | ⏳ 준비 중 | 도어·창문·시트·공조 **조작**은 핵심 기능으로 삼지 말 것 |

- 프로젝트 정보의 **프로젝트 ID(Key)**: App Market 심사 요청 때 필요
- **클라이언트 ID / 시크릿**: 서버 연결이 필요한 SDK(TTS·STT 서버 모드)용. **절대 커밋 금지** (보안 가이드: 하드코딩 금지)
- **CRN**: Emulator 2.1.0부터 입력 불필요

## 3. 앱 카테고리 (설계 시점에 정한다)

| 카테고리 | 핵심 목적 | 주행 중 기능 |
|---|---|---|
| Maps/Navigation | 위치 기반 길 안내 | 제한적 허용 (경로 안내, TBT 음성 등) |
| Mobility Services | 이동 수단·이동 서비스 | 제한적 허용 (주행 관련 차량 관리, 위치 기반 즉시 정보, 충전소·주유소·주차장) |
| Music/Audio | 오디오 콘텐츠 | 제한적 허용 (단순 조작, 청취) — MediaBrowserService 필수 |
| Lifestyle | 일상 관리 | 주로 정차 중 |
| Entertainment | 오락·여가 | 정차 중 (게임·영상은 주행 중 불가) |
| Business | 업무·생산성 | 주로 정차 중 |
| Kids | 아동 콘텐츠 | 주로 정차 중 / 뒷좌석 |
| Social/Communication | 소통 | 제한적 |

카테고리와 실제 기능이 다르면 App Market 심사에서 반려됩니다. 자세한 주행 규제: [driving-safety-and-ux.md](driving-safety-and-ux.md)

## 4. 플랫폼 원칙 (설계에 바로 영향을 주는 것)

- **하드웨어 직접 접근 금지**: 차량 상태는 Pleos SDK 또는 AAOS 표준 API로만.
- **AAOS 우선 정책**: 같은 기능이 AAOS 표준에 있으면 AAOS를 쓴다 (속도·기어 → `CarPropertyManager`). SDK는 제조사 특화 영역만 제공.
- **차종마다 지원 API가 다름**: Vehicle SDK는 `checkXxxCapabilities`, FusedLocation은 `isSupported`로 확인. 모델명으로 분기하지 않는다.
- **네트워크 불안정 대비**: 캐싱, 재시도·타임아웃, 오프라인 안내 UI.
- **모바일 전용 API 제한**: 전화·SMS·일부 센서·캘린더·주소록 등은 없거나 제한될 수 있다.
- **Google Play 서비스 없음(non-GAS)**: Firebase·Google 지도 SDK 등 Play 서비스 의존 라이브러리는 동작하지 않을 수 있다.

## 5. 도움 받을 곳

| 채널 | 용도 |
|---|---|
| GitHub Discussions — https://github.com/Pleos-Playground/pleos-playground-api/discussions | 공식 Q&A(General / Technical), FAQ, 공지. **공개 포럼이라 키·토큰·개인정보 금지** |
| partnership@pleos.ai | 개발사 지원 이메일 (공개하기 어려운 문의) |
| appdevchallenge@hyundai-partners.com | 공모전 운영 사무국 (규정·제출 문의) |
| Pleos 공식 샘플 — https://github.com/Pleos-Playground | Vehicle / NaviHelper / FusedLocation 샘플 앱 |
