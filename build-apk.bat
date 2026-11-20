@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

set "BUILD_TYPE=%~1"
if "%BUILD_TYPE%"=="" set "BUILD_TYPE=debug"

if not exist "gradlew.bat" (
  echo Error: gradlew.bat not found in project root.
  exit /b 1
)

if /I "%BUILD_TYPE%"=="debug" (
  if not exist "debug.keystore" (
    where keytool >nul 2>nul
    if %errorlevel%==0 (
      keytool -genkeypair -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
    )
  )
  echo Building Debug APK...
  call gradlew.bat :app:assembleDebug
  if errorlevel 1 exit /b %errorlevel%
  echo Done! APK path: %cd%\app\build\outputs\apk\debug\app-debug.apk
) else if /I "%BUILD_TYPE%"=="release" (
  echo Building Release APK...
  call gradlew.bat :app:assembleRelease
  if errorlevel 1 exit /b %errorlevel%
  echo Done! APK path: %cd%\app\build\outputs\apk\release\app-release.apk
) else (
  echo Usage: %~nx0 [debug^|release]
  exit /b 1
)
