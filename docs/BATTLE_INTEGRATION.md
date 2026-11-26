# 1인 퀴즈 전투 연결 계약

팀 통합 데모의 계약이다. `runIntegration`은 `src/test/java/org/kgu/integration/IntegrationMain.java`에서 시작한다. 일반 게임의 화면과 시연 진입점은 [코드 탐색 가이드](ARCHITECTURE.md)에 있다.

## 진입과 종료

```java
BattleEngine engine = new BattleEngine(target, questions, requiredCorrect);
JPanel screen = new BattleScreen(engine, finished -> {
    // 이 콜백은 자동 진행 완료 시 한 번만 호출됩니다.
    // finished.won(): 성공 여부
    // finished.target(): 대상 인물 (ID/이름/분야/이미지 경로)
    // finished.current().difficulty(): 카드 단계
    // finished.correctCount(), finished.questionIndex(): 정답/실제 진행 수
    // finished.remainingHp(): 남은 HP
    // 여기서 팀의 결과 화면 및 보상 서비스로 전달하세요.
}, () -> {
    // 기존 GameNavigator를 통해 메뉴나 맵으로 이동하세요.
});
```

실제 화면은 기존 JFrame에 JPanel로 붙입니다. 배틀 화면이 새 JFrame을 만들거나 사용자 파일에 직접 저장하지 않습니다.

`target`은 교수님·과학자·아이돌·한국사 중 하나입니다. `questions`는 같은 인물·난이도의 문제 목록, 보기 4개, 정답 인덱스 0~3이며 중복 ID는 허용하지 않습니다. 적중 횟수 `requiredCorrect`는 1 이상 문제 수 이하여야 합니다. 기본 정책은 전체 문제 수의 2/3 올림이며 팀의 최종 규칙에 따라 호출자가 별도로 전달할 수 있습니다.

## 자동 진행

정답 타격 효과 종료 → 1초 표시 → 다음 문제/결과. 오답 → 2.4초 표시 → 다음 문제/결과. 메뉴 이탈·화면 제거 시 효과 타이머와 자동 진행 타이머를 중지합니다.

## 도감·결과 담당 연결

성공 시 `finished.target()`의 인물 한 명과 `finished.current().difficulty()`만 저장합니다. 연습 획득 내역과 스테이지 클리어 기록을 혼용하지 않습니다. 실제 화면 이동과 카드 저장은 호출자 책임입니다.

테스트 어댑터의 연결 예:

```java
CardSave.record(playerId, finished.target().category(),
                finished.target().name(), finished.current().difficulty());
```

`BattleResultScreen`은 로컬 동작 확인용 배틀 결과 UI입니다. 실제 카드 기록 함수는 Supplier<Boolean>으로 주입하고 실패 시 재시도합니다. 팀 결과 화면의 최종 API가 정해지면 완료 콜백을 그쪽으로 연결하면 됩니다.

## 이번 변경 경계

- main: 배틀 도메인과 배틀 UI 패키지만 변경.
- test: 실제 팀 코드와 분리된 GameSetupScreen, 데이터 로더, 화면 연결 어댑터 변경.
- 루트: 실행 배치, README, Gradle 테스트 설정 변경.
- 원본 팀원 저장소는 수정하지 않음.
- 별도 로컬 통합본의 GameSetupScreen/QuestionRepository 변경은 이 브랜치 PR에 포함되지 않음. 필요하면 해당 담당자와 공유.

PR에는 배틀 구현뿐 아니라 루트 실행 파일과 테스트 연결부 변경도 명시하세요. 기존 1대1 API는 제거했습니다. `BattleEngine(target, questions, requiredCorrect)`와 `BattleScreen`을 사용합니다. 원격 Commit/Push/PR은 아직 수행하지 않았습니다.

## 통합 담당자 전달 사항

- 현재 기능 브랜치의 준비·전투·결과 화면은 `BattleSetupScreen`, `BattleScreen`, `BattleResultScreen`입니다.
- 승리 판정은 완료된 `BattleEngine.won()`으로 조회합니다. 이전 1대1 생성자와 공격/방어 API는 더 이상 제공하지 않습니다.
- 테스트 연결 패널은 `org.kgu.integration.ui.battle.BattleFlowPanel`이며 배포용 공통 UI가 아닙니다. 테스트용 `BattleService`는 완료된 승리에서만 대상 카드 한 장을 저장합니다.
- 이 변경은 명칭 통일과 이전 전투 구현 정리입니다. `BattleScreen`의 이벤트/자동 진행 분리와 결과 화면의 저장 요청 분리는 후속 리팩토링 대상입니다. 새 아키텍처에 완전히 통합된 상태를 의미하지 않습니다.
- 통합 경로는 `feature/battle-screen` → `chore/new-architecture`이며, 전체 통합·검증 전에는 `develop`에 신규 기능을 병합하지 않습니다.
