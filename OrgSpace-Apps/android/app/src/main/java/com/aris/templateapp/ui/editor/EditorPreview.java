package com.aris.templateapp.ui.editor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.aris.templateapp.ui.upload.LocalSitePathHandler;
import com.aris.templateapp.ui.upload.MarkWebView;
import com.aris.templateapp.ui.upload.SiteWebView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * WebView preview editor template mode (alur-buat-website-via-template.md bagian 11.5).
 * <ul>
 *   <li>file paket disajikan lewat {@link WebViewAssetLoader}, bukan {@code file://};</li>
 *   <li>gambar project disajikan dari path khusus {@value #PROJECT_PATH} (folder project, bukan folder paket);</li>
 *   <li>{@code assets/editor/editor.js} disuntik sebelum halaman dimuat;</li>
 *   <li>jembatan JS → Java hanya {@code tap(key)}, dan key diperiksa terhadap manifest.</li>
 * </ul>
 */
public class EditorPreview {

    /** Path gambar project di dalam WebView, mis. /_rakit/project/images/foto_hero-1.webp. */
    static final String PROJECT_PATH = "_rakit/project/";

    /** Dipanggil di thread utama. */
    public interface Listener {
        void onPageLoaded(String page);

        void onFieldTapped(String key);
    }

    private final WebView webView;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final String script;
    private final boolean documentStartScripts;
    private String page = "index.html";

    public EditorPreview(Context context, File packageDir, File projectDir, boolean desktop, boolean editing,
                         Set<String> keys, Listener listener) throws IOException {
        webView = new WebView(context);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        List<String> hosts = Arrays.asList(context.getResources().getStringArray(
                com.aris.templateapp.R.array.editor_allowed_hosts));
        SiteWebView.configure(webView, new Files(packageDir, projectDir), hosts, desktop, !editing,
                loaded -> page = loaded);
        script = MarkWebView.readAsset(context, "editor/editor.js");
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            documentStartScripts = true;
            Set<String> origin = Collections.singleton("https://" + SiteWebView.HOST);
            if (desktop) {
                WebViewCompat.addDocumentStartJavaScript(webView, MarkWebView.readAsset(context, "upload/desktop.js"), origin);
            }
            WebViewCompat.addDocumentStartJavaScript(webView, script, origin);
        } else {
            documentStartScripts = false;
        }
        webView.addJavascriptInterface(new Bridge(keys, listener), "RakitBridge");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    if (!documentStartScripts) {
                        view.evaluateJavascript(script, null);
                    }
                    view.evaluateJavascript("window.RakitEditor && RakitEditor.setEditing(" + editing + ")", null);
                    listener.onPageLoaded(page);
                }
            }
        });
    }

    public WebView view() {
        return webView;
    }

    public String page() {
        return page;
    }

    public void load(String page) {
        this.page = page;
        webView.loadUrl(SiteWebView.urlOf(page));
    }

    /** URL gambar project untuk dipakai di halaman (path relatif folder project, mis. "images/a.webp"). */
    static String imageUrl(String projectRelativePath) {
        return SiteWebView.BASE_URL + PROJECT_PATH + projectRelativePath;
    }

    public void run(String script, @Nullable ValueCallback<String> callback) {
        webView.evaluateJavascript(script, callback);
    }

    public void destroy() {
        main.removeCallbacksAndMessages(null);
        webView.removeJavascriptInterface("RakitBridge");
        webView.destroy();
    }

    /** File paket dari folder paket; path {@value #PROJECT_PATH} dari folder project. */
    private static final class Files implements WebViewAssetLoader.PathHandler {
        private final LocalSitePathHandler site;
        private final File projectDir;

        Files(File packageDir, File projectDir) throws IOException {
            this.site = new LocalSitePathHandler(packageDir);
            this.projectDir = projectDir;
        }

        @Nullable
        @Override
        public WebResourceResponse handle(@NonNull String path) {
            if (!path.startsWith(PROJECT_PATH)) {
                return site.handle(path);
            }
            try {
                File file = new File(projectDir, path.substring(PROJECT_PATH.length()));
                // Hanya file di dalam folder project yang boleh dilayani.
                if (!file.getCanonicalPath().startsWith(projectDir.getCanonicalPath() + File.separator) || !file.isFile()) {
                    return null;
                }
                return new WebResourceResponse(LocalSitePathHandler.mimeOf(file.getName()), null, new FileInputStream(file));
            } catch (IOException e) {
                return null;
            }
        }
    }

    /** Jembatan JS → Java. Dipanggil di thread WebView, jadi dipindah ke thread utama. */
    private final class Bridge {
        private final Set<String> keys;
        private final Listener listener;

        Bridge(Set<String> keys, Listener listener) {
            this.keys = keys;
            this.listener = listener;
        }

        @JavascriptInterface
        public void tap(String key) {
            // JavaScript template ikut berjalan di halaman ini, jadi hanya key yang ada di manifest yang diterima.
            if (key != null && keys.contains(key)) {
                main.post(() -> listener.onFieldTapped(key));
            }
        }
    }
}
