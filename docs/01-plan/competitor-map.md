# 공개된 다른 참가팀 주제 지도

> **2026-11-05 기준 스냅숏.** GitHub 코드 검색으로 `ai.pleos.playground`(Pleos SDK 패키지)를 쓰는 공개 저장소를 찾아,
> 저장소 소개 문구만 보고 분류했습니다. 비공개로 준비하는 팀이 훨씬 많을 것이므로 "이 영역이 비었다"는 뜻은 아닙니다.
>
> **용도는 겹침 피하기뿐입니다.** 다른 팀의 코드·문구·기획을 가져오지 않습니다 (표절은 심사 제외 사유이고, 이 저장소들에는 라이선스가 없습니다).

## 주제별 분류

| 분류 | 저장소 (소개 문구 요약) |
|---|---|
| **이야기·동화·오디오 해설** (가장 붐빔) | `WhoisHOO/pleos-app-challenge` — Roadio, 경로 따라 장소 이야기를 들려주는 오디오 도슨트 · `joooon28/Roadio` · `xxcordeau/running-tale` — 실제 주행 신호로 흘러가는 뒷좌석 동화 · `ShakeJ/dawasseo` — "다 왔어?", 도착 시간에 맞춰 끝나는 차 안 이야기 · `soojeongmin/StoryDrive` |
| **차량 정비·관리** | `Mujin8623/chadak-pleos-app` — 차닥, 경고·소모품 신호를 3단계로 나누고 음성 안내 후 정비소를 경유지로 · `hyuntaek2002/precheck` — 차량 관리 앱 |
| **장소 찾기** | `panicgit/poopilot` — 급할 때 화장실 찾기 · `QuantumNovice/feels-good` — 화장실·주차·데이트·관광·팝업 장소 |
| **운전 습관 코칭** | `tsetse012/cornernote` — 차량 신호로 코너별 원인과 처방 |
| **EV 충전** | `ian939/ev-fit` — 경로 위에서 내 차·결제수단에 맞는 충전소 고르기 |
| **뒷좌석·동승자 케어** | `tsetse012/rearguard` — 뒷자리 케어 · `sj-park-kr/appdevchallenge` — 조사 단계 후보로 고속도로 위기 대응, 멀미 케어 |
| **업무용 운행일지** | `chabinhwang/autolog-hyundai-2026` — 차가 쓰고 한마디로 끝내는 업무용 승용차 운행일지 |
| **여러 차 함께 이동** | `jonik2909/ConvoyStop` — 동행 차량 정차 계획 |
| **게임** | `HeIIowrld/z-ride` — 협동 게임 서버·연동 |
| **주차 중 사용 앱 / 실험** | `Fiddich-Dev/cabincue-pleos` — 주차 중 전용 앱 · `IJLee0812/Pleyground` — 실내 앱 프로토타입 |
| 조사·미정·설명 없음 | `leetae9yu/ulw-research`, `Univ-JH/pleos-app-2026`, `HanGilLab/Android`, `danmool78/h-k`, `khj777/senga`, `youngjin9145/buckleair`, `woominsung1116/tarang`, `bbang208/Spirit-AAOS`, `boseungk/restcar`, `tsetse012/gilnun`, `tjdus3725/contest_project` |

공모전 가이드 예시(음성 메모, 안전운전 점수, 배달 동선)와 이전 수상작(창밖동화, TaleSync)까지 합치면
**"이동 중 이야기·콘텐츠"**와 **"정비·장소·운전 점수"**는 이미 경쟁이 많습니다.

## 덜 보이는 출발점 (아이디어가 아니라 질문)

공개 저장소에서는 잘 보이지 않는 장면들입니다. 1단계 불편 장면 수집의 질문으로 쓰세요.

- 차에 탄 **반려동물**은 누가 챙기나? (좌석 탑승, 실내 온도, 정차 시간)
- **고령 운전자**·**초보 운전자**가 가장 긴장하는 순간은? (합류, 후진 주차, 야간·우천)
- 비·밤·터널처럼 **와이퍼·전조등이 바뀌는 순간** 운전자에게 필요한 한마디는?
- **출퇴근 같은 반복 이동**에서 매번 손으로 하는 일은? (목적지, 공조, 알림)
- **충전·정차 대기 시간**(10~30분)을 차 안에서 어떻게 보내나?
- 운전자와 뒷좌석이 **말로 주고받기 어려운 것**은? (멀미, 화장실, 소리 크기)
- **여러 사람이 같이 쓰는 차**(가족 공용, 공유차, 법인차)에서 사람마다 다른 것은?

## 다시 확인하는 법

```bash
# Pleos SDK 를 쓰는 공개 코드 검색 (GitHub 로그인 필요)
gh api -X GET search/code -f q='"ai.pleos.playground" in:file' --jq '[.items[].repository.full_name] | unique'
```

주 1회 정도 다시 보고, 핵심 장면이 겹치는 팀이 생기면 차별점을 기획서 2·3번에 분명히 적습니다.
