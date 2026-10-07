package com.aris.templateapp.ui.upload;

import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Menyajikan file situs hasil ekstrak ZIP ke WebView lewat {@link WebViewAssetLoader} (bukan {@code file://},
 * alur-fitur-upload.md bagian 7.9). Path yang diawali "/" (bawaan Vite) dibaca dari folder utama ZIP.
 * <p>
 * Dibuat sendiri (bukan InternalStoragePathHandler) agar jenis file selalu benar, mis. {@code .mjs} sebagai JavaScript:
 * browser menolak menjalankan modul JS dengan jenis file yang salah.
 */
class LocalSitePathHandler implements WebViewAssetLoader.PathHandler {

    private static final Map<String, String> MIME = new HashMap<>();

    static {
        MIME.put("html", "text/html");
        MIME.put("htm", "text/html");
        MIME.put("css", "text/css");
        MIME.put("js", "text/javascript");
        MIME.put("mjs", "text/javascript");
        MIME.put("json", "application/json");
        MIME.put("svg", "image/svg+xml");
        MIME.put("png", "image/png");
        MIME.put("jpg", "image/jpeg");
        MIME.put("jpeg", "image/jpeg");
        MIME.put("webp", "image/webp");
        MIME.put("gif", "image/gif");
        MIME.put("ico", "image/x-icon");
        MIME.put("woff", "font/woff");
        MIME.put("woff2", "font/woff2");
        MIME.put("ttf", "font/ttf");
        MIME.put("otf", "font/otf");
        MIME.put("txt", "text/plain");
        MIME.put("md", "text/plain");
    }

    private final File root;
    private final String canonicalRoot;

    LocalSitePathHandler(File root) throws IOException {
        this.root = root;
        this.canonicalRoot = root.getCanonicalPath() + File.separator;
    }

    @Nullable
    @Override
    public WebResourceResponse handle(@NonNull String path) {
        try {
            String clean = path.isEmpty() || path.endsWith("/") ? path + "index.html" : path;
            File file = new File(root, clean);
            // Jangan pernah melayani file di luar folder situs (mis. "../../shared_prefs").
            if (!file.getCanonicalPath().startsWith(canonicalRoot) || !file.isFile()) {
                return notFound();
            }
            InputStream in = new FileInputStream(file);
            String mime = mimeOf(file.getName());
            return new WebResourceResponse(mime, mime.startsWith("text/") || mime.endsWith("json") ? "utf-8" : null, in);
        } catch (IOException e) {
            return notFound();
        }
    }

    static String mimeOf(String name) {
        int dot = name.lastIndexOf('.');
        String ext = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
        String mime = MIME.get(ext);
        return mime != null ? mime : "application/octet-stream";
    }

    private static WebResourceResponse notFound() {
        WebResourceResponse response = new WebResourceResponse("text/plain", "utf-8", null);
        response.setStatusCodeAndReasonPhrase(404, "Not Found");
        return response;
    }
}
