# 최종 발표 자료

WHO’S NEXT? 객체지향프로그래밍 발표. 현재 로컬 코드와 최신 게임 규칙을 기준으로 전체 원고를 다시 작성했다. PPT 장식·템플릿 지시는 포함하지 않는다.

- [FINAL_PRESENTATION.md](FINAL_PRESENTATION.md): 페이지별 발표 내용과 코드 근거.
- [index.html](index.html): 같은 내용을 사진과 함께 읽는 HTML 예시. 브라우저에서 직접 열 수 있다.
- `images/`: 발표용 캡처. 기존 파일은 보존하며 최신 여부를 아래에서 구분한다.

## 페이지 구성

| 페이지 | 주제 | 핵심 내용 | 사진 |
|---|---|---|---|
| 01 | 프로젝트 소개 | 학생·교수님 구출, 기획 중점, 차별화 | 필수 없음 |
| 02 | 개발 과정 | MVP → 구조 예시 → 중간 평가 → 최종 정리 | 필수 없음 |
| 03 | 전체 코드 | core/domain, 실제 운영 전투와 통합 데모 경로 | 필수 없음 |
| 04 | 기능 실행 흐름 | View·Event·Feature, DI, MVC 대신 선택한 경계 | 필수 없음 |
| 05 | 객체지향 | 캡슐화·추상화·다형성·객체 조합·상태 생명주기 | 필수 없음 |
| 06 | SOLID | 다섯 원칙의 한글 명칭과 실제 코드 사례 | 필수 없음 |
| 07 | ADR | 문제와 결정 이유, 객체 책임의 변화, 승인 상태 | 필수 없음 |
| 08 | 빌드 | Gradle·Wrapper·classpath·이식성, Maven과 비교 | 필수 없음 |
| 09 | 데이터·화면 흐름 | 80명·1,200문제·240장, 저장과 화면 이동 | 마을 탐험·도감 지도 |
| 10 | 연습·복습 | 화면 구조·출제·기록·서로 다른 보상 정책 | 연습·연습 결과·복습 결과 |
| 11 | 배틀·보스 | 1·2·3페이즈, 통과 조건·HP·해금·연출 분리 | 일반 배틀·마을 보스·최종 교수님 |
| 12 | 카드·지원·결과 | 네 성향, 단계별 NEW, 저장·중복 정산 방지 | 도감 목록·획득 레이어·정산 결과 |
| 13 | 스토리·F1 | 최종 구출·엔딩, 실제 기능을 쓰는 격리 시연 | 구출 대화·엔딩·F1 메뉴 내용 |
| 14 | UI/UX 개편 | 디자인 시스템·공용 컴포넌트·기존 이미지 재사용 | `tutorial-current.png` 포함 |
| 15 | 팀 운영 | 변형 Git Flow, develop, 역할 분담·회의·PR | 필수 없음 |
| 16 | 자체 평가 | 성과·개선점·설계에서 배운 점·실제 팀원 소감 | 필수 없음 |

## 반드시 유지할 사실

- 연습만 보상·성장 차단 대상이다. 오답 복습의 기존 보상·성장 처리는 유지한다.
- 카드는 배틀 승리로만 획득한다. 연습·복습에서 카드를 지급한다고 설명하지 않는다.
- 일반 전투는 5문제, 마을 보스는 10문제, 최종 교수님은 15문제다.
- 교수님 해금은 타 분야의 서로 다른 문제 30개 정답이며 연습은 기여하지 않는다.
- core는 공통 실행 기반, domain은 실제 게임플레이·사용자 기능이다. 일반 전투 엔진은 `domain/game/feature`에 있다.
- SRP (단일 책임 원칙)처럼 모든 SOLID 원칙에 한글 뜻을 함께 쓴다.
- `develop`은 팀이 설명한 기본·통합 브랜치다. 현재 최종 결합 대상 `feat/final`과 구분한다.
- ADR-0001·0003은 Accepted, ADR-0002·0004는 Proposed다. 승인 상태를 바꾸지 않았다.

## 현재 사진 목록 — 2026-11-27 재촬영

현재 생산 코드와 classpath 이미지를 사용해 오프스크린으로 15장을 새로 촬영했다. 기존에 포함된 배틀·튜토리얼 사진도 교체했다. 아래 사진을 원고와 HTML에 모두 연결했다.

| 페이지 | 파일명 | 담은 장면·강조점 | 촬영 상태 |
|---|---|---|---|
| 09 | [adventure-map-current.png](images/adventure-map-current.png) | 마을·보스·최종 구출 경로와 도전 준비 | 재촬영 완료 |
| 09 | [collection-map-current.png](images/collection-map-current.png) | 기존 지도 이미지와 현재 공용 버튼 | 재촬영 완료 |
| 10 | [practice-current.png](images/practice-current.png) | 1/10 진행·보기 선택·보상 없음 안내 | 재촬영 완료 |
| 10 | [practice-result-current.png](images/practice-result-current.png) | 9/10 정답·보상 없음·다시 연습 | 재촬영 완료 |
| 10 | [review-result-current.png](images/review-result-current.png) | 복습 1문제 정답·XP 6·코인 4, 카드 지급 없음 | 정산 완료 상태 |
| 11 | [battle-current.png](images/battle-current.png) | 일반 배틀의 목표·HP·인물·아이템·질문 | 재촬영 완료 |
| 11 | [boss-phase-current.png](images/boss-phase-current.png) | 마을 보스 2페이즈·새 배경·정답 4개·HP 80 | 재촬영 완료 |
| 11 | [final-phase-current.png](images/final-phase-current.png) | 최종 교수님 3페이즈와 전용 배경 | 재촬영 완료 |
| 12 | [collection-new-current.png](images/collection-new-current.png) | 전시실 배경·보유 카드·카드별 NEW | 재촬영 완료 |
| 12 | [card-acquisition-current.png](images/card-acquisition-current.png) | 신규 카드 공개·지원 능력·즉시 장착 | 공개 완료 상태 |
| 12 | [battle-result-current.png](images/battle-result-current.png) | 카드·XP 150·코인 90·다음 행동 | 정산 완료 상태 |
| 13 | [rescue-dialogue-current.png](images/rescue-dialogue-current.png) | 최종 교수님 구출 대화 | 첫 대사 표시 완료 상태 |
| 13 | [ending-current.png](images/ending-current.png) | 새 캠퍼스 배경·GAME CLEAR·계정 통계 | 재촬영 완료 |
| 13 | [showcase-menu-current.png](images/showcase-menu-current.png) | F1 메뉴의 보스·엔딩 항목 | 실제 메뉴 내용 영역 |
| 14 | [tutorial-current.png](images/tutorial-current.png) | 연습 성장 차단·배틀 및 복습 보상 안내 | 성장 안내 페이지 재촬영 |

게임 화면은 1280×960 PNG다. F1은 실제 `ShowcaseLayout`과 `JOptionPane`의 권장 크기인 472×622로 내용 영역을 촬영했으며 운영체제 창 테두리는 포함하지 않는다. 이미지를 누르면 원본 PNG를 볼 수 있다.

별도 임시 프로필에서 실제 문제·전투·정산 객체를 진행했다. 연습 종료의 XP 0·코인 120·카드 0을 확인했고, 복습과 배틀 결과는 정산을 완료한 뒤 촬영했다. 원본 배경·카드 자산을 편집하거나 사진 위에 UI를 다시 그리지 않았다. 네이티브 창 조작과 연속 애니메이션 체감까지 확인했다는 뜻은 아니다.

### 보존한 이전 사진

기존 파일 8개는 삭제·덮어쓰기하지 않았다. 현재 HTML의 최종 화면 예시에서는 제외했다.

| 파일 | 사용 시 주의 |
|---|---|
| [adventure-map.webp](images/adventure-map.webp) | 이전 다크·골드 UI. `adventure-map-current.png`로 대체 |
| [collection-map.webp](images/collection-map.webp) | `collection-map-current.png`로 대체 |
| [practice.webp](images/practice.webp) | 폐기한 ‘정답이면 카드 획득’ 안내가 있으므로 최종 화면으로 사용 금지 |
| [battle-question.webp](images/battle-question.webp) | 현재 배틀 사진 `battle-current.png`로 대체 |
| [battle-feedback.webp](images/battle-feedback.webp) | 이전 피드백 캡처. 현재 효과와 비교 후 사용 |
| [battle-result.webp](images/battle-result.webp) | `battle-result-current.png`로 대체 |
| [rescue-dialogue.webp](images/rescue-dialogue.webp) | `rescue-dialogue-current.png`로 대체 |
| [showcase-menu.webp](images/showcase-menu.webp) | `showcase-menu-current.png`로 대체 |

## 근거와 남은 팀 확인

- 기능 설명은 [게임 규칙](../GAMEPLAY.md), [최종 보완 규칙](../POLISH_GAMEPLAY.md), 실제 생산 코드를 대조했다. 이후 변경한 연습 규칙을 우선한다.
- 구조는 [코드 탐색 가이드](../ARCHITECTURE.md)와 각 ADR을 기준으로 설명한다. 기존 ADR의 내용·상태는 수정하지 않았다.
- 개발 과정은 팀이 제공한 설명을 기준으로 썼다. 과거 분업 문서는 당시 기록이며 현재 패키지 안내로 사용하지 않는다.
- Gradle과 Maven의 비교는 현재 빌드 설정에 맞는 선택 이유다. 확인되지 않은 당시 도구 비교 회의를 만들어 쓰지 않았다. 공식 기술 문서는 원고 08페이지에 연결했다.
- 팀원 실명별 역할, 회의 횟수, 개인 소감은 확인된 자료가 없어 만들지 않았다. 최종 발표 전 팀원별 담당과 실제 소감 한 문장을 확정한다.
- 발표 문서 수정으로 게임 코드·이미지 원본·테스트 코드·주석은 변경하지 않는다. 전체 기능·모든 운영체제 검증이 끝났다는 표현도 사용하지 않는다.

## 시연 순서

게임 창에서 F1 → 연습 문제 → 첫 정답 공격 → 마을 보스 → 승리 결과 → 교수님 구출 → GAME CLEAR 순으로 선택한다. 연습을 끝까지 진행할 경우 성적과 보상 없음 안내를 확인한다. 실제 계정과 시연 프로필이 분리된다는 점을 함께 설명한다.

HTML은 원고와 같은 16페이지 순서다. 페이지 목차, 코드·문서 링크, 포함 사진을 제공하며 출력 시 페이지 구분을 유지한다. 내용이 바뀌면 원고와 HTML을 함께 갱신한다.
