package com.aris.templateapp.ui.upload;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * WebView editor Tandai bagian (alur-fitur-upload.md bagian 7.2–7.9). Memuat salinan bernomor, menyuntikkan
 * {@code assets/upload/mark.js}, dan menjadi satu-satunya pintu antara JavaScript halaman dan Java.
 * <p>
 * Jembatan ({@code RakitBridge}) sengaja hanya punya dua method dan hanya menerima angka, karena JavaScript milik
 * provider ikut berjalan di halaman yang sama (bagian 7.9). Data lain diminta Java lewat evaluateJavascript.
 */
public class MarkWebView {

    /** Dipanggil di thread utama. */
    public interface Listener {
        void onPageReady();

        void onElementTapped(int tplId);

        /** Elemen yang diketuk tidak bernomor: dibuat JavaScript, tidak bisa ditandai. */
        void onGeneratedTapped();
    }

    private final WebView webView;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private final String markScript;
    private final boolean documentStartScripts;

    public MarkWebView(Context context, File siteRoot, List<String> allowedHosts, boolean desktop, Listener listener)
            throws IOException {
        webView = new WebView(context);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        SiteWebView.configure(webView, siteRoot, allowedHosts, desktop, false, null);
        markScript = readAsset(context, "upload/mark.js");
        // Skrip dipasang sebelum halaman dimuat jika WebView mendukungnya, agar viewport desktop berlaku sejak awal.
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            documentStartScripts = true;
            Set<String> origin = Collections.singleton("https://" + SiteWebView.HOST);
            if (desktop) {
                WebViewCompat.addDocumentStartJavaScript(webView, readAsset(context, "upload/desktop.js"), origin);
            }
            WebViewCompat.addDocumentStartJavaScript(webView, markScript, origin);
        } else {
            documentStartScripts = false;
        }
        webView.addJavascriptInterface(new Bridge(listener), "RakitBridge");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    main.removeCallbacksAndMessages(null);
                    // Menunggu gambar & font agar posisi section sudah final.
                    main.postDelayed(() -> {
                        if (!documentStartScripts) {
                            view.evaluateJavascript(markScript, null);
                        }
                        listener.onPageReady();
                    }, 700);
                }
            }
        });
    }

    public WebView view() {
        return webView;
    }

    public void load(String page) {
        webView.loadUrl(SiteWebView.urlOf(page));
    }

    /** Piksel layar per piksel CSS (berbeda di tampilan HP dan Desktop, dan saat di-zoom). */
    public float scale(int innerWidthCss) {
        return innerWidthCss <= 0 ? 1f : webView.getWidth() / (float) innerWidthCss;
    }

    // ---------- pemanggil mark.js ----------

    public void call(String function, @Nullable ValueCallback<String> callback, Object... args) {
        StringBuilder script = new StringBuilder("window.RakitMark && RakitMark.").append(function).append('(');
        for (int i = 0; i < args.length; i++) {
            script.append(i == 0 ? "" : ",").append(gson.toJson(args[i]));
        }
        script.append(')');
        webView.evaluateJavascript(script.toString(), callback);
    }

    /** Hasil fungsi mark.js yang mengembalikan string JSON (dibungkus tanda kutip oleh evaluateJavascript). */
    public <T> void query(String function, Class<T> type, ResultCallback<T> callback, Object... args) {
        call(function, value -> callback.onResult(parse(value, type)), args);
    }

    public interface ResultCallback<T> {
        void onResult(@Nullable T result);
    }

    @Nullable
    <T> T parse(@Nullable String evaluated, Class<T> type) {
        try {
            if (evaluated == null || evaluated.equals("null")) {
                return null;
            }
            String json = JsonParser.parseString(evaluated).getAsString();
            return gson.fromJson(json, type);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Deteksi section ({@code assets/upload/sections.js}). */
    public void detectSections(ValueCallback<String> callback) {
        try {
            webView.evaluateJavascript(readAsset(webView.getContext(), "upload/sections.js"), callback);
        } catch (IOException e) {
            callback.onReceiveValue(null);
        }
    }

    public void destroy() {
        main.removeCallbacksAndMessages(null);
        webView.removeJavascriptInterface("RakitBridge");
        webView.destroy();
    }

    static String readAsset(Context context, String path) throws IOException {
        try (InputStream in = context.getAssets().open(path)) {
            byte[] bytes = new byte[in.available()];
            int total = 0;
            int n;
            while (total < bytes.length && (n = in.read(bytes, total, bytes.length - total)) > 0) {
                total += n;
            }
            return new String(bytes, 0, total, StandardCharsets.UTF_8);
        }
    }

    /** Jembatan JS → Java. Dipanggil di thread milik WebView, jadi dipindahkan ke thread utama. */
    private final class Bridge {
        private final Listener listener;

        Bridge(Listener listener) {
            this.listener = listener;
        }

        @JavascriptInterface
        public void select(int tplId) {
            if (tplId > 0) {
                main.post(() -> listener.onElementTapped(tplId));
            }
        }

        @JavascriptInterface
        public void selectGenerated() {
            main.post(listener::onGeneratedTapped);
        }
    }
}
