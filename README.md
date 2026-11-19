# WHO’S NEXT? — 1인 퀴즈 전투

`feature/battle-screen`의 배틀 담당 구현입니다. 대학교 4학년인 플레이어가 여러 분야의 인물에게 도전하며 기억을 잃은 교수님을 찾는 방향을 반영했습니다.

## 실행

**Windows 탐색기에서 이 폴더의 배치 파일을 실행하세요.** JDK가 필요하며, 다른 PC에서는 `JAVA_HOME` 또는 Java `PATH`를 설정하세요.

| 파일 | 실행 내용 | 카드 저장 |
|---|---|---|
| `run-battle.bat` | 교수님/중 난이도 전투 단독 미리보기 | 없음 |
| `run.bat` | 테스트용 로그인·게임 설정·전투·도감 연결 | `.integration-runtime/saves/` |

WSL에서는 이 저장소 폴더에서 Windows 실행기를 호출합니다.

```bash
cd "/mnt/c/users/metan/onedrive/desktop/객체지향_팀프로젝트/kgu-quiz-game"
cmd.exe /c run-battle.bat
# 로그인, 카테고리 선택과 저장까지 확인하려면:
cmd.exe /c run.bat
```

`run.bat` 실행 후: 새 ID 입력 → 게임 시작 → 분야(또는 무작위)·난이도·**모험** 선택 → 도전 시작.

초기 실행 시 Gradle과 의존성이 다운로드될 수 있습니다. 문제와 그림은 테스트 자료에서 자동 준비됩니다. 런타임 폴더를 직접 고치지 말고 `src/test/resources/integration`의 원본 자료를 수정하세요.

`gradlew run`은 기존 공용 Main의 Hello World를 실행합니다. 이 브랜치의 화면 확인은 `runIntegration`을 사용합니다.

## 현재 전투 동작

- 선택한 분야 또는 무작위 분야에서 상대 한 명에게 도전합니다. 공격·방어 턴 구분은 없습니다.
- HP 60에서 시작하며 정답이면 피해, 오답이면 HP 유지. 현재 기본 데이터는 3문제·2회 정답 적중(60 → 30 → 0)입니다. 문제 수와 성공에 필요한 적중 횟수는 분리되어 있습니다.
- 정답: 타격 효과 후 약 1초 뒤 자동 진행. 오답: 정답을 약 2.4초 표시한 뒤 자동 진행.
- HP 0이면 효과와 피드백 후 자동으로 결과 전달. 문제 소진 시 실패 결과 전달.
- 일시정지와 다음 버튼 없이 자동 진행합니다. Enter는 답안 제출에만 사용하며 피드백을 건너뛰지 않습니다.
- 승리한 인물의 해당 난이도 카드 한 장만 저장합니다. 기존 카드 중복 획득은 저장량이 증가하지 않습니다.
- 중앙 인물·상단 HP·하단 문제 배치, 화면 흔들림·카드 분리·피해 숫자·격파 표시를 제공합니다.
- 준비 및 전투 UI에서 실제 이름을 표시하지 않습니다. 결과 화면에서는 이름을 공개합니다. 문제 본문 자체의 오류나 이미지 속 글자는 별도 검수 대상입니다.
- 힌트와 응원은 능력/아이템 연동 전까지 비활성 상태입니다.

## 담당 범위와 소스 구조

| 위치 | 역할 |
|---|---|
| `src/main/java/org/kgu/battle` | 배틀 모델과 엔진 |
| `src/main/java/org/kgu/ui/battle` | 준비·전투·결과 화면 및 효과 |
| `src/main/resources/org/kgu/battle` | 캠퍼스 배경 |
| `src/test/java/org/kgu/integration` | 팀 화면 참고본과 데이터·저장 연결 어댑터 |
| `src/test/resources/integration` | 수동/자동 확인용 문제, 이미지, 샘플 저장 |

팀원의 실제 시작·문제·도감 클래스를 이 브랜치의 main에 복사하지 않습니다. 테스트 어댑터는 `org.kgu.integration` 패키지에 격리합니다. 별도 폴더 `kgu-quiz-game-integrated`는 로컬 통합 확인본이며 이 저장소에 포함되지 않습니다.

현재 실행 경로는 `BattleEngine` → `BattleScreen`입니다. 이전 1대1 공격·방어 구현은 제거했으며, 상대 한 명을 격파하는 규칙만 사용합니다.

## 빌드와 테스트

Windows PowerShell, 저장소 루트:

```powershell
.\gradlew.bat -g .gradle-user-home clean build
.\gradlew.bat -g .gradle-user-home runIntegration --args="--smoke"
```

WSL에서 Windows JDK를 사용하는 경우:

```bash
cmd.exe /c "gradlew.bat -g .gradle-user-home clean build"
```

Linux JDK가 설정된 환경에서는 `./gradlew clean build`를 사용할 수 있습니다.

- 테스트 보고서: `build/reports/tests/test/index.html`
- 화면 확인 이미지: `build/battle-preview-real.png`, `build/battle-preview-hit.png`
- 자동 테스트의 저장 위치는 `build/battle-test-runtime`으로 수동 실행 저장과 분리됩니다.

## 팀 공유

- [코딩 및 협업 규칙](docs/CONTRIBUTING.md)
- [담당 모듈 가이드](docs/reference/TEAM_MODULE_GUIDE.md)
- [현재 연결 API와 변경 범위](docs/BATTLE_INTEGRATION.md)
- [문제 확장 및 결정사항 반영](docs/PROFESSOR_BATTLE.md)

기능 브랜치에서 검증 후 PR로 공유합니다. 현재 세션의 develop 병합 중단 방침을 유지하며 main/develop Push나 강제 Push는 하지 않습니다.
