package com.aris.templateapp.upload.publish;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.upload.check.TemplateFiles;
import com.aris.templateapp.upload.marking.MarkingData;
import com.aris.templateapp.upload.marking.TemplateNumbering;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Membuat paket template yang dipajang (alur-fitur-upload.md bagian 9 & 10.5):
 * <ol>
 *   <li>library dari CDN terpercaya diunduh dan disimpan di {@code vendor/}, lalu link di HTML diganti ke file lokal
 *       (file CSS library ikut membawa font/gambar yang dirujuknya, agar ikon tetap tampil saat offline);</li>
 *   <li>atribut {@code data-edit}, {@code data-key}, {@code data-label}, {@code data-section} disisipkan ke HTML asli,
 *       memakai nomor elemen yang sama dengan editor di HP.</li>
 * </ol>
 * File asli provider tidak diubah; paket adalah salinan baru. Google Fonts tetap dimuat dari internet (bagian 10.5).
 */
public class PackageBuilder {

    /** Library gagal disalin; template tidak boleh tayang dengan link CDN yang belum disalin. */
    public static class LibraryCopyException extends Exception {
        public LibraryCopyException(String message) {
            super(message);
        }
    }

    private static final long MAX_LIBRARY_BYTES = 5L * 1024 * 1024;
    private static final Pattern JSDELIVR = Pattern.compile("^/npm/((?:@[^/]+/)?[^/@]+)@(\\d+\\.\\d+\\.\\d+[^/]*)(/.*)?$");
    private static final Pattern UNPKG = Pattern.compile("^/((?:@[^/]+/)?[^/@]+)@(\\d+\\.\\d+\\.\\d+[^/]*)(/.*)?$");
    private static final Pattern CDNJS = Pattern.compile("^/ajax/libs/([^/]+)/([^/]+)(/.*)$");
    private static final Pattern JQUERY = Pattern.compile("^/(jquery-(\\d+\\.\\d+\\.\\d+)(?:\\.slim)?(?:\\.min)?\\.js)$");
    private static final Pattern CSS_URL = Pattern.compile("url\\(\\s*(['\"]?)([^'\")]+)\\1\\s*\\)");
    private static final String TAILWIND_PLAY = "cdn.tailwindcss.com";

    private final AppProperties.Upload settings;
    private final LibraryFetcher fetcher;
    private final Map<String, String> localPathByUrl = new HashMap<>();
    private final Map<String, byte[]> vendorFiles = new LinkedHashMap<>();

    public PackageBuilder(AppProperties.Upload settings, LibraryFetcher fetcher) {
        this.settings = settings;
        this.fetcher = fetcher;
    }

    /** @return isi ZIP paket template */
    public byte[] build(TemplateFiles files, MarkingData marking) throws LibraryCopyException, IOException {
        Map<String, byte[]> output = new LinkedHashMap<>();
        for (String path : files.paths()) {
            byte[] content = files.content(path);
            if (content == null) {
                continue;
            }
            if (files.pages().contains(path)) {
                content = buildPage(path, new String(content, StandardCharsets.UTF_8), marking)
                        .getBytes(StandardCharsets.UTF_8);
            }
            output.put(path, content);
        }
        vendorFiles.forEach(output::putIfAbsent);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, byte[]> entry : output.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    String buildPage(String page, String html, MarkingData marking) throws LibraryCopyException {
        Document doc = TemplateNumbering.number(html);
        Map<Integer, Element> byId = new HashMap<>();
        for (Element element : doc.select("[" + TemplateNumbering.ATTRIBUTE + "]")) {
            byId.put(Integer.parseInt(element.attr(TemplateNumbering.ATTRIBUTE)), element);
        }
        for (MarkingData.Section section : list(marking.sections())) {
            Element element = section.tplId() == null || !page.equals(section.page()) ? null : byId.get(section.tplId());
            if (element != null) {
                element.attr("data-section", section.name());
            }
        }
        for (MarkingData.Field field : list(marking.fields())) {
            for (MarkingData.Element target : field.elements()) {
                Element element = page.equals(target.page()) ? byId.get(target.tplId()) : null;
                if (element != null) {
                    element.attr("data-edit", field.type());
                    element.attr("data-key", field.key());
                    element.attr("data-label", field.label());
                }
            }
        }
        for (Element script : doc.select("script[src]")) {
            copyLibrary(page, script, "src");
        }
        for (Element link : doc.select("link[href]")) {
            if (link.attr("rel").toLowerCase(Locale.ROOT).contains("stylesheet")) {
                copyLibrary(page, link, "href");
            }
        }
        doc.select("[" + TemplateNumbering.ATTRIBUTE + "]").removeAttr(TemplateNumbering.ATTRIBUTE);
        return doc.outerHtml();
    }

    private void copyLibrary(String page, Element element, String attribute) throws LibraryCopyException {
        String url = element.attr(attribute).strip();
        if (url.startsWith("//")) {
            url = "https:" + url;
        }
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            return;
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            return;
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (!settings.trustedCdnHosts().contains(host)) {
            return; // Google Fonts dan sumber lain tidak disalin
        }
        String local = localPath(host, uri.getRawPath());
        if (local == null) {
            return;
        }
        download(url, local);
        element.attr(attribute, relativeFrom(page, local));
        // SRI (integrity) tetap cocok karena isi file sama persis; crossorigin tidak dibutuhkan untuk file lokal.
        element.removeAttr("crossorigin");
    }

    private void download(String url, String local) throws LibraryCopyException {
        if (localPathByUrl.containsKey(url)) {
            return;
        }
        byte[] content;
        try {
            content = fetcher.fetch(url, MAX_LIBRARY_BYTES);
        } catch (IOException e) {
            throw new LibraryCopyException("Library " + url + " gagal disalin: " + e.getMessage());
        }
        localPathByUrl.put(url, local);
        vendorFiles.put(local, content);
        if (local.endsWith(".css")) {
            copyCssAssets(url, new String(content, StandardCharsets.UTF_8));
        }
    }

    /** Font/gambar yang dirujuk CSS library dengan path relatif ikut disalin dengan susunan folder yang sama. */
    private void copyCssAssets(String cssUrl, String css) throws LibraryCopyException {
        Matcher m = CSS_URL.matcher(css);
        URI base = URI.create(cssUrl);
        while (m.find()) {
            String ref = m.group(2).strip();
            if (ref.startsWith("data:") || ref.startsWith("#") || ref.contains("://") || ref.startsWith("//")) {
                continue;
            }
            URI resolved;
            try {
                resolved = base.resolve(ref.split("[?#]")[0]);
            } catch (IllegalArgumentException e) {
                continue;
            }
            String host = resolved.getHost() == null ? "" : resolved.getHost().toLowerCase(Locale.ROOT);
            String local = localPath(host, resolved.getRawPath());
            if (local != null) {
                download(resolved.toString(), local);
            }
        }
    }

    /** Path lokal di paket untuk file CDN, mengikuti susunan folder di CDN agar rujukan relatif CSS tetap benar. */
    static String localPath(String host, String path) {
        Matcher m;
        switch (host) {
            case "cdn.jsdelivr.net" -> m = JSDELIVR.matcher(path);
            case "unpkg.com" -> m = UNPKG.matcher(path);
            case "cdnjs.cloudflare.com" -> m = CDNJS.matcher(path);
            case "code.jquery.com" -> {
                Matcher jq = JQUERY.matcher(path);
                return jq.matches() ? "vendor/jquery@" + jq.group(2) + "/" + jq.group(1) : null;
            }
            case TAILWIND_PLAY -> {
                String rest = path == null || path.isEmpty() || path.equals("/") ? "/tailwind.js" : path;
                return "vendor/tailwindcss-play" + safe(rest);
            }
            default -> {
                return null;
            }
        }
        if (!m.matches()) {
            return null;
        }
        String name = m.group(1).replace("@", "").replace('/', '-');
        String rest = m.group(3) == null || m.group(3).equals("/") ? "/index.js" : m.group(3);
        return "vendor/" + name + "@" + m.group(2) + safe(rest);
    }

    /** Membuang segmen ".." agar path tidak keluar dari folder vendor. */
    private static String safe(String path) {
        StringBuilder out = new StringBuilder();
        for (String part : path.split("/")) {
            if (!part.isEmpty() && !part.equals(".") && !part.equals("..")) {
                out.append('/').append(part);
            }
        }
        return out.toString();
    }

    /** "blog/a.html" + "vendor/x.js" → "../vendor/x.js". */
    static String relativeFrom(String page, String target) {
        int depth = page.split("/").length - 1;
        return "../".repeat(depth) + target;
    }

    private static <T> List<T> list(List<T> list) {
        return list == null ? List.of() : list;
    }
}
