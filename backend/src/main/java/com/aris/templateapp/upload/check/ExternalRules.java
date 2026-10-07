package com.aris.templateapp.upload.check;

import com.aris.templateapp.config.AppProperties;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Bagian 5.6 E: library dan sumber dari luar (detail aturan di bagian 10.4). */
final class ExternalRules {

    private static final Pattern JSDELIVR_NPM = Pattern.compile("^/npm/((?:@[^/]+/)?[^/@]+)(?:@([^/]+))?(?:/.*)?$");
    private static final Pattern UNPKG = Pattern.compile("^/((?:@[^/]+/)?[^/@]+)(?:@([^/]+))?(?:/.*)?$");
    private static final Pattern CDNJS = Pattern.compile("^/ajax/libs/([^/]+)/([^/]+)/.*$");
    private static final Pattern JQUERY_CORE = Pattern.compile("^/jquery-(\\d+\\.\\d+\\.\\d+)(?:\\.slim)?(?:\\.min)?\\.js$");
    // Versi jelas = angka lengkap x.y.z (mis. 3.14.1). "@latest", "@3", atau tanpa versi bisa berubah diam-diam.
    private static final Pattern EXACT_VERSION = Pattern.compile("^\\d+\\.\\d+\\.\\d+(?:[-+][0-9A-Za-z.-]+)?$");
    private static final String TAILWIND_PLAY = "cdn.tailwindcss.com";

    /** Paket di CDN yang sudah dikenali: nama paket dan versinya (null jika tidak ditulis). */
    record CdnPackage(String name, String version) {
    }

    private final CheckContext ctx;
    private boolean googleFontsReported;
    private boolean tailwindPlayReported;

    ExternalRules(CheckContext ctx) {
        this.ctx = ctx;
    }

    void check() {
        for (Map.Entry<String, Document> page : ctx.pages.entrySet()) {
            String file = page.getKey();
            for (Element script : page.getValue().select("script[src]")) {
                checkResource(file, CheckContext.line(script), script.attr("src"), script.outerHtml());
            }
            for (Element link : page.getValue().select("link[href]")) {
                String rel = link.attr("rel").toLowerCase(Locale.ROOT);
                if (rel.contains("stylesheet") || rel.contains("preload") || rel.contains("modulepreload")) {
                    checkResource(file, CheckContext.line(link), link.attr("href"), link.outerHtml());
                }
            }
            for (Element iframe : page.getValue().select("iframe[src]")) {
                checkIframe(file, iframe);
            }
        }
    }

    /** Dipanggil juga oleh CssRules untuk {@code @import} dari luar. */
    void checkResource(String file, Integer line, String url, String snippet) {
        Ref ref = Ref.of(file, url);
        if (!ref.isExternal()) {
            return;
        }
        String host = ref.host();
        String full = ref.raw().toLowerCase(Locale.ROOT);

        if (host.equals("fonts.googleapis.com") || host.equals("fonts.gstatic.com")) {
            ctx.libraries.add("Google Fonts");
            if (!googleFontsReported) {
                googleFontsReported = true;
                ctx.findings.add(CheckRule.GOOGLE_FONTS, "Font dari Google Fonts butuh internet; tanpa internet font bawaan HP yang tampil.",
                        file, line, "Boleh dipakai. Jika ingin selalu tampil, sertakan file font (woff2) di ZIP.", snippet);
            }
            return;
        }
        if (matchesAny(full, ctx.settings.trackerPatterns(), true)) {
            ctx.findings.add(CheckRule.TRACKER, file + " memuat pelacak/analytics dari " + host + ".", file, line,
                    "Hapus script ini. Template tidak boleh melacak pengunjung website orang lain.", snippet);
            return;
        }
        if (matchesAny(full, ctx.settings.remoteDataPatterns(), false)) {
            ctx.findings.add(CheckRule.REMOTE_DATA_SDK, file + " memuat library yang mengambil data dari server (" + host + ").",
                    file, line, "Template harus statis. Tulis datanya langsung di HTML.", snippet);
            return;
        }
        if (host.equals(TAILWIND_PLAY)) {
            ctx.libraries.add("Tailwind (Play CDN)");
            if (!tailwindPlayReported) {
                tailwindPlayReported = true;
                ctx.findings.add(CheckRule.TAILWIND_PLAY_CDN, "Tailwind Play CDN tidak disarankan untuk website produksi karena lebih berat.",
                        file, line, "Sebaiknya pakai file CSS hasil build Tailwind.", snippet);
            }
            return;
        }
        if (!ctx.settings.trustedCdnHosts().contains(host)) {
            ctx.findings.add(CheckRule.UNTRUSTED_SOURCE, file + " memuat script/CSS dari " + host + ", bukan CDN terpercaya.",
                    file, line, "Sertakan file-nya di ZIP, atau muat dari jsDelivr, cdnjs, atau unpkg.", snippet);
            return;
        }

        CdnPackage pkg = parsePackage(host, ref.externalPath());
        AppProperties.AllowedLibrary library = pkg == null ? null : allowedLibrary(pkg.name());
        if (library == null) {
            ctx.findings.add(CheckRule.CDN_NOT_ALLOWED, file + " memuat " + (pkg == null ? ref.raw() : pkg.name())
                            + " dari CDN, tetapi library ini belum ada di daftar yang diizinkan.", file, line,
                    "Sertakan file library di ZIP, atau ajukan agar library ini ditambahkan.", snippet);
            return;
        }
        if (pkg.version() == null || !EXACT_VERSION.matcher(pkg.version()).matches()) {
            ctx.findings.add(CheckRule.CDN_VERSION_UNCLEAR, "Versi " + library.name() + " di " + file + " tidak jelas ("
                            + (pkg.version() == null ? "tanpa versi" : "@" + pkg.version()) + ").", file, line,
                    "Tulis versi lengkap, mis. " + pkg.name() + "@1.2.3, bukan @latest.", snippet);
            return;
        }
        ctx.libraries.add(library.name() + " " + pkg.version());
    }

    /** Mengenali nama & versi paket dari URL CDN terpercaya; null jika bentuk URL tidak dikenal. */
    static CdnPackage parsePackage(String host, String path) {
        Matcher m;
        switch (host) {
            case "cdn.jsdelivr.net" -> m = JSDELIVR_NPM.matcher(path);
            case "unpkg.com" -> m = UNPKG.matcher(path);
            case "cdnjs.cloudflare.com" -> m = CDNJS.matcher(path);
            case "code.jquery.com" -> {
                Matcher core = JQUERY_CORE.matcher(path);
                if (core.matches()) {
                    return new CdnPackage("jquery", core.group(1));
                }
                return path.startsWith("/jquery") ? new CdnPackage("jquery", null) : null;
            }
            default -> {
                return null;
            }
        }
        return m.matches() ? new CdnPackage(m.group(1), m.group(2)) : null;
    }

    private AppProperties.AllowedLibrary allowedLibrary(String packageName) {
        for (AppProperties.AllowedLibrary library : ctx.settings.allowedLibraries()) {
            for (String allowed : library.packages()) {
                if (allowed.equalsIgnoreCase(packageName)) {
                    return library;
                }
            }
        }
        return null;
    }

    private void checkIframe(String file, Element iframe) {
        Ref ref = Ref.of(file, iframe.attr("src"));
        if (!ref.isExternal()) {
            return; // iframe lokal dicek ReferenceRules
        }
        String hostAndPath = ref.host() + ref.externalPath();
        for (String allowed : ctx.settings.iframeAllowed()) {
            if (allowed.contains("/") ? hostAndPath.startsWith(allowed) : ref.host().equals(allowed)) {
                return;
            }
        }
        ctx.findings.add(CheckRule.IFRAME_NOT_ALLOWED, file + " memuat iframe dari " + ref.host() + ".", file,
                CheckContext.line(iframe), "Hanya iframe YouTube dan Google Maps yang diizinkan.", iframe.outerHtml());
    }

    /**
     * Pola berakhiran "(" (mis. {@code gtag(}) adalah pemanggilan fungsi: harus diawali karakter bukan nama,
     * agar {@code foohj(} tidak dianggap {@code hj(}. Pola lain dicocokkan sebagai potongan teks.
     */
    static boolean matchesAny(String text, List<String> patterns, boolean allowCalls) {
        for (String pattern : patterns) {
            if (pattern.endsWith("(")) {
                if (allowCalls && Pattern.compile("(?<![\\w$.])" + Pattern.quote(pattern.substring(0, pattern.length() - 1))
                        + "\\s*\\(").matcher(text).find()) {
                    return true;
                }
            } else if (text.contains(pattern.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
