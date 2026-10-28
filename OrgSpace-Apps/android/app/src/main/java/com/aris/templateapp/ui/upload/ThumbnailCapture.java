package com.aris.templateapp.ui.upload;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.aris.templateapp.core.upload.ThumbnailImages;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Memotret thumbnail dari halaman index.html (alur-fitur-upload.md bagian 6.2): bagian atas halaman (otomatis) atau
 * section pilihan. Lebar 390 px CSS = tampilan HP, 1280 px = Desktop.
 */
class ThumbnailCapture {

    interface Captured {
        void onCaptured(@Nullable Bitmap bitmap);
    }

    interface SectionsLoaded {
        void onLoaded(List<SectionInfo> sections);
    }

    private final OffscreenPage page;

    ThumbnailCapture(OffscreenPage page) {
        this.page = page;
    }

    void captureTop(File siteRoot, List<String> allowedHosts, int cssWidth, Captured done) {
        try {
            page.load(siteRoot, allowedHosts, "index.html", cssWidth,
                    () -> page.capture(0, ThumbnailImages.SIZE_PX, bitmap -> finish(bitmap, done)));
        } catch (IOException e) {
            done.onCaptured(null);
        }
    }

    /** Memuat halaman lalu mendeteksi section; halaman tetap terbuka untuk {@link #captureSection}. */
    void loadSections(File siteRoot, List<String> allowedHosts, int cssWidth, SectionsLoaded done) {
        try {
            page.load(siteRoot, allowedHosts, "index.html", cssWidth,
                    () -> page.evaluateAsset("upload/sections.js", value -> done.onLoaded(SectionInfo.parse(value))));
        } catch (IOException e) {
            done.onLoaded(Collections.emptyList());
        }
    }

    void captureSection(SectionInfo section, Captured done) {
        page.capture(section.top, ThumbnailImages.SIZE_PX, bitmap -> finish(bitmap, done));
    }

    /**
     * Halaman ditutup setelah dipotret. WebView tersembunyi digambar CPU di thread utama; jika dibiarkan, animasi
     * template (mis. efek mengetik) menggambar ulang terus dan membuat ketikan di form Info tersendat.
     */
    private void finish(@Nullable Bitmap bitmap, Captured done) {
        page.destroy();
        done.onCaptured(bitmap);
    }

    void destroy() {
        page.destroy();
    }
}
