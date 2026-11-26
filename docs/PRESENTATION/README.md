# FINAL_PRESENTATION 제작 가이드

**WHO’S NEXT? — 객체지향프로그래밍 최종 발표**

[발표 예시](index.html) · [발표 원고](FINAL_PRESENTATION.md)

발표의 중심은 **기능별 MVP가 어떻게 객체지향 구조로 발전했는가**다. View 단위 분업에서 시작해 구조 개선 예시를 공유하고, 중간 평가를 반영한 화면 개편과 게임 기능으로 이어간다.

## 페이지 구성

| 페이지 | 제목 | 들어갈 내용 | 사진 |
|---|---|---|---|
| 01 | 팀 개발과 View 단위 MVP | 변형 Git Flow, develop, 회의, View별 역할, PR | 없음 |
| 02 | 설계 결정 ① View에서 View·Event·Feature로 | 개선 예시 브랜치, 생성자 주입, 관심사 분리, MVC를 도입하지 않은 배경 | 없음 |
| 03 | 전체 코드 구조와 도메인 기반 설계 | core의 공통 기반·domain의 사용자 기능, 실행·입력 흐름, layout/component | 없음 |
| 04 | 빌드 표준화와 Gradle 선택 | Wrapper, Windows·macOS·Linux, classpath 리소스, Maven과의 선택 기준 | 없음 |
| 05 | SOLID를 코드로 설명하기 | SOLID 다섯 원칙의 한글 명칭과 실제 코드 사례 | 없음 |
| 06 | 중간 평가 이후 UI와 게임 흐름 개편 | 중간 평가, 디자인 시스템, 전투 연출·스토리·대화 | `adventure-map.webp` |
| 07 | 전체 데이터와 화면 흐름 | 데이터 규모와 화면 이동 | `collection-map.webp` |
| 08 | 주요 화면 ① 연습 퀴즈 | 영역별 배치, 출제·셔플, 이벤트 전달 | `practice.webp` |
| 09 | 주요 화면 ② 배틀과 보상 | 전투 판정과 연출 분리, 정오답, 보상 정산 | `battle-question.webp`, `battle-feedback.webp`, `battle-result.webp` |
| 10 | 최종 목표와 시연 | 해금 조건, 구출 대화, F1 시연 | `rescue-dialogue.webp`, `showcase-menu.webp` |
| 11 | 자체 평가와 팀원 소감 | 성과, 한계, 실제 팀원 소감 | 없음 |

## 사진 목록

- [adventure-map.webp](images/adventure-map.webp) — 마을 선택과 최종 구출 진행률
- [collection-map.webp](images/collection-map.webp) — 마을별 카드 수집 현황
- [practice.webp](images/practice.webp) — 연습 퀴즈의 진행률·카드·문제·보기
- [battle-question.webp](images/battle-question.webp) — 전투 문제 제출 전 상대 HP·아이템·문제 영역
- [battle-feedback.webp](images/battle-feedback.webp) — 오답 직후 정답 안내와 자동 진행
- [battle-result.webp](images/battle-result.webp) — 전투 승리 후 카드·XP·코인 보상
- [rescue-dialogue.webp](images/rescue-dialogue.webp) — 최종 전투 승리 후 교수님 구출 대화
- [showcase-menu.webp](images/showcase-menu.webp) — F1에서 선택할 수 있는 시연 장면

전투 문제 사진과 오답 피드백 사진은 서로 다른 플레이 장면이다. 시연에서는 게임 창에서 F1을 열고 `첫 정답 · 공격 연출`, `승리 결과 · 보상 바로 보기`, `교수님 구출 · 대화`를 차례로 선택한다.

## 근거와 발표 전 확인

- 개발 과정은 팀이 설명한 브랜치 운영과 중간 평가 내용을 기준으로 작성했다. [분업 가이드](../reference/TEAM_MODULE_GUIDE.md), [회의 반영 기록](../PROFESSOR_BATTLE.md), Git 병합 이력을 함께 참고했다.
- 초기의 설계 선택은 팀 개발 과정의 회고로 설명한다. [ADR 0001](../adr/0001-core-and-domain-packages.md)은 최종 코어·기능 패키지 경계를, [ADR 0003](../adr/0003-classpath-game-resources.md)은 리소스 결정을 기록한다. [ADR 0002](../adr/0002-isolated-showcase.md)는 시연 격리 방식을 제안 상태로 기록한다.
- 팀원 이름, 실제 역할과 소감은 발표 전 팀 자료를 확인해 채운다.
