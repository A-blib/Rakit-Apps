package com.aris.templateapp.core.template;

import androidx.annotation.Nullable;

import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.TemplateManifest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Membuat ZIP website dari paket template + nilai project (alur-buat-website-via-template.md bagian 8.1).
 * <ol>
 *   <li>isi paket disalin (paket asli di HP tidak diubah; semua perubahan terjadi di memori);</li>
 *   <li>setiap halaman diparse jsoup, nilai diterapkan ke {@code [data-key]} (teks selalu sebagai teks biasa);</li>
 *   <li>gambar project disalin ke {@code img/user/};</li>
 *   <li>{@code custom.css} dibuat dan dimuat paling akhir di {@code <head>};</li>
 *   <li>atribut khusus app dihapus agar HTML bersih;</li>
 *   <li>file yang tidak dipakai (manifest, gambar contoh yang sudah diganti) dibuang.</li>
 * </ol>
 * Murni Java (tanpa Android) agar bisa diuji dengan paket contoh di laptop.
 */
public final class ProjectExporter {

    public static final String USER_IMAGES = "img/user/";
    public static final String CUSTOM_CSS = "custom.css";
    static final String[] APP_ATTRIBUTES = {"data-edit", "data-key", "data-label", "data-section", "data-tpl-id"};

    private static final Pattern BACKGROUND = Pattern.compile("background-image\\s*:[^;]*;?", Pattern.CASE_INSENSITIVE);
    private static final Set<String> TEXT_EXTENSIONS = Set.of("html", "htm", "css", "js", "mjs", "json", "svg", "txt");

    /** Pengecil foto bawaan template (opsi "Kompres foto agar ringan"); implementasinya memakai Bitmap Android. */
    public interface ImageCompressor {
        /** @return isi baru yang lebih kecil, atau null jika tidak bisa/tidak lebih kecil */
        @Nullable
        byte[] compress(String path, byte[] content);
    }

    public interface Progress {
        void onProgress(int done, int total);
    }

    private ProjectExporter() {
    }

    /** Hasil yang dibutuhkan layar Export (jumlah file & ukuran). */
    public static final class Result {
        public final int fileCount;
        public final long bytes;

        Result(int fileCount, long bytes) {
            this.fileCount = fileCount;
            this.bytes = bytes;
        }
    }

    public static Result export(File packageDir, File projectDir, TemplateManifest manifest, ProjectValues values,
                                @Nullable ImageCompressor compressor, OutputStream out, @Nullable Progress progress)
            throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<>();
        collect(packageDir, "", files);
        files.remove(TemplateFiles.MANIFEST);

        String css = CustomCss.build(manifest, values, CustomCss.Target.EXPORT);
        Map<String, String> userImages = new LinkedHashMap<>();
        Set<String> replacedSamples = new LinkedHashSet<>();
        for (TemplateManifest.Field field : manifest.fields) {
            ProjectValues.FieldValue value = values.peek(field.key);
            if (value != null && value.image != null && TemplateManifest.TYPE_IMAGE.equals(field.type)) {
                String name = new File(value.image).getName();
                userImages.put(field.key, USER_IMAGES + name);
                if (field.sample != null && !field.sample.contains("://")) {
                    replacedSamples.add(field.sample);
                }
            }
        }

        List<String> pages = new ArrayList<>();
        for (String path : files.keySet()) {
            String lower = path.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".html") || lower.endsWith(".htm")) {
                pages.add(path);
            }
        }
        int total = pages.size() + 1;
        int done = 0;
        for (String page : pages) {
            String html = new String(files.get(page), StandardCharsets.UTF_8);
            files.put(page, applyPage(page, html, manifest, values, userImages, !css.isEmpty())
                    .getBytes(StandardCharsets.UTF_8));
            if (progress != null) {
                progress.onProgress(++done, total);
            }
        }
        for (Map.Entry<String, String> image : userImages.entrySet()) {
            ProjectValues.FieldValue value = values.peek(image.getKey());
            File source = new File(projectDir, value.image);
            if (source.isFile()) {
                files.put(image.getValue(), read(source));
            }
        }
        if (!css.isEmpty()) {
            files.put(CUSTOM_CSS, css.getBytes(StandardCharsets.UTF_8));
        }
        removeUnusedSamples(files, replacedSamples);
        if (compressor != null) {
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (!file.getKey().startsWith(USER_IMAGES)) {
                    byte[] smaller = compressor.compress(file.getKey(), file.getValue());
                    if (smaller != null && smaller.length < file.getValue().length) {
                        file.setValue(smaller);
                    }
                }
            }
        }

        long bytes = 0;
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue());
                zip.closeEntry();
                bytes += file.getValue().length;
            }
        }
        if (progress != null) {
            progress.onProgress(total, total);
        }
        return new Result(files.size(), bytes);
    }

    /** Satu halaman: nilai diterapkan, class gaya ditambahkan, custom.css dimuat paling akhir, atribut app dihapus. */
    static String applyPage(String page, String html, TemplateManifest manifest, ProjectValues values,
                            Map<String, String> userImages, boolean hasCss) {
        Document doc = Jsoup.parse(html, "", Parser.htmlParser());
        // Tanpa pretty print agar spasi & baris asli template tidak berubah.
        doc.outputSettings().prettyPrint(false);
        for (TemplateManifest.Field field : manifest.fields) {
            ProjectValues.FieldValue value = values.peek(field.key);
            boolean styled = CustomCss.hasStyle(field, values);
            if (value == null && !styled) {
                continue;
            }
            for (Element element : doc.select("[data-key]")) {
                if (!element.attr("data-key").equals(field.key)) {
                    continue;
                }
                if (value != null) {
                    applyValue(page, element, field, value, userImages.get(field.key));
                }
                if (styled) {
                    element.addClass(CustomCss.EXPORT_CLASS_PREFIX + field.key);
                }
            }
        }
        if (hasCss) {
            Element head = doc.head();
            head.appendElement("link").attr("rel", "stylesheet").attr("href", relative(page, CUSTOM_CSS));
        }
        for (String attribute : APP_ATTRIBUTES) {
            doc.select("[" + attribute + "]").removeAttr(attribute);
        }
        return doc.outerHtml();
    }

    private static void applyValue(String page, Element element, TemplateManifest.Field field,
                                   ProjectValues.FieldValue value, @Nullable String userImage) {
        if (value.text != null && field.hasText()) {
            setText(element, value.text);
        }
        // Link yang belum valid (termasuk javascript:) tidak dipasang; link bawaan template dipertahankan.
        if (value.href != null && field.hasHref() && LinkRules.isValid(value.href)) {
            element.attr("href", value.href.trim());
        }
        if (userImage != null && TemplateManifest.TYPE_IMAGE.equals(field.type)) {
            String src = relative(page, userImage);
            if (element.tagName().equals("img")) {
                element.attr("src", src);
                element.removeAttr("srcset");
            } else {
                String style = BACKGROUND.matcher(element.attr("style")).replaceAll("").trim();
                String rule = "background-image: url('" + src + "');";
                element.attr("style", style.isEmpty() ? rule : (style.endsWith(";") ? style : style + ";") + " " + rule);
            }
        }
    }

    /** Teks biasa (bukan HTML), ikon {@code <i>/<svg>} di dalam tombol dipertahankan seperti di preview. */
    static void setText(Element element, String text) {
        Element icon = element.selectFirst("i, svg");
        Element keep = icon == null ? null : icon.clone();
        element.text(text);
        if (keep != null) {
            element.prependChild(new TextNode(" "));
            element.prependChild(keep);
        }
    }

    /** "blog/a.html" + "img/user/x.webp" → "../img/user/x.webp". */
    static String relative(String page, String target) {
        int depth = page.split("/").length - 1;
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            prefix.append("../");
        }
        return prefix + target;
    }

    /** Gambar contoh yang sudah diganti dibuang, kecuali masih dirujuk file teks lain (mis. CSS atau halaman lain). */
    private static void removeUnusedSamples(Map<String, byte[]> files, Set<String> samples) {
        for (String sample : samples) {
            if (!files.containsKey(sample)) {
                continue;
            }
            String name = new File(sample).getName();
            boolean referenced = false;
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (isText(file.getKey()) && new String(file.getValue(), StandardCharsets.UTF_8).contains(name)) {
                    referenced = true;
                    break;
                }
            }
            if (!referenced) {
                files.remove(sample);
            }
        }
    }

    private static boolean isText(String path) {
        int dot = path.lastIndexOf('.');
        return dot >= 0 && TEXT_EXTENSIONS.contains(path.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    private static void collect(File dir, String prefix, Map<String, byte[]> files) throws IOException {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        java.util.Arrays.sort(children);
        for (File child : children) {
            if (child.isDirectory()) {
                collect(child, prefix + child.getName() + "/", files);
            } else {
                files.put(prefix + child.getName(), read(child));
            }
        }
    }

    // InputStream.readAllBytes baru ada di Android 13, sedangkan minSdk app 26.
    private static byte[] read(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream((int) Math.max(32, file.length()));
            byte[] buffer = new byte[32 * 1024];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }
}
