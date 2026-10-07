package com.aris.templateapp.upload.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Isi ZIP yang sudah diekstrak ke memori, dengan path relatif terhadap folder utama (folder tempat index.html).
 * File yang terlalu besar tetap tercatat ukurannya, tetapi isinya tidak dimuat ({@link #content} = null).
 */
public final class TemplateFiles {

    private final String rootFolder;
    // TreeMap agar urutan file selalu sama, sehingga hasil pengecekan konsisten setiap kali dijalankan.
    private final Map<String, byte[]> contents = new TreeMap<>();
    private final Map<String, Long> sizes = new TreeMap<>();
    private final Map<String, String> lowerCaseIndex = new HashMap<>();
    private final List<String> ignored = new ArrayList<>();
    private boolean hasNodeModules;

    TemplateFiles(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    void put(String path, long size, byte[] content) {
        sizes.put(path, size);
        if (content != null) {
            contents.put(path, content);
        }
        lowerCaseIndex.put(path.toLowerCase(Locale.ROOT), path);
    }

    /** Mengeluarkan file dari template (mis. package.json di samping hasil build) tanpa menganggapnya masalah. */
    void ignore(String path) {
        sizes.remove(path);
        contents.remove(path);
        lowerCaseIndex.remove(path.toLowerCase(Locale.ROOT));
        ignored.add(path);
    }

    void setHasNodeModules(boolean hasNodeModules) {
        this.hasNodeModules = hasNodeModules;
    }

    /** ZIP ikut membawa folder node_modules (tanda kode sumber yang belum di-build). */
    public boolean hasNodeModules() {
        return hasNodeModules;
    }

    /** Folder pembungkus yang dianggap folder utama (mis. "toko-kue/"), atau "" jika index.html di paling luar. */
    public String rootFolder() {
        return rootFolder;
    }

    public Set<String> paths() {
        return Collections.unmodifiableSet(sizes.keySet());
    }

    public boolean exists(String path) {
        return sizes.containsKey(path);
    }

    /** Path asli jika ada file yang namanya sama kecuali huruf besar/kecil. */
    public String findIgnoringCase(String path) {
        return lowerCaseIndex.get(path.toLowerCase(Locale.ROOT));
    }

    public byte[] content(String path) {
        return contents.get(path);
    }

    public long size(String path) {
        return sizes.getOrDefault(path, 0L);
    }

    public long totalSize() {
        return sizes.values().stream().mapToLong(Long::longValue).sum();
    }

    /** File yang dibuang/diabaikan (file sampah, konfigurasi build). */
    public List<String> ignored() {
        return Collections.unmodifiableList(ignored);
    }

    /** Halaman HTML, index.html selalu pertama. */
    public List<String> pages() {
        List<String> pages = new ArrayList<>();
        for (String path : sizes.keySet()) {
            String ext = Texts.extension(path);
            if (ext.equals("html") || ext.equals("htm")) {
                pages.add(path);
            }
        }
        pages.sort((a, b) -> a.equals("index.html") ? -1 : b.equals("index.html") ? 1 : a.compareTo(b));
        return pages;
    }

    public List<String> withExtension(String... extensions) {
        List<String> result = new ArrayList<>();
        for (String path : sizes.keySet()) {
            String ext = Texts.extension(path);
            for (String wanted : extensions) {
                if (ext.equals(wanted)) {
                    result.add(path);
                    break;
                }
            }
        }
        return result;
    }
}
