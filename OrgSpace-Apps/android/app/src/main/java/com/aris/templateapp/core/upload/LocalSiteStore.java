package com.aris.templateapp.core.upload;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Folder pribadi app tempat ZIP template diekstrak untuk ditampilkan di WebView (alur-fitur-upload.md bagian 7.9):
 * {@code files/upload-sites/<templateId>/}. File provider tidak pernah diubah; salinan bernomor untuk mode tandai
 * dibuat terpisah.
 */
@Singleton
public class LocalSiteStore {

    private static final String FOLDER = "upload-sites";
    private static final String ROOT_MARKER = ".root";
    private static final long MAX_EXTRACTED_BYTES = 60L * 1024 * 1024;

    private final File base;

    @Inject
    public LocalSiteStore(@ApplicationContext Context context) {
        this.base = new File(context.getFilesDir(), FOLDER);
    }

    /** Folder utama situs (tempat index.html), atau null jika belum diekstrak. */
    @Nullable
    public File siteRoot(String templateId) {
        File dir = new File(base, templateId);
        File marker = new File(dir, ROOT_MARKER);
        if (!marker.exists()) {
            return null;
        }
        String root = readMarker(marker);
        File result = root.isEmpty() ? new File(dir, "files") : new File(new File(dir, "files"), root);
        return new File(result, "index.html").exists() ? result : null;
    }

    /**
     * Mengekstrak ZIP dengan aman: path {@code ../} dan path absolut dilewati (zip slip), total isi dibatasi
     * (zip bomb). Hasil lama untuk template yang sama diganti.
     */
    @WorkerThread
    public File extract(String templateId, InputStream zipStream) throws IOException {
        File dir = new File(base, templateId);
        deleteRecursively(dir);
        File files = new File(dir, "files");
        if (!files.mkdirs()) {
            throw new IOException("Folder tidak bisa dibuat");
        }
        String canonicalFiles = files.getCanonicalPath() + File.separator;
        List<String> names = new ArrayList<>();
        long total = 0;
        try (ZipInputStream zip = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            byte[] buffer = new byte[32 * 1024];
            while ((entry = zip.getNextEntry()) != null) {
                String name = ZipPaths.normalize(entry.getName());
                if (entry.isDirectory() || ZipPaths.isJunk(name) || ZipPaths.isUnsafe(name)) {
                    continue;
                }
                File target = new File(files, name);
                if (!target.getCanonicalPath().startsWith(canonicalFiles)) {
                    continue;
                }
                File parent = target.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    throw new IOException("Folder tidak bisa dibuat");
                }
                try (OutputStream out = new FileOutputStream(target)) {
                    int n;
                    while ((n = zip.read(buffer)) > 0) {
                        total += n;
                        if (total > MAX_EXTRACTED_BYTES) {
                            throw new IOException("Isi ZIP terlalu besar");
                        }
                        out.write(buffer, 0, n);
                    }
                }
                names.add(name);
            }
        }
        String root = ZipPaths.findRoot(names);
        if (root == null) {
            throw new IOException("index.html tidak ditemukan");
        }
        try (OutputStream out = new FileOutputStream(new File(dir, ROOT_MARKER))) {
            out.write(root.getBytes(StandardCharsets.UTF_8));
        }
        return root.isEmpty() ? files : new File(files, root);
    }

    public void delete(String templateId) {
        deleteRecursively(new File(base, templateId));
    }

    private static String readMarker(File marker) {
        try (InputStream in = new FileInputStream(marker)) {
            byte[] bytes = new byte[(int) marker.length()];
            int total = 0;
            int n;
            while (total < bytes.length && (n = in.read(bytes, total, bytes.length - total)) > 0) {
                total += n;
            }
            return new String(bytes, 0, total, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
