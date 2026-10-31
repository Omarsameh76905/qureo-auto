@echo off
chcp 65001 > nul
title رفع تطبيق الأندرويد لـ GitHub لبناء ملف الـ APK تلقائياً
color 0B

echo ==============================================================================
echo             أداة الرفع السريع لـ GitHub لبناء ملف الـ APK للأندرويد
echo ==============================================================================
echo.
echo الخطوات البسيطة:
echo 1. افتح موقع GitHub وأنشئ مستودع جديد (New Repository) وسمّه مثلاً: qureo-android
echo 2. انسخ رابط المستودع (مثال: https://github.com/your-username/qureo-android.git)
echo.

set /p REPO_URL="[?] الصق رابط المستودع هنا: "

if "%REPO_URL%"=="" (
    echo [!] لم يتم إدخال رابط. تم إلغاء العملية.
    pause
    exit /b
)

echo.
echo [+] جاري إعداد مستودع Git ورفع الملفات...
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
    echo [❌] حدث خطأ أثناء الرفع. يرجى التأكد من أنك سجلت الدخول في Git أو استخدام Personal Access Token.
    pause
    exit /b
)

echo.
echo ==============================================================================
echo [✓] تم الرفع بنجاح تام!
echo [✓] الآن افتح صفحة المستودع على GitHub واضغط على تبويب "Actions".
echo [✓] ستجد سيرفر GitHub يقوم ببناء الـ APK تلقائياً (خلال دقيقتين).
echo [✓] بعد انتهاء البناء، ستجد ملف QureoAutoSolver_Turbo.apk جاهز للتحميل
echo     مباشرة في قسم Releases أو Artifacts وثبته على موبايلك فوراً!
echo ==============================================================================
pause
