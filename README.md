# WHO’S NEXT? — 배틀 페이지

같은 `feature/battle-screen` 브랜치 안에서 실제 배틀 구현과 통합 확인 코드를 분리했습니다.

- `src/main/java/org/kgu/battle`: 전투 상태, 인물, 문제 모델.
- `src/main/java/org/kgu/ui/battle`: 배틀 페이지와 그리기·입력 처리.
- `src/main/resources/org/kgu/battle`: 배틀 배경.
- `src/test/java/org/kgu/integration`: 팀 화면 참고본, 테스트용 화면 전환·문제 로더·카드 저장, 수동 실행 진입점.
- `src/test/java/org/kgu/battle`: 배틀 자동 테스트.
- `src/test/resources/integration`: 통합 확인용 문제·카드 그림·샘플 저장 자료.

기존 Main/Manager 등 초기 기본 파일은 원래 브랜치 내용으로 유지합니다.
시작·문제·도감·정답 담당 코드는 main에 추가하지 않았습니다.
테스트용 참고본은 `org.kgu.integration` 패키지여서 나중에 실제 팀 클래스와 이름이 충돌하지 않습니다.

## Windows PowerShell에서 통합 화면 실행

```powershell
cd "C:\Users\metan\Onedrive\Desktop\객체지향_팀프로젝트\kgu-quiz-game"
.\run.bat
```

같은 폴더에서 Gradle로 직접 실행할 수도 있습니다.

```powershell
.\gradlew.bat runIntegration
```

JDK를 찾지 못하면 설치 경로를 지정합니다. 현재 컴퓨터에서 확인한 경로:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2.1"
.\run.bat
```

`run.bat`은 `runIntegration`을 호출합니다. 테스트 자료와 카드 이미지는 자동으로 준비합니다.
처음에는 Gradle 및 테스트 의존성 다운로드를 위한 인터넷이 필요합니다.
기존 `gradlew run`은 초기 Main의 Hello, World! 실행이므로 통합 확인에는 사용하지 않습니다.

## 배틀 화면 구현

자료4의 battle_design.png와 campus_background.png를 기준으로 1280×960 화면을 구성했습니다.
카드 패널, HP JProgressBar, 보기 JToggleButton 4개, 힌트/제출/다음 JButton, 문제·해설 텍스트가 각각 독립 컴포넌트입니다.
작은 화면에서는 컴포넌트 위치와 크기를 함께 조정합니다. 전투 모델과 화면은 분리되어 있습니다.

문제별 힌트·해설은 BattleQuestion의 값을 표시합니다. 현재 팀 데이터에는 해당 필드가 없어
통합 확인본에서는 미등록 안내를 표시합니다. 인물 소개를 문제별 힌트로 가정하지 않습니다.
정식 연습 모드의 문제 서비스와 이미지 공급 API 연결은 팀과 계약 확정이 필요합니다.
현재 test 어댑터는 제공된 팀 데이터로 흐름을 확인하는 용도입니다.

## 확인 순서

새 ID 입력 → 게임 시작 → 분야·난이도·대진 선택 → 인물 선택 → 6문제 진행 → 결과 → 도감 확인.
양쪽 HP 60, 공격 정답 시 상대 HP -20, 방어 오답 시 내 HP -20입니다.
6턴 후 승리하면 테스트용 결과 화면이 두 인물의 해당 난이도 카드를 기록합니다.
배틀 본체는 결과를 콜백으로 전달할 뿐 파일 저장이나 결과 화면 이동을 직접 하지 않습니다.

수동 실행의 저장 기록은 `.integration-runtime/saves/{ID}.txt`에 있으며 Git에서 제외됩니다.
자동 테스트는 `build/battle-test-runtime`을 사용하여 수동 실행의 기록을 건드리지 않습니다.
`clean`은 수동 실행의 저장 기록을 지우지 않습니다.
원본 자료2·자료3 폴더는 수정하지 않았습니다.

## 자동 검증

```powershell
.\gradlew.bat clean build
```

- 테스트 보고서: `build/reports/tests/test/index.html`
- 렌더링 미리보기: `build/battle-preview.png`
- 창 없이 통합 실행 진입점 확인: `.\gradlew.bat runIntegration --args="--smoke"`

일반 JAR에는 main만 들어가며, 통합 확인 화면이나 도감 데이터는 포함되지 않습니다.

## 팀과 합칠 때

[배틀 연결 계약과 담당 범위](docs/BATTLE_INTEGRATION.md)를 참고하세요.
이 브랜치는 별도 테스트 브랜치를 만들지 않고 main/test 소스 폴더로 구분합니다.

## 배틀 화면만 바로 확인하기

WSL에서 프로젝트 폴더로 이동한 뒤 `cmd.exe /c run-battle.bat`을 실행합니다.
로그인·준비 화면 없이 교수님/중 난이도의 배틀이 바로 열립니다.
창 제목은 `배틀 단독 확인 · Swing 컴포넌트 버전`이며, 이 미리보기에서는 도감 기록을 저장하지 않습니다.
프로젝트에 내려받은 `.gradle-user-home`을 명시적으로 사용합니다.
