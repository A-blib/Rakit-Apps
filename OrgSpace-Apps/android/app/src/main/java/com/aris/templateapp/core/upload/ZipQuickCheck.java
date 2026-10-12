package com.aris.templateapp.core.upload;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Cek kilat di HP sebelum upload (alur-fitur-upload.md bagian 4): ZIP bisa dibuka, ada index.html, dan ukurannya
 * tidak melewati batas. Tujuannya agar file yang jelas rusak tidak perlu dikirim dan kuota provider tidak terbuang.
 * Pengecekan lengkap tetap dilakukan server.
 */
public final class ZipQuickCheck {

    public enum Problem { RAR, NOT_ZIP, CORRUPT, NO_INDEX, TOO_LARGE }

    /** Hasil cek kilat. {@code problem} null = lolos. */
    public static final class Result {
        @Nullable
        public final Problem problem;
        /** Untuk TOO_LARGE: 5 file terbesar, mis. "hero.jpg 6,2 MB" (bagian 5.7b). */
        public final List<String> largestFiles;
        /** Untuk NO_INDEX: letak index.html yang ditemukan di folder lebih dalam (mis. "dist/"), atau null. */
        @Nullable
        public final String deeperIndexFolder;

        Result(@Nullable Problem problem, List<String> largestFiles, @Nullable String deeperIndexFolder) {
            this.problem = problem;
            this.largestFiles = largestFiles;
            this.deeperIndexFolder = deeperIndexFolder;
        }

        public boolean passed() {
            return problem == null;
        }
    }

    /** Membuka isi file; dipanggil ulang jika perlu membaca dari awal. */
    public interface Source {
        InputStream open() throws IOException;
    }

    private static final byte[] ZIP_MAGIC = {'P', 'K', 3, 4};
    private static final byte[] RAR_MAGIC = {'R', 'a', 'r', '!'};

    private ZipQuickCheck() {
    }

    public static Result check(String fileName, long size, long maxBytes, Source source) throws IOException {
        String lower = fileName.toLowerCase(Locale.ROOT);
        byte[] head = new byte[4];
        int read;
        try (InputStream in = source.open()) {
            read = readFully(in, head);
        }
        if (lower.endsWith(".rar") || startsWith(head, read, RAR_MAGIC)) {
            return new Result(Problem.RAR, List.of(), null);
        }
        if (!startsWith(head, read, ZIP_MAGIC)) {
            return new Result(Problem.NOT_ZIP, List.of(), null);
        }

        Map<String, Long> sizes = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(source.open())) {
            ZipEntry entry;
            byte[] buffer = new byte[16 * 1024];
            while ((entry = zip.getNextEntry()) != null) {
                long entrySize = 0;
                int n;
                // Membaca isi entri sekaligus memastikan datanya tidak rusak.
                while ((n = zip.read(buffer)) > 0) {
                    entrySize += n;
                }
                String name = ZipPaths.normalize(entry.getName());
                if (!entry.isDirectory() && !ZipPaths.isJunk(name)) {
                    sizes.put(name, entrySize);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            // ZipException (turunan IOException) = ZIP rusak; IllegalArgumentException = nama file tidak terbaca.
            return new Result(Problem.CORRUPT, List.of(), null);
        }

        if (size > maxBytes) {
            return new Result(Problem.TOO_LARGE, largest(sizes), null);
        }
        if (ZipPaths.findRoot(sizes.keySet()) == null) {
            String deeper = null;
            for (String name : sizes.keySet()) {
                if (name.endsWith("/index.html") && (deeper == null || name.length() < deeper.length())) {
                    deeper = name.substring(0, name.length() - "index.html".length());
                }
            }
            return new Result(Problem.NO_INDEX, List.of(), deeper);
        }
        return new Result(null, List.of(), null);
    }

    private static List<String> largest(Map<String, Long> sizes) {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(sizes.entrySet());
        entries.sort(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(5, entries.size()); i++) {
            result.add(ZipPaths.fileName(entries.get(i).getKey()) + " " + FileSizes.format(entries.get(i).getValue()));
        }
        return result;
    }

    private static int readFully(InputStream in, byte[] target) throws IOException {
        int total = 0;
        int n;
        while (total < target.length && (n = in.read(target, total, target.length - total)) > 0) {
            total += n;
        }
        return total;
    }

    private static boolean startsWith(byte[] data, int length, byte[] prefix) {
        if (length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
