package com.aris.templateapp.ui.upload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * WebView tersembunyi untuk memotret thumbnail dan menjalankan cek tahap C. WebView ditaruh di belakang isi layar
 * (bukan di luar layar), karena WebView yang dianggap tidak terlihat bisa berhenti menggambar.
 * <p>
 * Lebar ditentukan dalam piksel CSS: 390 = tampilan HP, 1280 = tampilan Desktop (bagian 7.4).
 */
public class OffscreenPage {

    public static final int MOBILE_WIDTH = 390;
    public static final int DESKTOP_WIDTH = 1280;
    // Menunggu gambar, font, dan animasi awal selesai setelah halaman dimuat.
    private static final long SETTLE_MS = 1200;

    /** Satu pesan error JavaScript dari console WebView. */
    public static final class ConsoleError {
        public final String file;
        public final int line;
        public final String message;

        ConsoleError(String file, int line, String message) {
            this.file = file;
            this.line = line;
            this.message = message;
        }
    }

    public interface Ready {
        void onReady();
    }

    private final ViewGroup host;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<ConsoleError> consoleErrors = new ArrayList<>();
    @Nullable
    private WebView webView;
    private int cssWidth;
    private float density;

    /** @param host FrameLayout di layar; WebView ditaruh paling belakang sehingga tertutup isi layar. */
    public OffscreenPage(FrameLayout host) {
        this.host = host;
    }

    /** Memuat satu halaman situs dengan lebar CSS tertentu; {@code ready} dipanggil setelah halaman tenang. */
    public void load(File siteRoot, List<String> allowedHosts, String page, int cssWidth, Ready ready) throws IOException {
        destroy();
        Context context = host.getContext();
        density = context.getResources().getDisplayMetrics().density;
        this.cssWidth = cssWidth;
        WebView view = new WebView(context);
        // Digambar dengan CPU agar draw() ke Bitmap selalu berisi tampilan halaman.
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        SiteWebView.configure(view, siteRoot, allowedHosts, cssWidth >= DESKTOP_WIDTH, false, null);
        consoleErrors.clear();
        view.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                if (message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    consoleErrors.add(new ConsoleError(SiteWebView.pageOf(message.sourceId()), message.lineNumber(),
                            message.message()));
                }
                return true;
            }

            @Override
            public void onProgressChanged(WebView v, int progress) {
                if (progress == 100) {
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(ready::onReady, SETTLE_MS);
                }
            }
        });
        int widthPx = Math.round(cssWidth * density);
        // Tinggi = lebar: area yang dipotret thumbnail berbentuk persegi, sama dengan thumbnail kartu galeri.
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(widthPx, widthPx);
        host.addView(view, 0, params);
        webView = view;
        view.loadUrl(SiteWebView.urlOf(page));
    }

    public List<ConsoleError> consoleErrors() {
        return new ArrayList<>(consoleErrors);
    }

    /** Menjalankan JavaScript dan mengembalikan hasilnya (string JSON) lewat callback. */
    public void evaluate(String script, ValueCallback<String> callback) {
        if (webView != null) {
            webView.evaluateJavascript(script, callback);
        }
    }

    /** Menjalankan file JS dari assets (mis. upload/sections.js). */
    public void evaluateAsset(String assetPath, ValueCallback<String> callback) {
        try (InputStream in = host.getContext().getAssets().open(assetPath)) {
            byte[] bytes = new byte[in.available()];
            int total = 0;
            int n;
            while (total < bytes.length && (n = in.read(bytes, total, bytes.length - total)) > 0) {
                total += n;
            }
            evaluate(new String(bytes, 0, total, StandardCharsets.UTF_8), callback);
        } catch (IOException e) {
            callback.onReceiveValue(null);
        }
    }

    /** Menggulir ke posisi (piksel CSS) lalu memotret area persegi selebar halaman, diperkecil ke {@code sizePx}. */
    public void capture(int scrollTopCss, int sizePx, CaptureCallback callback) {
        if (webView == null) {
            callback.onCaptured(null);
            return;
        }
        WebView view = webView;
        view.evaluateJavascript("window.scrollTo(0, " + scrollTopCss + ");", ignored ->
                handler.postDelayed(() -> {
                    Bitmap full = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                    view.draw(new Canvas(full));
                    Bitmap scaled = Bitmap.createScaledBitmap(full, sizePx, sizePx, true);
                    if (scaled != full) {
                        full.recycle();
                    }
                    callback.onCaptured(scaled);
                }, 400));
    }

    public interface CaptureCallback {
        void onCaptured(@Nullable Bitmap bitmap);
    }

    public int cssWidth() {
        return cssWidth;
    }

    public float density() {
        return density;
    }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
        if (webView != null) {
            host.removeView(webView);
            webView.destroy();
            webView = null;
        }
    }
}
