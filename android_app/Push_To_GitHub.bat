@echo off
chcp 65001 > nul
title Push Android App to GitHub for Automated APK Build
color 0B

echo ==============================================================================
echo             Push to GitHub to Build Android APK via GitHub Actions
echo ==============================================================================
echo.
echo Steps:
echo 1. Create a new repository on GitHub (e.g. qureo-android).
echo 2. Copy the repository URL (e.g. https://github.com/your-username/qureo-android.git).
echo.

set /p REPO_URL="[?] Enter your GitHub repository URL: "

if "%REPO_URL%"=="" (
    echo [!] No URL provided. Aborted.
    pause
    exit /b
)

echo.
echo [+] Initializing Git and pushing code...
cd /d "%~dp0"
git init 2>nul
git config user.name "Qureo Developer"
git config user.email "qureo@autosolver.local"
git remote remove origin 2>nul
git remote add origin %REPO_URL%
git add .
git commit -m "Qureo Auto Solver 2026 - Turbo Mobile Edition v2.0"
git branch -M main
git push -u origin main --force

if %errorlevel% neq 0 (
    echo.
    echo [❌] Push failed. Make sure you are authenticated with GitHub.
    pause
    exit /b
)

echo.
echo ==============================================================================
echo [✓] Pushed successfully!
echo [✓] Go to your repository on GitHub and open the "Actions" tab.
echo [✓] The APK build job is now running (takes ~2 minutes).
echo [✓] When finished, download QureoAutoSolver_Turbo.apk from Releases or Artifacts!
echo ==============================================================================
pause
