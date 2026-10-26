# Java Project Boilerplate

Java 25 기반의 간단한 프로젝트입니다.

[Eclipse Temurin® JDK 25 LTS](https://adoptium.net/)를 사용하는 것을 권장합니다.

## 빌드 및 실행

저장소를 복제한 뒤 프로젝트 디렉터리로 이동합니다.

```sh
git clone https://github.com/KGU-OOP-J/java-project-boilerplate.git
cd java-project-boilerplate
```

* **Windows**

```pwsh
.\gradlew.bat build
.\gradlew.bat run
```

* **Linux / macOS**

```sh
./gradlew build
./gradlew run
```

## Project Structure

프로젝트는 Gradle의 표준 디렉터리 구조를 따릅니다.

* `src/main/java/`: Java 소스 코드
* `src/main/resources/`: 애플리케이션 리소스
* `src/test/java/`: 테스트 코드
* `src/test/resources/`: 테스트용 리소스

Java 소스 코드는 `org.kgu` 패키지를 기준으로 작성하며, 테스트 코드는 테스트 대상과 동일한 패키지 구조를 따르는 것을 권장합니다.

## 개발 문서

* [Gradle 사용법 및 실행 설정](docs/GRADLE.md)
* [코딩 및 협업 규칙](docs/CONTRIBUTING.md)
