# 화면 모듈 분업 가이드

## 담당 파일 경계

| 담당 | 수정 가능한 기본 패키지 | 화면 진입점 |
|---|---|---|
| 배틀 | `org.kgu.ui.battle` | `BattleScreen` |
| 문제 | `org.kgu.ui.question` | `QuestionScreen` |
| 정답/결과 | `org.kgu.ui.answer` | `AnswerResultScreen` |
| 카드 도감 | `org.kgu.ui.collection` | `CollectionScreen`, `CardSliderPanel` |

각 담당자는 원칙적으로 자기 패키지 안에서만 새 클래스와 화면 코드를 추가한다.

## 공용 영역

- `org.kgu.ui.navigation.GameNavigator`: 화면 전환 등록만 담당한다. 새 화면을 연결할 때만 수정한다.
- `org.kgu.ui.common`: 공통 버튼과 색상/폰트 도구다. 전체 UI에 영향이 있으므로 합의 후 수정한다.
- `org.kgu.game.PlayerSession`: ID, 분야, 난이도, 모드 등 화면 간 선택값을 보관한다.
- `org.kgu.game.GameOutcome`: `CORRECT`, `INCORRECT` 결과를 정답 화면으로 전달한다.

## 화면 전환 규칙

화면 클래스에서 새 `JFrame`을 만들지 않는다. 기존 `GameNavigator`를 사용한다.

```java
navigator.showQuestion();
navigator.showBattle();
navigator.showAnswerResult(GameOutcome.CORRECT);
navigator.showCollection();
navigator.showMenu();
```

## 데이터·UI 분리

크롤링, 문제 선택, 카드 저장은 화면 클래스에 직접 작성하지 않는다. 각 담당자는 필요하면 자기 도메인 패키지를 추가한다.

```text
org.kgu.question      // Question, QuestionService, QuestionRepository
org.kgu.battle        // BattleService, BattleQuestion
org.kgu.collection    // Card, CardRepository
```

UI 클래스는 서비스에서 받은 데이터를 표시하고 버튼 이벤트를 전달하는 역할만 맡는다.

## Git 규칙

- 배틀: `feature/battle-screen`
- 문제: `feature/question-screen`
- 정답: `feature/answer-result-screen`
- 도감: `feature/collection-screen`

각 브랜치는 자기 담당 패키지 위주로 변경하고 `develop`으로 Pull Request를 보낸다. 공용 파일을 수정했다면 PR 설명에 반드시 적는다.
