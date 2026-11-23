# 교수님 이름 맞추기

Java Swing으로 만드는 교수님 이름 맞추기 게임입니다.

## 실행

JDK가 설치된 Windows 환경에서 프로젝트 최상단의 `run.bat`을 더블 클릭하거나, 터미널에서 실행합니다.

```cmd
run.bat
```

`run.bat`은 모든 Java 소스를 임시 폴더에 컴파일한 뒤 `org.kgu.Main`을 실행합니다. 생성 파일은 Git에 올라가지 않습니다.

## 화면 담당

- ID 입력·메뉴·게임 준비: `org.kgu.ui.start`
- 문제 풀이: `org.kgu.ui.question`
- 교수님 도감: `org.kgu.ui.collection`
- 설정: `org.kgu.ui.settings`
- 결과·획득 카드: `org.kgu.ui.result`
- 화면 전환: `org.kgu.ui.navigation.GameNavigator`
- 공통 UI: `org.kgu.ui.common`

자세한 모듈 담당 규칙은 [docs/SCREEN_OWNERSHIP.md](docs/SCREEN_OWNERSHIP.md)를 참고하세요.
