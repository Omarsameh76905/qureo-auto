package com.qureo.solver;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private TextView tvHeaderTitle, tvHeaderSub;
    private Button btnLangToggle;
    private TextView tvUserLabel, tvPassLabel, tvRangeLabel, tvFromLabel, tvToLabel;
    private EditText etStudentId, etPassword;
    private CheckBox cbShowPass;
    private Spinner spStartCh, spEndCh;
    private Button btnStart, btnStop;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private TextView tvConsoleTitle, btnClearLog, tvLog;
    private ScrollView svLog;
    private WebView webViewEngine;

    // State
    private boolean isArabic = false;
    private boolean isRunning = false;
    private String currentStudentId = "";
    private String currentPassword = "";
    private int currentStartCh = 1;
    private int currentEndCh = 42;
    private boolean hasInjectedEngine = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupSpinners();
        setupListeners();
        setupWebView();
        updateLanguageUI();
    }

    private void initViews() {
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderSub = findViewById(R.id.tvHeaderSub);
        btnLangToggle = findViewById(R.id.btnLangToggle);

        tvUserLabel = findViewById(R.id.tvUserLabel);
        etStudentId = findViewById(R.id.etStudentId);
        tvPassLabel = findViewById(R.id.tvPassLabel);
        etPassword = findViewById(R.id.etPassword);
        cbShowPass = findViewById(R.id.cbShowPass);

        tvRangeLabel = findViewById(R.id.tvRangeLabel);
        tvFromLabel = findViewById(R.id.tvFromLabel);
        spStartCh = findViewById(R.id.spStartCh);
        tvToLabel = findViewById(R.id.tvToLabel);
        spEndCh = findViewById(R.id.spEndCh);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);

        progressBar = findViewById(R.id.progressBar);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);

        tvConsoleTitle = findViewById(R.id.tvConsoleTitle);
        btnClearLog = findViewById(R.id.btnClearLog);
        tvLog = findViewById(R.id.tvLog);
        svLog = findViewById(R.id.svLog);

        webViewEngine = findViewById(R.id.webViewEngine);
    }

    private void setupSpinners() {
        List<String> chapters = new ArrayList<>();
        for (int i = 1; i <= 42; i++) {
            chapters.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                chapters
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spStartCh.setAdapter(adapter);
        spEndCh.setAdapter(adapter);

        spStartCh.setSelection(0);   // Chapter 1
        spEndCh.setSelection(41);   // Chapter 42
    }

    private void setupListeners() {
        btnLangToggle.setOnClickListener(v -> {
            isArabic = !isArabic;
            updateLanguageUI();
        });

        cbShowPass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnStart.setOnClickListener(v -> startSolvingProcess());

        btnStop.setOnClickListener(v -> stopSolvingProcess());

        btnClearLog.setOnClickListener(v -> tvLog.setText(""));
    }

    private void updateLanguageUI() {
        if (isArabic) {
            btnLangToggle.setText("English");
            tvHeaderTitle.setText("أداة كوريو الشاملة 2026");
            tvHeaderSub.setText("إصدار الموبايل السريع (100% ماسات كاملة)");
            tvUserLabel.setText("كود الطالب (Student ID):");
            etStudentId.setHint("أدخل كود الطالب هنا");
            tvPassLabel.setText("كلمة المرور (Password):");
            etPassword.setHint("أدخل كلمة المرور");
            cbShowPass.setText("إظهار كلمة المرور");
            tvRangeLabel.setText("نطاق الفصول للحل:");
            tvFromLabel.setText("من فصل:");
            tvToLabel.setText("إلى فصل:");
            btnStart.setText("🚀 بدء الحل التلقائي");
            btnStop.setText("⏹ إيقاف");
            tvConsoleTitle.setText("💻 سجل التنفيذ المباشر:");
            btnClearLog.setText("مسح السجل");
        } else {
            btnLangToggle.setText("العربية");
            tvHeaderTitle.setText("QUREO AUTO SOLVER");
            tvHeaderSub.setText("Turbo Mobile Edition (100% Perfect)");
            tvUserLabel.setText("Student ID:");
            etStudentId.setHint("Enter Student ID");
            tvPassLabel.setText("Password:");
            etPassword.setHint("Enter Password");
            cbShowPass.setText("Show Password");
            tvRangeLabel.setText("Solve Chapter Range:");
            tvFromLabel.setText("From:");
            tvToLabel.setText("To:");
            btnStart.setText("🚀 Start Solving");
            btnStop.setText("⏹ Stop");
            tvConsoleTitle.setText("💻 Live Progress Console:");
            btnClearLog.setText("Clear Log");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webViewEngine.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(false); // Speed up network
        settings.setBlockNetworkImage(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webViewEngine, true);

        webViewEngine.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        webViewEngine.setWebChromeClient(new WebChromeClient());
        webViewEngine.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!isRunning) return;

                if (url.contains("/login")) {
                    // Inject automated login
                    appendLog("[+] Login page loaded. Authenticating...");
                    injectLoginScript(currentStudentId, currentPassword);
                } else if (url.contains("/course") || url.contains("/dashboard") || url.contains("qureo.education")) {
                    if (!hasInjectedEngine) {
                        hasInjectedEngine = true;
                        appendLog("[✓] Signed in successfully! Launching Turbo Engine...");
                        injectTurboEngine();
                    }
                }
            }
        });
    }

    private void startSolvingProcess() {
        String user = etStudentId.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, isArabic ? "يرجى إدخال كود الطالب وكلمة المرور" : "Please enter Student ID and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        int startCh = Integer.parseInt((String) spStartCh.getSelectedItem());
        int endCh = Integer.parseInt((String) spEndCh.getSelectedItem());

        if (startCh > endCh) {
            int tmp = startCh;
            startCh = endCh;
            endCh = tmp;
        }

        currentStudentId = user;
        currentPassword = pass;
        currentStartCh = startCh;
        currentEndCh = endCh;
        hasInjectedEngine = false;
        isRunning = true;

        setControlsEnabled(false);
        progressBar.setProgress(0);
        tvProgressPercent.setText("0%");

        appendLog("=======================================================");
        appendLog(isArabic ? "🚀 بدء جلسة الحل السريع للفصول من " + startCh + " إلى " + endCh
                : "🚀 Starting Turbo Session for Chapters " + startCh + " to " + endCh);
        appendLog("=======================================================");

        // Clear previous cookies and navigate to login
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        webViewEngine.loadUrl("https://me-portal.qureo.education/login");
    }

    private void stopSolvingProcess() {
        if (!isRunning) return;
        appendLog("\n[!] Stopping solver requested...");
        webViewEngine.evaluateJavascript("window.stopTurboSolver();", null);
        btnStop.setEnabled(false);
    }

    private void setControlsEnabled(boolean enabled) {
        btnStart.setEnabled(enabled);
        btnStop.setEnabled(!enabled);
        etStudentId.setEnabled(enabled);
        etPassword.setEnabled(enabled);
        spStartCh.setEnabled(enabled);
        spEndCh.setEnabled(enabled);
        btnLangToggle.setEnabled(enabled);
    }

    private void injectLoginScript(String user, String pass) {
        String js = "(function() {" +
                "  var btns = document.querySelectorAll('button');" +
                "  for (var b of btns) {" +
                "    if (b.innerText.includes('Learning Login') || b.innerText.includes('للتعلم')) {" +
                "      b.click(); break;" +
                "    }" +
                "  }" +
                "  setTimeout(function() {" +
                "    var u = document.getElementById('student_id');" +
                "    var p = document.getElementById('password');" +
                "    if (u && p) {" +
                "      u.value = '" + escapeJs(user) + "';" +
                "      u.dispatchEvent(new Event('input', { bubbles: true }));" +
                "      p.value = '" + escapeJs(pass) + "';" +
                "      p.dispatchEvent(new Event('input', { bubbles: true }));" +
                "      var sub = document.querySelector(\"button[type='submit']\");" +
                "      if (sub) sub.click();" +
                "    }" +
                "  }, 1000);" +
                "})();";
        webViewEngine.evaluateJavascript(js, null);
    }

    private void injectTurboEngine() {
        try {
            InputStream is = getAssets().open("qureo_turbo_engine.js");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            String engineCode = sb.toString();
            String launcher = "\nwindow.startTurboSolver(" + currentStartCh + ", " + currentEndCh + ");";

            webViewEngine.evaluateJavascript(engineCode + launcher, null);
        } catch (Exception e) {
            appendLog("[❌] Failed to load Turbo Engine asset: " + e.getMessage());
            onSolveFinished(false);
        }
    }

    private void appendLog(String message) {
        mainHandler.post(() -> {
            tvLog.append("\n" + message);
            svLog.post(() -> svLog.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void updateProgress(int current, int total) {
        mainHandler.post(() -> {
            int percent = (int) (((float) current / total) * 100);
            progressBar.setProgress(percent);
            tvProgressPercent.setText(percent + "%");
        });
    }

    private void onSolveFinished(boolean success) {
        mainHandler.post(() -> {
            isRunning = false;
            setControlsEnabled(true);
            progressBar.setProgress(100);
            tvProgressPercent.setText("100%");

            if (success) {
                new AlertDialog.Builder(this)
                        .setTitle(isArabic ? "تم بنجاح كامل! 🎉" : "Success! 🎉")
                        .setMessage(isArabic
                                ? "ألف مبروك! تم إنهاء جميع الفصول المحددة بنجاح كامل 100% وحصلت على 5 ماسات لكل فصل."
                                : "Congratulations! All requested chapters have been completed 100% Perfect with 5 Diamonds each.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private String escapeJs(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("'", "\\'");
    }

    // JavaScript Bridge
    public class AndroidBridge {
        @JavascriptInterface
        public void onLog(String message) {
            appendLog(message);
        }

        @JavascriptInterface
        public void onProgress(int current, int total) {
            updateProgress(current, total);
        }

        @JavascriptInterface
        public void onComplete(String summaryJson) {
            onSolveFinished(true);
        }

        @JavascriptInterface
        public void onError(String error) {
            appendLog("[❌] Engine Error: " + error);
            onSolveFinished(false);
        }
    }
}
