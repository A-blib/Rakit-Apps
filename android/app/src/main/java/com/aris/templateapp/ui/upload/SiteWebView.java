package com.aris.templateapp.ui.upload;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.ViewGroup;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Pengaturan WebView untuk menampilkan template provider dengan aman (alur-fitur-upload.md bagian 7.9):
 * <ul>
 *   <li>file situs disajikan dari folder pribadi app lewat {@link WebViewAssetLoader};</li>
 *   <li>JavaScript provider tetap berjalan (Alpine, Bootstrap JS, Tailwind Play butuh itu);</li>
 *   <li>permintaan ke host di luar daftar yang diizinkan server (CDN terpercaya, Google Fonts) diblokir;</li>
 *   <li>perpindahan ke situs lain diblokir.</li>
 * </ul>
 */
public final class SiteWebView {

    /** Domain khusus WebViewAssetLoader; tidak pernah diakses ke internet. */
    public static final String HOST = "appassets.androidplatform.net";
    public static final String BASE_URL = "https://" + HOST + "/";
    // User-agent browser desktop untuk tampilan Desktop (bagian 7.4), bagi website yang mengecek jenis perangkat lewat JS.
    private static final String DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Safari/537.36";

    private SiteWebView() {
    }

    /** Dipanggil saat WebView memuat halaman lain di situs yang sama (mis. link antar-halaman di mode Coba). */
    public interface PageListener {
        void onPageStarted(String page);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void configure(WebView webView, File siteRoot, List<String> allowedHosts, boolean desktop,
                                 boolean allowSiteNavigation, @Nullable PageListener pageListener) throws IOException {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        if (desktop) {
            settings.setUserAgentString(DESKTOP_USER_AGENT);
        }

        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .setDomain(HOST)
                .addPathHandler("/", new LocalSitePathHandler(siteRoot))
                .build();
        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(@NonNull WebView view, @NonNull WebResourceRequest request) {
                Uri url = request.getUrl();
                WebResourceResponse local = loader.shouldInterceptRequest(url);
                if (local != null) {
                    return local;
                }
                String host = url.getHost() == null ? "" : url.getHost().toLowerCase(Locale.ROOT);
                // null = biarkan WebView memuat dari internet seperti biasa.
                return allowedHosts.contains(host) ? null : blocked();
            }

            @Override
            public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
                boolean sameSite = HOST.equals(request.getUrl().getHost());
                return !(allowSiteNavigation && sameSite);
            }

            /**
             * Proses render WebView bisa dimatikan sistem saat memori HP penuh. Tanpa ini seluruh app ikut tertutup;
             * dengan ini hanya WebView yang dilepas, dan layar bisa memuat ulang halaman.
             */
            @Override
            public boolean onRenderProcessGone(@NonNull WebView view, @NonNull RenderProcessGoneDetail detail) {
                if (view.getParent() instanceof ViewGroup) {
                    ((ViewGroup) view.getParent()).removeView(view);
                }
                view.destroy();
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (pageListener != null) {
                    pageListener.onPageStarted(pageOf(url));
                }
            }
        });
    }

    public static String urlOf(String page) {
        return BASE_URL + page;
    }

    /** "https://appassets.androidplatform.net/tentang.html" → "tentang.html". */
    public static String pageOf(@Nullable String url) {
        if (url == null || !url.startsWith(BASE_URL)) {
            return url == null ? "" : url;
        }
        String path = url.substring(BASE_URL.length());
        int cut = path.indexOf('?');
        if (cut < 0) {
            cut = path.indexOf('#');
        }
        return cut < 0 ? path : path.substring(0, cut);
    }

    private static WebResourceResponse blocked() {
        WebResourceResponse response = new WebResourceResponse("text/plain", "utf-8",
                new ByteArrayInputStream(new byte[0]));
        response.setStatusCodeAndReasonPhrase(403, "Blocked");
        return response;
    }
}
