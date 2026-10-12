package com.aris.templateapp.upload.check;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Satu rujukan di HTML/CSS ({@code src}, {@code href}, {@code url(...)}) yang sudah dikelompokkan:
 * file lokal di ZIP, alamat luar, data URI, atau skema khusus (mailto:, tel:, ...).
 *
 * @param path        path lokal relatif terhadap folder utama; null jika bukan LOCAL atau keluar dari folder ZIP
 * @param host        host huruf kecil untuk EXTERNAL
 * @param fragment    bagian setelah # (tanpa #), atau null
 * @param rootAbsolute rujukan lokal yang diawali "/" (bawaan Vite), dibaca dari folder utama
 */
record Ref(Kind kind, String raw, String path, String host, String fragment, boolean rootAbsolute) {

    enum Kind { LOCAL, EXTERNAL, DATA, SPECIAL, FRAGMENT, EMPTY }

    private static final Pattern SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:.*", Pattern.DOTALL);

    /**
     * @param fromFile file yang berisi rujukan; path relatif dihitung dari foldernya
     *                 (untuk CSS: dari folder file CSS, bukan dari halaman HTML)
     */
    static Ref of(String fromFile, String raw) {
        String value = raw == null ? "" : raw.strip();
        if (value.isEmpty()) {
            return new Ref(Kind.EMPTY, value, null, null, null, false);
        }
        if (value.startsWith("#")) {
            return new Ref(Kind.FRAGMENT, value, null, null, value.substring(1), false);
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.startsWith("data:")) {
            return new Ref(Kind.DATA, value, null, null, null, false);
        }
        if (value.startsWith("//") || lower.startsWith("http://") || lower.startsWith("https://")) {
            return new Ref(Kind.EXTERNAL, value, null, hostOf(value), null, false);
        }
        if (SCHEME.matcher(value).matches()) {
            return new Ref(Kind.SPECIAL, value, null, null, null, false);
        }

        String fragment = null;
        int hash = value.indexOf('#');
        if (hash >= 0) {
            fragment = value.substring(hash + 1);
            value = value.substring(0, hash);
        }
        int query = value.indexOf('?');
        if (query >= 0) {
            value = value.substring(0, query);
        }
        boolean rootAbsolute = value.startsWith("/");
        String base = rootAbsolute ? "" : folderOf(fromFile);
        String path = normalize(base + percentDecode(value));
        if (path != null && (path.isEmpty() || path.endsWith("/"))) {
            // Link ke folder (mis. "tentang/") dibuka sebagai index.html di folder itu.
            path = path + "index.html";
        }
        return new Ref(Kind.LOCAL, raw.strip(), path, null, fragment, rootAbsolute);
    }

    boolean isLocal() {
        return kind == Kind.LOCAL;
    }

    boolean isExternal() {
        return kind == Kind.EXTERNAL;
    }

    /** Path URL eksternal tanpa host (mis. "/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"). */
    String externalPath() {
        try {
            String full = raw.startsWith("//") ? "https:" + raw : raw;
            String p = URI.create(full.replace(" ", "%20")).getRawPath();
            return p == null ? "" : p;
        } catch (IllegalArgumentException e) {
            int start = raw.indexOf("//") + 2;
            int slash = raw.indexOf('/', start);
            return slash < 0 ? "" : raw.substring(slash);
        }
    }

    static String hostOf(String url) {
        String rest = url.startsWith("//") ? url.substring(2) : url.substring(url.indexOf("//") + 2);
        int end = rest.length();
        for (char stop : new char[]{'/', '?', '#', ':'}) {
            int i = rest.indexOf(stop);
            if (i >= 0 && i < end) {
                end = i;
            }
        }
        String host = rest.substring(0, end);
        int at = host.lastIndexOf('@');
        return (at >= 0 ? host.substring(at + 1) : host).toLowerCase(Locale.ROOT);
    }

    static String folderOf(String file) {
        int slash = file.lastIndexOf('/');
        return slash < 0 ? "" : file.substring(0, slash + 1);
    }

    /** Menyelesaikan "." dan ".."; null jika path keluar dari folder utama. */
    static String normalize(String path) {
        Deque<String> parts = new ArrayDeque<>();
        String[] segments = path.split("/", -1);
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            boolean last = i == segments.length - 1;
            if (segment.equals("..")) {
                if (parts.isEmpty()) {
                    return null;
                }
                parts.removeLast();
                if (last) {
                    parts.add("");
                }
            } else if (segment.equals(".") || (segment.isEmpty() && !last)) {
                if (last) {
                    parts.add("");
                }
            } else {
                parts.add(segment);
            }
        }
        return String.join("/", parts);
    }

    /** %20 → spasi, dst. Tanda "+" dibiarkan, karena di path URL "+" bukan spasi. */
    static String percentDecode(String value) {
        if (value.indexOf('%') < 0) {
            return value;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == '%' && i + 2 < bytes.length) {
                int hi = Character.digit(bytes[i + 1], 16);
                int lo = Character.digit(bytes[i + 2], 16);
                if (hi >= 0 && lo >= 0) {
                    out.write(hi * 16 + lo);
                    i += 2;
                    continue;
                }
            }
            out.write(bytes[i]);
        }
        return out.toString(StandardCharsets.UTF_8);
    }
}
