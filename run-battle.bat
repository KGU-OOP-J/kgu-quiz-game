@echo off
setlocal
cd /d "%~dp0"
echo [BATTLE PREVIEW] %CD%
call "%~dp0gradlew.bat" -g "%CD%\.gradle-user-home" runIntegration --args=--battle
if errorlevel 1 pause
endlocal
