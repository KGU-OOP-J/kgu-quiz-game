# 배경·사운드 리소스 매핑

리소스는 classpath에서 읽는다. 실제 파일을 제작하거나 다운로드하지 않았다. 파일 추가 후 Gradle 리소스 처리와 앱 재실행으로 반영한다.

## 배경

배경 탐색·디코딩은 `GameResources`의 전용 스레드에서 한 번 수행한다. 성공·실패를 캐시하며, 실패 시 화면 식별자·파일명·전체 경로를 WARNING으로 한 번 출력한다. 원래 이미지를 사용하던 화면은 기존 파일 경로를 명시해 유지한다. 신규 배경이 없는 신규 화면은 기존 단색을 출력한다. 이미지는 Cover로 표시한다.

일반 실행에서는 초기 데이터와 카드 이미지를 메인 스레드에서 준비하고 화면 등록·조작은 EDT로 넘긴다. 현재 카드 PNG 헤더 기준 전체 디코딩 크기는 약 186MiB다. 선택적 초상화·마을·성향 이미지는 초기 캐시를 준비하고, 나머지 선택적 이미지는 비동기 캐시를 사용한다. 배경은 완료된 Future를 게시한 뒤 repaint를 요청한다.

| 화면 식별자 | 전체 classpath 경로 | 현재 상태 |
|---|---|---|
| `login` | `/org/kgu/login/main_login.png` | 존재 |
| `menu` | `/org/kgu/battle/campus_background.png` | 존재 · 기존 이미지 유지 |
| `practice-setup` | `/org/kgu/battle/campus_background.png` | 존재 · 기존 이미지 유지 |
| `study` | `/images/question-background.png` | 존재 · 기존 이미지 유지 |
| `review` | `/images/question-background.png` | 존재 · 기존 이미지 유지 |
| `adventure` | `/org/kgu/battle/campus_background.png` | 존재 · 기존 이미지 유지 |
| `shop` | `/org/kgu/art/scene-shop.png` | 존재 · Gradle에서 docs/design-system 자산 포함 |
| `guild` | `/org/kgu/art/scene-guild.png` | 존재 · Gradle에서 docs/design-system 자산 포함 |
| `collection` | `/org/kgu/art/scene-collection.png` | 존재 · 도감 목록 오프스크린 렌더링 확인, 지도는 기존 이미지 유지 |
| `tutorial` | `/org/kgu/art/scene-tutorial.png` | 존재 · Gradle에서 docs/design-system 자산 포함 |
| `settings` | `/org/kgu/art/scene-settings.png` | 존재 · Gradle에서 docs/design-system 자산 포함 |
| `battle` | `/org/kgu/battle/campus_background.png` | 존재 · 기존 이미지 유지 |
| `boss-phase-1` | `/org/kgu/art/scene-boss-phase-1.png` | 존재 · 오프스크린 렌더링 확인 |
| `boss-phase-2` | `/org/kgu/art/scene-boss-phase-2.png` | 존재 · 전환·오프스크린 렌더링 확인 |
| `final-phase-1` | `/org/kgu/art/scene-final-phase-1.png` | 존재 · 오프스크린 렌더링 확인 |
| `final-phase-2` | `/org/kgu/art/scene-final-phase-2.png` | 존재 · 전환·오프스크린 렌더링 확인 |
| `final-phase-3` | `/org/kgu/art/scene-final-phase-3.png` | 존재 · 전환·오프스크린 렌더링 확인 |
| `result` | `/org/kgu/result/result_background.png` | 존재 · 기존 이미지 유지 |
| `ending` | `/org/kgu/art/scene-ending.png` | 존재 · 오프스크린 렌더링 확인 |
| `prologue` | `/org/kgu/result/result_background.png` | 존재 · 기존 이미지 유지 |
| `dialogue` | `/org/kgu/result/result_background.png` | 존재 · 기존 이미지 유지 |

로그인은 기존 지정 자산 `main_login.png`를 사용한다. `scene-login.png`로 바꾸지 않는다. 프롤로그·구출 대사·결과는 기존 결과 배경을, 연습·복습은 기존 문제 배경을 유지한다. 일반 전투의 기존 캠퍼스 배경은 보스 전투에도 기본 배경으로 유지하고, 전용 페이즈 이미지가 있을 때만 그 위에 전환한다. 마을 지도와 인물 카드는 게임 콘텐츠 자산을 그대로 사용한다.

## 사운드

사운드 설정이 켜져 있을 때만 탐색·재생한다. 비동기 로딩 후 디코딩 데이터를 캐시한다. 동시 재생은 최대 12개이며 재생 완료·설정 해제·앱 종료 시 Clip을 닫는다. 누락·손상·재생 불가 시 한 번 경고하고 생략한다. 기본 비프음은 사용하지 않는다.

| 이벤트 | 전체 classpath 경로 | 현재 상태 |
|---|---|---|
| `UI_HOVER` | `/org/kgu/audio/ui-hover.wav` | 누락 · 무음 |
| `UI_CLICK` | `/org/kgu/audio/ui-click.wav` | 누락 · 무음 |
| `UI_CONFIRM` | `/org/kgu/audio/ui-confirm.wav` | 누락 · 무음 |
| `UI_CANCEL` | `/org/kgu/audio/ui-cancel.wav` | 누락 · 무음 |
| `UI_TRANSITION` | `/org/kgu/audio/ui-transition.wav` | 누락 · 무음 |
| `CORRECT` | `/org/kgu/audio/quiz-correct.wav` | 누락 · 무음 |
| `WRONG` | `/org/kgu/audio/quiz-wrong.wav` | 누락 · 무음 |
| `HINT` | `/org/kgu/audio/quiz-hint.wav` | 누락 · 무음 |
| `ATTACK` | `/org/kgu/audio/battle-attack.wav` | 누락 · 무음 |
| `HEAVY_ATTACK` | `/org/kgu/audio/battle-heavy-attack.wav` | 누락 · 무음 |
| `COMBO` | `/org/kgu/audio/battle-combo.wav` | 누락 · 무음 |
| `FINISHER` | `/org/kgu/audio/battle-finisher.wav` | 누락 · 무음 |
| `BOSS_ENTRANCE` | `/org/kgu/audio/boss-entrance.wav` | 누락 · 무음 |
| `PHASE_TRANSITION` | `/org/kgu/audio/boss-phase-transition.wav` | 누락 · 무음 |
| `CARD_FLIP` | `/org/kgu/audio/card-flip.wav` | 누락 · 무음 |
| `CARD_REVEAL_1` | `/org/kgu/audio/card-reveal-1.wav` | 누락 · 무음 |
| `CARD_REVEAL_2` | `/org/kgu/audio/card-reveal-2.wav` | 누락 · 무음 |
| `CARD_REVEAL_3` | `/org/kgu/audio/card-reveal-3.wav` | 누락 · 무음 |
| `COIN` | `/org/kgu/audio/reward-coin.wav` | 누락 · 무음 |
| `XP` | `/org/kgu/audio/reward-xp.wav` | 누락 · 무음 |
| `LEVEL_UP` | `/org/kgu/audio/reward-level-up.wav` | 누락 · 무음 |
| `UNLOCK` | `/org/kgu/audio/progression-unlock.wav` | 누락 · 무음 |
| `VICTORY` | `/org/kgu/audio/battle-victory.wav` | 누락 · 무음 |
| `DEFEAT` | `/org/kgu/audio/battle-defeat.wav` | 누락 · 무음 |

## 현재 누락 목록

위 표의 `누락` 행이 현재 배치가 필요한 파일 목록이다. WAV·PNG를 지정 경로에 추가하면 된다. UI에 리소스 누락 팝업은 표시하지 않는다.

UI 버튼은 클릭·호버, 화면은 전환, 퀴즈는 정답·오답·힌트, 전투는 공격·콤보·마무리·승패·보스 진입·페이즈, 카드는 뒤집기·단계별 공개, 결과는 XP·코인·레벨업에 연결한다. 확인·취소·해금용 파일 경로도 지정되어 있으며 개별 동작에서 사용한다.
