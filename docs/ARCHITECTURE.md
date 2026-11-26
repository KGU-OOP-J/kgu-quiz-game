# 코드 탐색 가이드

실행은 `org.kgu.Main`에서 시작한다. `core.application.Application`이 리소스와 세션을 준비하고 화면을 등록한다. `core.window.MainWindow`는 `ViewId`에 해당하는 화면을 열고 진입·이탈 이벤트를 호출한다.

`org.kgu.core`에는 창, 공용 Swing 렌더링, 리소스와 저장 어댑터, 여러 기능이 공유하는 모델이 있다. `org.kgu.domain`의 패키지는 사용자 기능을 기준으로 찾는다. 화면 입력은 `event`, 흐름과 상태 처리는 `feature`, 화면 구성은 `view`에서 찾는다. 컨테이너와 섹션 배치는 `view/layout`에 있고, 화면에서 재사용하는 카드, 버튼, 진행 막대, 애니메이션은 `view/component`에 있다. 여러 화면이 공유하는 디자인 토큰과 기본 페인터는 `core/ui`에 있다.

## 화면별 진입점

| 화면 | 등록 위치 | View → Event → Feature |
|---|---|---|
| 프롤로그 | `Application.start` | `story.view.PrologueView` → `PrologueGUIEvent` / `PrologueLifecycleEvent` → `PrologueFeature` |
| 로그인 | `Application.start` | `login.view.LoginView` → `LoginGUIEvent` → `LoginFeature` → `SaveFactory` |
| 메인과 게임 준비 | `Application.registerPlayer` | `menu.view.MenuView` → `MenuGUIEvent` → `MenuFeature` → `GameFeature` |
| 모험·상점·도전과제·덱·오답 노트 | `Application.registerPlayer` | `adventure.view.AdventureView` → `AdventureGUIEvent` → `AdventureFeature` → `GameFeature` / `ProgressStore` |
| 튜토리얼 | `Application.registerPlayer` | `adventure.view.TutorialView` → `AdventureFeature.completeTutorial` |
| 연습·전투 | `Application.registerPlayer` | `game.view.QuestionView` / `BattlePanel` → `QuestionGUIEvent` / `BattleGUIEvent` → `GameFeature` → `GameSession` / `BattleEngine` |
| 결과·보상 | `Application.registerPlayer` | `game.view.ResultView` → `ResultGUIEvent` → `GameFeature.saveRewards` → `SaveFactory` / `ProgressStore` |
| 구출 대화 | `Application.registerPlayer` | `story.view.DialogueView` → `DialogueGUIEvent` → `DialogueFeature` → `GameFeature.finishDialogue` |
| 카드 도감 | `Application.registerPlayer` | `collection.view.CollectionView` → `CollectionGUIEvent` → `CollectionFeature` |
| 설정 | `Application.registerPlayer` | `setting.view.SettingView` → `SettingGUIEvent` → `SettingFeature` → `SettingFactory` |
| 장면 시연 | `MainWindow.bindShowcase` | `ShowcaseView` → `Application.launchShowcase` → `ShowcaseFeature` → 일반 화면 등록 경로 |

운영 클래스 경로는 `src/main/java/org/kgu`를 기준으로 한다. 팀 통합 데모의 진입점은 `src/test/java/org/kgu/integration/IntegrationMain.java`이며 Gradle의 `runIntegration`으로 실행한다. 이 데모에서 사용하는 전투 경로는 `domain.battle`에서 찾을 수 있다. 과거 분업과 화면 담당 문서는 `docs/reference/`에 보관한다.

## 리소스와 시연

- 문제 데이터는 `src/main/resources/questions`, 카드와 지도·아이콘은 `src/main/resources/images`, 프롤로그와 화면별 배경은 `src/main/resources/org/kgu`에 있다. 화면은 `ImageManager`에 등록된 논리 키로 카드를 조회한다.
- F1은 로그인 전에도 사용할 수 있다. 장면을 선택하고 Enter를 누르면 별도 창에 열린다. 전투 시연은 실제 전투 판정과 타이머를 사용한다. 시연 창에서 F1을 누르면 장면을 교체할 수 있다.
- 게임 저장은 `saves/`, 설정은 `settings.properties`에 기록된다. 시연 저장과 설정은 운영체제 임시 디렉터리를 사용한다.
- 시각 기준과 Swing 매핑은 [디자인 시스템](design-system/index.html#implementation)에 있다.

## 결정 기록

- [ADR 0001 — 코어와 기능 패키지 경계](adr/0001-core-and-domain-packages.md)
- [ADR 0002 — 시연 데이터 격리](adr/0002-isolated-showcase.md)
- [ADR 0003 — 읽기 전용 게임 데이터를 classpath로 로드](adr/0003-classpath-game-resources.md)
