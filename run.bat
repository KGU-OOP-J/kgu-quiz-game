@echo off
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME if exist "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
if not defined GRADLE_USER_HOME set "GRADLE_USER_HOME=%CD%\.gradle-user-home"
call "%~dp0gradlew.bat" runIntegration %*
if errorlevel 1 pause
endlocal
