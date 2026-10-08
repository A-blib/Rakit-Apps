package com.aris.templateapp.core.template;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * Opsi "Kompres foto agar ringan" saat export (bagian 8.1 langkah 7): foto JPG/WebP bawaan template yang besar
 * diperkecil (maks 1920 px) dan dikompres ulang kualitas 80, dengan format dan nama file tetap sama sehingga semua
 * rujukan di HTML/CSS tetap benar. PNG dibiarkan (biasanya ikon/logo yang butuh transparansi).
 */
public class AndroidImageCompressor implements ProjectExporter.ImageCompressor {

    /** Foto lebih kecil dari ini sudah cukup ringan. */
    static final int MIN_BYTES = 150 * 1024;

    @Nullable
    @Override
    public byte[] compress(String path, byte[] content) {
        String lower = path.toLowerCase(Locale.ROOT);
        Bitmap.CompressFormat format;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            format = Bitmap.CompressFormat.JPEG;
        } else if (lower.endsWith(".webp")) {
            format = ImageProcessor.webp();
        } else {
            return null;
        }
        if (content.length < MIN_BYTES) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(content, 0, content.length, bounds);
        if (bounds.outWidth <= 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = ImageProcessor.sampleSize(bounds.outWidth, bounds.outHeight, ImageProcessor.MAX_SIDE);
        Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length, options);
        if (bitmap == null) {
            return null;
        }
        try {
            float scale = Math.min(1f, ImageProcessor.MAX_SIDE / (float) Math.max(bitmap.getWidth(), bitmap.getHeight()));
            Bitmap scaled = scale < 1f ? Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * scale),
                    Math.round(bitmap.getHeight() * scale), true) : bitmap;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(format, ImageProcessor.QUALITY, out);
            if (scaled != bitmap) {
                scaled.recycle();
            }
            return out.toByteArray();
        } finally {
            bitmap.recycle();
        }
    }
}
