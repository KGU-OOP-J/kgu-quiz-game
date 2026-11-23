@echo off
setlocal

REM Compile every Java source to a temporary folder, then run the application.
REM No build artifacts are written into this Git repository.
set "RUN_DIR=%TEMP%\kgu-run"
set "SOURCE_LIST=%RUN_DIR%\sources.txt"

if not exist "%RUN_DIR%" mkdir "%RUN_DIR%"
dir /s /b src\main\java\*.java > "%SOURCE_LIST%"

javac -encoding UTF-8 -d "%RUN_DIR%" @"%SOURCE_LIST%"
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed. Check the Java error messages above.
    pause
    exit /b 1
)

java -cp "%RUN_DIR%" org.kgu.Main
endlocal
