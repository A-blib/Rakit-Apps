package com.aris.templateapp.core.upload;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Aturan path ZIP yang sama dengan server (alur-fitur-upload.md bagian 5.6 A): file sampah dibuang, dan folder utama
 * adalah folder tempat index.html berada (paling luar, atau di dalam satu folder pembungkus).
 */
public final class ZipPaths {

    private static final Set<String> JUNK = Set.of(".ds_store", "thumbs.db", "desktop.ini");

    private ZipPaths() {
    }

    /** Nama entri dengan pemisah "/" (ZIP buatan Windows kadang memakai "\"). */
    public static String normalize(String name) {
        return name.replace('\\', '/');
    }

    public static boolean isJunk(String path) {
        for (String part : path.split("/")) {
            if (part.equals("__MACOSX") || part.equals("node_modules")) {
                return true;
            }
        }
        String file = fileName(path);
        return JUNK.contains(file.toLowerCase(Locale.ROOT)) || file.startsWith("._");
    }

    /** Path yang mencoba keluar dari folder ekstrak (zip slip). */
    public static boolean isUnsafe(String path) {
        if (path.startsWith("/") || path.matches("^[A-Za-z]:.*")) {
            return true;
        }
        for (String part : path.split("/")) {
            if (part.equals("..")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Folder utama: "" jika index.html paling luar, "nama-folder/" jika semua file ada di satu folder pembungkus
     * yang berisi index.html, atau null jika index.html tidak ditemukan.
     */
    public static String findRoot(Collection<String> files) {
        if (files.contains("index.html")) {
            return "";
        }
        Set<String> top = new LinkedHashSet<>();
        for (String file : files) {
            int slash = file.indexOf('/');
            top.add(slash < 0 ? "" : file.substring(0, slash + 1));
        }
        if (top.size() == 1 && !top.contains("")) {
            String folder = top.iterator().next();
            return files.contains(folder + "index.html") ? folder : null;
        }
        return null;
    }

    public static String fileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }
}
