@echo off
setlocal
cd /d "%~dp0"
if not defined GRADLE_USER_HOME set "GRADLE_USER_HOME=%CD%\.gradle-user-home"
call "%~dp0gradlew.bat" run %*
if errorlevel 1 pause
endlocal
