package com.aris.templateapp.ui.creator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thumbnail project (tangkapan preview beranda website, dibuat editor template mode) di kartu project. Dibaca dari
 * file di thread latar dan disimpan di cache memori; kuncinya ikut waktu file diubah, jadi thumbnail baru langsung
 * tampil setelah project diedit.
 */
final class ProjectThumbnails {

    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };
    private static final ExecutorService DISK = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ProjectThumbnails() {
    }

    static void load(ImageView target, @Nullable String path) {
        File file = path == null ? null : new File(path);
        String key = file == null ? null : path + "@" + file.lastModified();
        target.setTag(key);
        Bitmap cached = key == null ? null : CACHE.get(key);
        target.setImageBitmap(cached);
        if (key == null || cached != null) {
            return;
        }
        DISK.execute(() -> {
            Bitmap bitmap = file.isFile() ? BitmapFactory.decodeFile(file.getPath()) : null;
            if (bitmap == null) {
                return;
            }
            CACHE.put(key, bitmap);
            MAIN.post(() -> {
                // Kartu di daftar bisa sudah dipakai ulang untuk project lain.
                if (key.equals(target.getTag())) {
                    target.setImageBitmap(bitmap);
                }
            });
        });
    }
}
