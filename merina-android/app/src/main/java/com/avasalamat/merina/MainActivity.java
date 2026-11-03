package com.avasalamat.merina;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

/** Merina: a thin shell around the Merina web app. The server address is typed once (Menu > Server address). Everything else - editing, access rights, updates - lives on the server. */
public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> chooser;
    private static final int PICK = 77;
    private SharedPreferences sp;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("merina", MODE_PRIVATE);
        getWindow().setStatusBarColor(Color.parseColor("#17406f"));
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false); s.setBuiltInZoomControls(true); s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true); s.setLoadWithOverviewMode(false);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl(), base = Uri.parse(server());
                if (u.getHost() != null && u.getHost().equals(base.getHost())) return false;
                startActivity(new Intent(Intent.ACTION_VIEW, u)); return true;
            }
            @Override public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) v.loadData("<html><body style='font-family:sans-serif;padding:24px'><h3>Cannot reach the server</h3><p>Check your internet connection and the server address (menu &rarr; Server address).</p></body></html>", "text/html", "utf-8");
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (chooser != null) chooser.onReceiveValue(null);
                chooser = cb;
                try { startActivityForResult(p.createIntent(), PICK); } catch (Exception e) { chooser = null; return false; }
                return true;
            }
        });
        web.setDownloadListener((url, ua, cd, mime, len) -> {          // Excel exports / "Update my Excel" files
            DownloadManager.Request q = new DownloadManager.Request(Uri.parse(url));
            q.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url));
            q.addRequestHeader("User-Agent", ua);
            q.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            q.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, cd, mime));
            ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(q);
            Toast.makeText(this, "Downloading to the Downloads folder…", Toast.LENGTH_SHORT).show();
        });
        if (server().isEmpty()) askServer(); else web.loadUrl(server());
    }

    private String server() { return sp.getString("url", ""); }

    private void askServer() {
        final EditText e = new EditText(this);
        e.setHint("https://merina.example.com"); e.setText(server()); e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Server address").setMessage("Enter the address of your Merina server.").setView(e)
            .setPositiveButton("Save", (d, w) -> {
                String u = e.getText().toString().trim();
                if (u.isEmpty()) return;
                if (!u.startsWith("http")) u = "https://" + u;
                sp.edit().putString("url", u).apply(); web.loadUrl(u);
            }).setNegativeButton("Cancel", null).show();
    }

    @Override public boolean onCreateOptionsMenu(Menu m) { m.add(0, 1, 0, "Server address"); m.add(0, 2, 1, "Reload"); return true; }
    @Override public boolean onOptionsItemSelected(MenuItem i) { if (i.getItemId() == 1) askServer(); else web.reload(); return true; }
    @Override public boolean onKeyDown(int k, KeyEvent ev) {
        if (k == KeyEvent.KEYCODE_MENU) { askServer(); return true; }
        if (k == KeyEvent.KEYCODE_BACK && web.canGoBack()) { web.goBack(); return true; }
        return super.onKeyDown(k, ev);
    }
    @Override protected void onActivityResult(int rq, int rs, Intent d) {
        if (rq == PICK && chooser != null) { chooser.onReceiveValue(android.webkit.WebChromeClient.FileChooserParams.parseResult(rs, d)); chooser = null; }
        else super.onActivityResult(rq, rs, d);
    }
}
