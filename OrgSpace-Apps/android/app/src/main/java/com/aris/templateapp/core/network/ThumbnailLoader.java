package com.aris.templateapp.core.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.aris.templateapp.BuildConfig;
import com.aris.templateapp.core.util.AppExecutors;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Memuat thumbnail template dari backend ke ImageView, dengan cache di memori. Dibuat sendiri dengan OkHttp
 * (yang sudah ada) karena library gambar seperti Glide belum boleh dipasang (AGENTS.md bagian 4.2).
 * Memakai client yang menempelkan token, karena thumbnail draft hanya boleh dilihat pemiliknya.
 */
@Singleton
public class ThumbnailLoader {

    // Thumbnail berukuran kecil (±720 px); 1/8 memori app cukup untuk puluhan gambar.
    private final LruCache<String, Bitmap> cache =
            new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }
            };
    private final OkHttpClient client;
    private final AppExecutors executors;
    private final HttpUrl base = HttpUrl.get(BuildConfig.API_BASE_URL);

    @Inject
    public ThumbnailLoader(OkHttpClient client, AppExecutors executors) {
        this.client = client;
        this.executors = executors;
    }

    /**
     * @param url        thumbnailUrl dari backend (mis. "/api/templates/…/thumbnail?v=…"); null = tampilkan placeholder
     * @param onLoaded   dipanggil di thread utama setelah gambar terpasang (mis. menyembunyikan ikon placeholder)
     */
    public void load(@Nullable String url, ImageView target, @Nullable Runnable onLoaded) {
        target.setTag(url);
        if (url == null) {
            target.setImageDrawable(null);
            return;
        }
        Bitmap cached = cache.get(url);
        if (cached != null) {
            target.setImageBitmap(cached);
            if (onLoaded != null) {
                onLoaded.run();
            }
            return;
        }
        target.setImageDrawable(null);
        HttpUrl resolved = base.resolve(url);
        if (resolved == null) {
            return;
        }
        executors.networkIO().execute(() -> {
            Bitmap bitmap = fetch(resolved);
            if (bitmap == null) {
                return;
            }
            cache.put(url, bitmap);
            executors.mainThread().execute(() -> {
                // ImageView di daftar bisa sudah dipakai ulang untuk template lain.
                if (url.equals(target.getTag())) {
                    target.setImageBitmap(bitmap);
                    if (onLoaded != null) {
                        onLoaded.run();
                    }
                }
            });
        });
    }

    /** Menaruh gambar yang baru dibuat di HP ke cache, agar tidak perlu diunduh ulang. */
    public void put(String url, Bitmap bitmap) {
        cache.put(url, bitmap);
    }

    @Nullable
    private Bitmap fetch(HttpUrl url) {
        try (Response response = client.newCall(new Request.Builder().url(url).build()).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) {
                return null;
            }
            byte[] bytes = body.bytes();
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IOException e) {
            return null;
        }
    }
}
