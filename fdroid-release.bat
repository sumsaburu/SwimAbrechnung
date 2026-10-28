@echo off
REM SwimAbrechnung - F-Droid Release 1.2 vorbereiten, committen, taggen und pushen
cd /d "%~dp0"
git rm -r -q --ignore-unmatch package.json package-lock.json tsconfig.json vite.config.ts src .env.example app/release gradle/gradle-daemon-jvm.properties
if exist src rmdir /s /q src
if exist app\release rmdir /s /q app\release
if exist gradle\gradle-daemon-jvm.properties del gradle\gradle-daemon-jvm.properties
git add -A
git commit -m "Prepare release 1.2 for F-Droid"
git tag -a v1.2 -m "Release 1.2"
git push SwimAbrechnung master
git push SwimAbrechnung v1.2
echo.
echo Commit-Hash fuer das F-Droid-Rezept:
git rev-parse HEAD
pause
