package com.aris.templateapp.core.upload;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Menyiapkan gambar thumbnail (alur-fitur-upload.md bagian 6.2): persegi sesuai thumbnail kartu galeri,
 * {@value #SIZE_PX} px, JPEG di bawah 1 MB.
 */
@Singleton
public class ThumbnailImages {

    public static final int SIZE_PX = 720;
    private static final int MAX_BYTES = 1024 * 1024;

    private final ContentResolver resolver;

    @Inject
    public ThumbnailImages(@ApplicationContext Context context) {
        this.resolver = context.getContentResolver();
    }

    /** Gambar pilihan provider: dibaca hemat memori, dipotong persegi di tengah, diperkecil. Null jika gagal dibaca. */
    @Nullable
    @WorkerThread
    public Bitmap fromUri(Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = resolver.openInputStream(uri)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            int shortest = Math.min(bounds.outWidth, bounds.outHeight);
            options.inSampleSize = 1;
            while (shortest / (options.inSampleSize * 2) >= SIZE_PX) {
                options.inSampleSize *= 2;
            }
            Bitmap decoded;
            try (InputStream in = resolver.openInputStream(uri)) {
                decoded = BitmapFactory.decodeStream(in, null, options);
            }
            return decoded == null ? null : squareCrop(decoded);
        } catch (IOException | SecurityException e) {
            return null;
        }
    }

    /** Potong persegi di tengah lalu ubah ke {@value #SIZE_PX} px. */
    public static Bitmap squareCrop(Bitmap source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        Bitmap square = Bitmap.createBitmap(source, x, y, side, side);
        Bitmap scaled = Bitmap.createScaledBitmap(square, SIZE_PX, SIZE_PX, true);
        if (square != source) {
            square.recycle();
        }
        return scaled;
    }

    /** JPEG di bawah 1 MB; kualitas diturunkan bertahap jika masih terlalu besar. Null jika tetap kebesaran. */
    @Nullable
    @WorkerThread
    public static byte[] toJpeg(Bitmap bitmap) {
        for (int quality = 85; quality >= 45; quality -= 10) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            if (out.size() <= MAX_BYTES) {
                return out.toByteArray();
            }
        }
        return null;
    }
}
