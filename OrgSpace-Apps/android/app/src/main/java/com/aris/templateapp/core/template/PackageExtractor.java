package com.aris.templateapp.core.template;

import com.aris.templateapp.core.upload.ZipPaths;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Mengekstrak paket template dari server ke folder di HP (alur-buat-website-via-template.md bagian 5).
 * <p>
 * Berbeda dari ZIP provider (yang entri anehnya dilewati), paket dari server seharusnya selalu bersih. Jadi entri
 * dengan path berbahaya ({@code ../}, path absolut) membuat seluruh ekstrak <b>gagal</b> (cegah zip slip), begitu
 * juga isi yang melebihi batas (cegah zip bomb). Murni Java agar bisa diuji tanpa HP.
 */
public final class PackageExtractor {

    /** Paket dari server tidak mungkin sebesar ini; lebih dari ini dianggap rusak/berbahaya. */
    public static final long MAX_EXTRACTED_BYTES = 80L * 1024 * 1024;

    /** Paket tidak aman atau tidak lengkap. */
    public static class InvalidPackageException extends IOException {
        public InvalidPackageException(String message) {
            super(message);
        }
    }

    private PackageExtractor() {
    }

    /** @param target folder kosong tujuan ekstrak */
    public static void extract(InputStream zipStream, File target, long maxBytes) throws IOException {
        String canonicalTarget = target.getCanonicalPath() + File.separator;
        long total = 0;
        try (ZipInputStream zip = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            byte[] buffer = new byte[32 * 1024];
            while ((entry = zip.getNextEntry()) != null) {
                String name = ZipPaths.normalize(entry.getName());
                if (ZipPaths.isUnsafe(name)) {
                    throw new InvalidPackageException("Path berbahaya di paket: " + name);
                }
                File file = new File(target, name);
                if (!file.getCanonicalPath().startsWith(canonicalTarget)) {
                    throw new InvalidPackageException("Path berbahaya di paket: " + name);
                }
                if (entry.isDirectory()) {
                    mkdirs(file);
                    continue;
                }
                mkdirs(file.getParentFile());
                try (OutputStream out = new FileOutputStream(file)) {
                    int n;
                    while ((n = zip.read(buffer)) > 0) {
                        total += n;
                        if (total > maxBytes) {
                            throw new InvalidPackageException("Isi paket terlalu besar");
                        }
                        out.write(buffer, 0, n);
                    }
                }
            }
        }
        if (!new File(target, TemplateFiles.MANIFEST).isFile() || !new File(target, "index.html").isFile()) {
            throw new InvalidPackageException("Paket tidak lengkap (manifest.json atau index.html tidak ada)");
        }
    }

    private static void mkdirs(File dir) throws IOException {
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Folder tidak bisa dibuat: " + dir);
        }
    }
}
