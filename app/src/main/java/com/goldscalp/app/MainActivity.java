package com.goldscalp.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

/**
 * Full-screen WebView that shows the Gold Scalp MTF mobile page served by your bridge server.
 * First launch asks for the server address (e.g. http://192.168.1.10:8000).
 * Long-press Volume Up at any time to change the address.
 */
public class MainActivity extends Activity {

    private static final String BG = "#0b0f14";
    private WebView web;
    private SharedPreferences prefs;
    private boolean dialogShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.parseColor(BG));
        getWindow().setNavigationBarColor(Color.parseColor(BG));
        prefs = getSharedPreferences("gsm", MODE_PRIVATE);

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor(BG));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    askUrl("Can't reach the server (" + error.getDescription() + "). Check the address:");
                }
            }
        });

        // Without this, confirm()/prompt()/alert() in the page do not work inside a WebView.
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView v, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("OK", (d, w) -> result.confirm())
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView v, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("OK", (d, w) -> result.confirm())
                        .setNegativeButton("Cancel", (d, w) -> result.cancel())
                        .show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView v, String url, String message, String defaultValue,
                                      final JsPromptResult result) {
                final EditText input = new EditText(MainActivity.this);
                input.setText(defaultValue);
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setView(input)
                        .setCancelable(false)
                        .setPositiveButton("OK", (d, w) -> result.confirm(input.getText().toString()))
                        .setNegativeButton("Cancel", (d, w) -> result.cancel())
                        .show();
                return true;
            }
        });

        String url = prefs.getString("url", "");
        if (url.isEmpty()) {
            askUrl("Enter the server address, e.g. http://192.168.1.10:8000");
        } else {
            web.loadUrl(url);
        }
    }

    private void askUrl(String message) {
        if (dialogShowing) return;
        dialogShowing = true;
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(prefs.getString("url", "http://"));
        new AlertDialog.Builder(this)
                .setTitle("Server address")
                .setMessage(message)
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Connect", (d, w) -> {
                    dialogShowing = false;
                    String u = input.getText().toString().trim();
                    if (!u.startsWith("http")) u = "http://" + u;
                    prefs.edit().putString("url", u).apply();
                    web.loadUrl(u);
                })
                .setNegativeButton("Exit", (d, w) -> {
                    dialogShowing = false;
                    finish();
                })
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            askUrl("Change the server address:");
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onPause() {
        web.onPause();
        super.onPause();
    }
}
