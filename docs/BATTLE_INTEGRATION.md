# 배틀 페이지 연결 계약

## 이번 구조 정리

`feature/battle-screen` 브랜치 안에서 source set을 분리했습니다.
별도 test 브랜치를 만들거나 원격 저장소를 수정하지 않았습니다.

### main: 배틀 담당 구현

- `org.kgu.battle.BattleEngine`: 6턴, HP, 공격/방어 판정, 승패, 보상 대상 ID.
- `BattleCharacter`, `BattleQuestion`: 화면과 전투에 필요한 입력 데이터.
- `org.kgu.ui.battle.BattleScreen`: 외부에서 준비된 전투를 받아 표시하는 진입점.
- `BattlePanel`: 그리기, 보기 선택, 제출, 다음 턴, 정보 표시, 메뉴 이벤트.
- 배틀 배경은 main resources에 포함되어 JAR에서도 읽을 수 있습니다.

배틀 본체에서 로그인 세션, GameNavigator, 도감 API, 파일 문제 로더, 결과 화면을 참조하지 않습니다.
원래 브랜치의 Main과 mgr 파일은 HEAD의 내용으로 복구하여 팀 공용 코드 변경을 제거했습니다.

### test: 통합 확인 전용

- `org.kgu.integration.IntegrationMain`: 수동 실행 시작점.
- `org.kgu.integration.ui.*`: 자료3의 시작/도감/문제/정답 화면 참고본과 테스트용 전환 관리자.
- `org.kgu.integration.ui.battle`: 테스트용 인물 선택 및 배틀 결과 화면.
- `org.kgu.integration.collection`: 자료3의 도감 저장·조회 참고본.
- `org.kgu.integration.battle`: 파일 문제 로더, 무작위 상대 선정, 보상 저장 어댑터.
- `src/test/resources/integration`: 팀 데이터·카드 ZIP·샘플 저장 기록.

테스트 참고본에 별도 integration 패키지를 사용했습니다.
나중에 `org.kgu.collection` 등 실제 팀 구현을 합쳐도 동일한 클래스 이름이 겹치지 않습니다.
테스트용 결과 화면과 저장 동작은 기능을 확인하기 위한 예시이며 결과 담당자의 최종 구현을 대체하지 않습니다.
원본 자료3의 가이드와 파일 출처 목록은 `docs/reference`에 보존했습니다.
분리 전 소스·문서 백업은 저장소 밖 `../battle-before-test-split.zip`입니다.

## 실제 팀 코드에서 호출하는 방법

문제/설정 담당 코드가 선택한 인물과 문제를 전달합니다.
각 인물에 해당하는 같은 난이도 문제를 3개 이상 준비합니다.
문제 데이터 파일의 정답 번호가 1~4라면 BattleQuestion의 answerIndex는 1을 빼서 0~3으로 변환합니다.

```java
BattleEngine engine = new BattleEngine(
    playerCharacter, opponentCharacter, playerQuestions, opponentQuestions);

JPanel screen = new BattleScreen(
    engine,
    finished -> {
        // 결과 담당 화면으로 전달: finished.result(), playerHp(), computerHp(),
        // correctCount(), player(), computer(), rewardIds() 등.
        // 실제 팀의 화면 전환 API를 여기서 호출합니다.
    },
    navigator::showMenu
);
```

GameNavigator가 위 패널을 기존 창에 넣으면 됩니다. 새 JFrame을 만들지 않습니다.
기존 임시 화면의 `BattleScreen(GameNavigator)` 생성자와는 다릅니다.
최종 통합 시 Navigator의 배틀 화면 생성 부분을 이 입력·콜백 계약에 맞추면 됩니다.
배틀 종료 콜백은 6턴을 완료한 시점에 호출되고, 중간 메뉴 이동은 onExit만 호출합니다.

## 결과·보상 담당자에게 전달할 내용

자료2 규칙을 유지했습니다: HP 60, 피해 20, 6턴을 모두 진행, 승리 시 두 인물 카드 보상.
팀 최종 규칙과 다르면 전투 모델과 테스트를 함께 수정합니다.

보상 저장 책임은 최종 결과 담당자가 가져가면 됩니다. 예시:

```java
if (finished.result() == BattleEngine.Result.WIN) {
    for (BattleCharacter person : List.of(finished.player(), finished.computer())) {
        CardSave.record(session.getPlayerId(), person.category(), person.name(),
                        finished.current().difficulty());
    }
}
```

배틀 본체에서는 CardSave를 호출하지 않습니다. 결과 화면의 저장 호출과 중복되지 않습니다.
현재 test의 BattleService가 위 연동을 시연하며 중복 방지와 부분 실패 후 재시도를 검사합니다.
인물 그림은 BattleCharacter.imagePath에 실제 팀의 그림 파일 경로를 전달합니다.
현재 BattlePanel의 인물 그림 로더는 파일 경로를 사용하며, 배경만 클래스패스 리소스입니다.

## 문제 담당자에게 전달할 내용

현재 test 어댑터는 자료3의 하/중/상, 과학자/교수님/한국사/아이돌 데이터를 사용합니다.
기존 문제 브랜치의 getQuestions는 인물당 한 문제만 반환하므로,
실제 연동에서는 분야·난이도·인물별 문제 3개를 제공할 API가 필요합니다.
팀 API의 실제 타입을 BattleQuestion으로 바꾸는 코드는 통합 지점에서 작성합니다.

## 실행과 저장 위치

- 수동 확인: `run.bat` 또는 `gradlew.bat runIntegration`.
- 수동 runtime: `.integration-runtime` (Git 제외, clean 이후에도 보존).
- 자동 테스트 runtime: `build/battle-test-runtime` (개인 기록과 분리).
- 일반 build/JAR: main만 포함. 테스트용 시작·도감·결과 클래스는 포함되지 않음.
- 기존 `gradlew run`은 초기 Main 실행이므로 통합 화면 확인 명령이 아님.

## Git에 올릴 때

같은 브랜치에서 main은 배틀 구현, test는 재현·통합 확인 자료라고 PR에 설명하면 됩니다.
main의 타 담당 패키지와 공용 화면 전환 파일을 수정하지 않습니다.
테스트용 팀 자료가 포함되어 있으므로 PR 설명에 자료3 출처를 명시하세요.
카드 ZIP 약 62MB는 원본 팀 제공 자료이며 테스트 리소스에 있습니다.
현재 커밋·push·merge는 실행하지 않았습니다.

## 자료4 디자인 반영

- 배틀 기본 크기를 1280×960으로 변경하고 원본 campus_background.png를 사용합니다.
- 좌표 클릭 판정을 없애고 실제 Swing 버튼과 ButtonGroup으로 선택/제출/잠금을 처리합니다.
- HP는 JProgressBar와 Timer로 표시만 보간하고, 실제 HP는 BattleEngine에서 한 번만 변경합니다.
- 화면 제거/중단 시 타이머를 정지하고 최종 결과 콜백은 한 번만 호출합니다.
- 배틀 하단에는 힌트/제출/다음 버튼만 배치합니다. 보상 안내는 테스트 결과 화면이 담당합니다.
- 팀 데이터에 없는 hint/explanation은 비워 두고 미등록 상태를 표시합니다. 정답은 항상 표시합니다.
- 도감/시작/정답 팀원의 원본 구현은 수정하지 않았습니다. 테스트용 배틀 준비 화면과 문제 변환만 조정했습니다.
