package com.aris.templateapp.core.template;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Mengolah foto pilihan user (alur-buat-website-via-template.md bagian 6.6): perbaiki orientasi EXIF, potong tengah
 * sesuai rasio isian, perkecil hingga sisi terpanjang maks 1920 px, lalu simpan sebagai WebP kualitas 80 di folder
 * gambar project. Orientasi dibaca dengan {@link ExifInterface} bawaan Android (keputusan Aris, tanpa library).
 */
// ExifInterface bawaan sudah bisa membaca dari stream sejak API 24 (minSdk app 26); Aris memutuskan tanpa library
// androidx.exifinterface.
@SuppressLint("ExifInterface")
@Singleton
public class ImageProcessor {

    public static final int MAX_SIDE = 1920;
    public static final int QUALITY = 80;

    private final ContentResolver resolver;

    @Inject
    public ImageProcessor(@ApplicationContext Context context) {
        this.resolver = context.getContentResolver();
    }

    /**
     * @param aspectRatio mis. "16:9"; null = bentuk asli dipertahankan
     * @return path relatif folder project, mis. "images/foto_hero-1728370000.webp", atau null jika gambar tidak
     *         bisa dibaca
     */
    @WorkerThread
    @Nullable
    public String process(Uri uri, @Nullable String aspectRatio, File projectDir, String key) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        // Dibaca sudah diperkecil (kelipatan 2) agar foto kamera 50 MP tidak menghabiskan memori.
        options.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_SIDE);
        Bitmap decoded;
        try (InputStream in = resolver.openInputStream(uri)) {
            decoded = BitmapFactory.decodeStream(in, null, options);
        }
        if (decoded == null) {
            return null;
        }
        int orientation;
        try (InputStream in = resolver.openInputStream(uri)) {
            orientation = in == null ? ExifInterface.ORIENTATION_NORMAL
                    : new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        }
        Bitmap upright = rotate(decoded, orientation);
        int[] crop = cropRect(upright.getWidth(), upright.getHeight(), aspectRatio);
        float scale = Math.min(1f, MAX_SIDE / (float) Math.max(crop[2], crop[3]));
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap result = Bitmap.createBitmap(upright, crop[0], crop[1], crop[2], crop[3], matrix, true);

        File imagesDir = new File(projectDir, ProjectStore.IMAGES);
        if (!imagesDir.isDirectory() && !imagesDir.mkdirs()) {
            throw new IOException("Folder gambar tidak bisa dibuat");
        }
        String name = safeKey(key) + "-" + System.currentTimeMillis() + ".webp";
        try (OutputStream out = new FileOutputStream(new File(imagesDir, name))) {
            if (!result.compress(webp(), QUALITY, out)) {
                throw new IOException("Gambar tidak bisa disimpan");
            }
        } finally {
            if (result != upright) {
                result.recycle();
            }
            if (upright != decoded) {
                upright.recycle();
            }
            decoded.recycle();
        }
        return ProjectStore.IMAGES + "/" + name;
    }

    /** Menghapus file gambar lama isian jika tidak dipakai nilai sekarang maupun riwayat undo (bagian 6.6). */
    @WorkerThread
    public void deleteIfUnused(File projectDir, @Nullable String oldRelative, Set<String> inUse) {
        if (oldRelative == null || inUse.contains(oldRelative) || !oldRelative.startsWith(ProjectStore.IMAGES + "/")) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        new File(projectDir, oldRelative).delete();
    }

    @SuppressWarnings("deprecation")
    public static Bitmap.CompressFormat webp() {
        // WEBP_LOSSY baru ada di Android 11; sebelumnya WEBP dengan kualitas < 100 juga lossy.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? Bitmap.CompressFormat.WEBP_LOSSY
                : Bitmap.CompressFormat.WEBP;
    }

    private static Bitmap rotate(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(270);
                matrix.postScale(-1, 1);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /** inSampleSize terbesar (kelipatan 2) yang sisi terpanjangnya masih ≥ maxSide. */
    static int sampleSize(int width, int height, int maxSide) {
        int longest = Math.max(width, height);
        int sample = 1;
        while (longest / (sample * 2) >= maxSide) {
            sample *= 2;
        }
        return sample;
    }

    /**
     * Potongan tengah sesuai rasio (bagian 6.6: potong tengah otomatis).
     *
     * @return {x, y, lebar, tinggi}
     */
    static int[] cropRect(int width, int height, @Nullable String aspectRatio) {
        float ratio = parseRatio(aspectRatio);
        if (ratio <= 0) {
            return new int[] {0, 0, width, height};
        }
        if (width / (float) height > ratio) {
            int w = Math.max(1, Math.round(height * ratio));
            return new int[] {(width - w) / 2, 0, w, height};
        }
        int h = Math.max(1, Math.round(width / ratio));
        return new int[] {0, (height - h) / 2, width, h};
    }

    /** "16:9" → 1,777…; tidak terbaca → 0. */
    static float parseRatio(@Nullable String aspectRatio) {
        if (aspectRatio == null) {
            return 0;
        }
        String[] parts = aspectRatio.split(":");
        if (parts.length != 2) {
            return 0;
        }
        try {
            float w = Float.parseFloat(parts[0].trim());
            float h = Float.parseFloat(parts[1].trim());
            return w > 0 && h > 0 ? w / h : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String safeKey(String key) {
        String clean = key.replaceAll("[^A-Za-z0-9_-]", "");
        return clean.isEmpty() ? "gambar" : clean;
    }
}
