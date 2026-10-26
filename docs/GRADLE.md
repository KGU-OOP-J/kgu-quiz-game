# Gradle 사용법

이 프로젝트는 Gradle Wrapper(`gradlew`, `gradlew.bat`)를 사용합니다.

[Gradle](https://gradle.org/)은 자바(Java), 안드로이드(Android), 코틀린(Kotlin) 등 다양한 프로그래밍 언어를 지원하는 오픈소스 빌드 자동화 도구입니다.

## 주요 명령어

Windows는 `.\gradlew.bat`, Linux/macOS는 `./gradlew`로 명령어를 대체하여 실행해야 합니다.

| 명령어 | 기능 |
| --- | --- |
| `gradlew build` | 컴파일, 테스트 및 빌드 |
| `gradlew run` | 애플리케이션 실행 |
| `gradlew test` | 테스트 실행 |
| `gradlew clean` | Gradle 빌드 산출물 삭제 |
| `gradlew clean build` | 빌드 산출물 삭제 후 다시 빌드 |
| `gradlew tasks` | 사용 가능한 작업 확인 |
| `gradlew dependencies` | 의존성 구성 확인 |
