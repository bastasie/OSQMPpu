@echo off
setlocal
where gradle >nul 2>nul
if errorlevel 1 (
  echo Gradle is not installed. Use GitHub Actions -> PPU Android APK -> Run workflow.
  exit /b 1
)
gradle :app:assembleDebug
if errorlevel 1 exit /b 1
echo.
echo APK: %~dp0app\build\outputs\apk\debug\app-debug.apk
endlocal
