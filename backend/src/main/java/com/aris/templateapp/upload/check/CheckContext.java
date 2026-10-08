package com.aris.templateapp.upload.check;

import com.aris.templateapp.config.AppProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Bahan bersama untuk tahap B: file hasil ekstrak, halaman HTML yang sudah di-parse, pengaturan, dan penampung
 * masalah. Setiap kelompok aturan (HtmlRules, CssRules, ...) membaca dari sini.
 */
final class CheckContext {

    /** Satu kode CSS: file .css, isi tag {@code <style>}, atau atribut {@code style}. */
    record CssSource(String file, Integer line, String code) {
    }

    /** Satu kode JavaScript milik provider: file .js atau isi tag {@code <script>}. */
    record JsSource(String file, Integer line, String code, boolean module) {
    }

    final AppProperties.Upload settings;
    final TemplateFiles files;
    final Findings findings;
    final Map<String, Document> pages = new LinkedHashMap<>();
    // Hash file JS yang dikenali sebagai library terkenal (tidak dipindai ulang, bagian 5.6 F).
    final Set<String> knownLibraryFiles = new HashSet<>();
    // Library yang terdeteksi untuk info teknis (bagian 6.1), mis. "Bootstrap 5.3.3".
    final Set<String> libraries = new LinkedHashSet<>();
    // File lokal yang dimuat setiap halaman (CSS, JS, gambar) dan yang dimuat setiap file CSS (url(), @import),
    // untuk menghitung berat halaman (bagian 5.6 G).
    final Map<String, Set<String>> pageAssets = new LinkedHashMap<>();
    final Map<String, Set<String>> cssAssets = new LinkedHashMap<>();
    // Variabel CSS di :root (mis. --primary), bahan tema global di editor tandai (bagian 7.11).
    final Map<String, String> cssVariables = new LinkedHashMap<>();
    boolean responsive;
    private final Set<String> reportedCaseMismatch = new HashSet<>();

    CheckContext(AppProperties.Upload settings, TemplateFiles files, Findings findings) {
        this.settings = settings;
        this.files = files;
        this.findings = findings;
    }

    AppProperties.Limits limits() {
        return settings.limits();
    }

    /** Parse semua halaman. trackPosition dinyalakan agar setiap masalah bisa menyebut nomor barisnya. */
    void parsePages() {
        for (String page : files.pages()) {
            byte[] bytes = files.content(page);
            if (bytes == null) {
                continue;
            }
            String html = Texts.decode(bytes);
            pages.put(page, Jsoup.parse(html, "", Parser.htmlParser().setTrackPosition(true)));
        }
    }

    String text(String path) {
        byte[] bytes = files.content(path);
        return bytes == null ? null : Texts.decode(bytes);
    }

    static Integer line(Element element) {
        int line = element.sourceRange().start().lineNumber();
        return line > 0 ? line : null;
    }

    /** Semua kode CSS: file .css + tag style + atribut style di setiap halaman. */
    List<CssSource> cssSources() {
        List<CssSource> sources = new ArrayList<>();
        for (String css : files.withExtension("css")) {
            String code = text(css);
            if (code != null) {
                sources.add(new CssSource(css, null, code));
            }
        }
        pages.forEach((page, doc) -> {
            for (Element style : doc.select("style")) {
                sources.add(new CssSource(page, line(style), style.data()));
            }
            for (Element el : doc.select("[style]")) {
                sources.add(new CssSource(page, line(el), el.attr("style")));
            }
        });
        return sources;
    }

    /** Kode JS milik provider: file .js/.mjs yang bukan library terkenal + script inline di setiap halaman. */
    List<JsSource> ownScripts() {
        List<JsSource> sources = new ArrayList<>();
        for (String js : files.withExtension("js", "mjs")) {
            if (knownLibraryFiles.contains(js)) {
                continue;
            }
            String code = text(js);
            if (code != null) {
                sources.add(new JsSource(js, null, code, Texts.extension(js).equals("mjs")));
            }
        }
        pages.forEach((page, doc) -> {
            for (Element script : doc.select("script:not([src])")) {
                String type = script.attr("type").toLowerCase(Locale.ROOT);
                // JSON-LD, template, dsb. bukan kode yang dijalankan browser.
                if (type.isEmpty() || type.contains("javascript") || type.equals("module")) {
                    sources.add(new JsSource(page, line(script), script.data(), type.equals("module")));
                }
            }
            for (Element script : doc.select("script[src][type=module]")) {
                Ref ref = Ref.of(page, script.attr("src"));
                if (ref.isLocal() && ref.path() != null) {
                    sources.stream().filter(s -> s.file().equals(ref.path())).findFirst().ifPresent(found -> {
                        sources.remove(found);
                        sources.add(new JsSource(found.file(), null, found.code(), true));
                    });
                }
            }
        });
        return sources;
    }

    /** Hasil mencari file lokal: ada, ada dengan huruf besar/kecil berbeda, atau tidak ada. */
    enum Lookup { FOUND, CASE_MISMATCH, MISSING }

    /**
     * Mencari file yang dirujuk. Jika hanya beda huruf besar/kecil, sekaligus mencatat Error CASE_MISMATCH
     * (di Windows/macOS terlihat normal, tetapi di hosting Linux dan Android file tidak ditemukan).
     */
    Lookup lookup(Ref ref, String fromFile, Integer line) {
        if (ref.path() == null) {
            return Lookup.MISSING;
        }
        if (files.exists(ref.path())) {
            return Lookup.FOUND;
        }
        String actual = files.findIgnoringCase(ref.path());
        if (actual == null) {
            return Lookup.MISSING;
        }
        if (reportedCaseMismatch.add(fromFile + "|" + ref.path())) {
            findings.add(CheckRule.CASE_MISMATCH, fromFile + (line != null ? " baris " + line : "") + " memanggil "
                            + ref.path() + ", tapi nama filenya " + actual + ".", fromFile, line,
                    "Samakan huruf besar/kecil nama file dan path-nya (sebaiknya huruf kecil semua), lalu ZIP ulang.",
                    ref.raw());
        }
        return Lookup.CASE_MISMATCH;
    }

    static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
