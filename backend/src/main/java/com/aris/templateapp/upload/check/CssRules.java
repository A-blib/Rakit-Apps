package com.aris.templateapp.upload.check;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Bagian 5.6 C2: CSS. Sekaligus mendeteksi variabel {@code :root} untuk fitur tema global (bagian 7.11). */
final class CssRules {

    private static final Pattern URL = Pattern.compile("url\\(\\s*(['\"]?)(.*?)\\1\\s*\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMPORT = Pattern.compile(
            "@import\\s+(?:url\\(\\s*)?(['\"]?)([^'\"\\s);]+)\\1", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_TRICK = Pattern.compile(
            "expression\\s*\\(|-moz-binding|(?<![\\w-])behavior\\s*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern MEDIA = Pattern.compile("@media", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROOT_BLOCK = Pattern.compile(":root\\s*\\{([^}]*)}");
    private static final Pattern VARIABLE = Pattern.compile("(--[\\w-]+)\\s*:\\s*([^;]+);?");
    // Framework yang sudah responsif walau CSS provider sendiri tidak punya @media.
    private static final Set<String> RESPONSIVE_FRAMEWORKS = Set.of("bootstrap", "tailwind", "bulma");

    private final CheckContext ctx;
    private final ExternalRules external;
    private boolean hasMedia;

    CssRules(CheckContext ctx, ExternalRules external) {
        this.ctx = ctx;
        this.external = external;
    }

    void check() {
        for (CheckContext.CssSource css : ctx.cssSources()) {
            boolean isFile = Texts.extension(css.file()).equals("css");
            boolean isAttribute = !isFile && !css.code().contains("{");
            JsScanner.Result scan = JsScanner.scanCss(css.code());
            String code = scan.code();

            if (scan.error() != null && !isAttribute) {
                int line = lineIn(css, code, scan.errorIndex());
                ctx.findings.add(CheckRule.CSS_SYNTAX, "Kesalahan penulisan CSS di " + css.file() + " baris " + line + ": "
                        + scan.error(), css.file(), line, "Periksa kurung kurawal { } dan tanda kutip di sekitar baris itu.");
            }
            Matcher trick = SCRIPT_TRICK.matcher(code);
            if (trick.find()) {
                ctx.findings.add(CheckRule.CSS_SCRIPT_TRICK, css.file() + " memakai trik lama yang menjalankan script lewat CSS.",
                        css.file(), lineIn(css, code, trick.start()), "Hapus expression(), -moz-binding, atau behavior:.",
                        HtmlRules.snippetAt(code, trick.start()));
            }
            if (MEDIA.matcher(code).find()) {
                hasMedia = true;
            }
            checkUrls(css, code);
            checkImports(css, code);
            collectVariables(code);

            if (isFile && ctx.files.size(css.file()) > ctx.limits().cssWarnBytes()) {
                ctx.findings.add(CheckRule.CSS_TOO_LARGE, css.file() + " berukuran " + Texts.size(ctx.files.size(css.file()))
                                + "; biasanya Tailwind/Bootstrap belum di-purge.", css.file(), null,
                        "Aktifkan purge/content di konfigurasi Tailwind, atau pakai file .min.css yang lebih kecil.");
            }
        }
        if (!hasMedia && !usesResponsiveFramework()) {
            ctx.findings.add(CheckRule.NOT_RESPONSIVE, "Tidak ada @media di CSS dan tidak memakai framework responsif. "
                    + "Tampilan mungkin tidak menyesuaikan layar HP.", null, null,
                    "Tambahkan aturan @media (max-width: 768px) { ... } untuk layar HP.");
        }
        ctx.responsive = hasMedia || usesResponsiveFramework();
    }

    private void checkUrls(CheckContext.CssSource css, String code) {
        Matcher m = URL.matcher(code);
        while (m.find()) {
            Ref ref = Ref.of(css.file(), m.group(2));
            if (!ref.isLocal()) {
                continue;
            }
            int line = lineIn(css, code, m.start());
            CheckContext.Lookup found = ctx.lookup(ref, css.file(), line);
            if (found == CheckContext.Lookup.FOUND) {
                ctx.cssAssets.computeIfAbsent(css.file(), k -> new LinkedHashSet<>()).add(ref.path());
            } else if (found == CheckContext.Lookup.MISSING && !isImportAt(code, m.start())) {
                ctx.findings.add(CheckRule.CSS_MISSING_URL, css.file() + " baris " + line + " memanggil " + m.group(2)
                        + ", tetapi file itu tidak ada di ZIP.", css.file(), line,
                        "Sertakan gambar/font tersebut di ZIP atau perbaiki path-nya.", m.group());
            }
        }
    }

    /** {@code @import} CSS lokal yang hilang = Error (sama dengan CSS lokal yang tidak ada); dari luar = aturan library. */
    private void checkImports(CheckContext.CssSource css, String code) {
        Matcher m = IMPORT.matcher(code);
        while (m.find()) {
            String url = m.group(2);
            int line = lineIn(css, code, m.start());
            Ref ref = Ref.of(css.file(), url);
            if (ref.isExternal()) {
                external.checkResource(css.file(), line, url, HtmlRules.snippetAt(code, m.start()));
            } else if (ref.isLocal() && ctx.lookup(ref, css.file(), line) == CheckContext.Lookup.MISSING) {
                ctx.findings.add(CheckRule.MISSING_ASSET, css.file() + " baris " + line + " meng-import " + url
                        + ", tetapi file itu tidak ada di ZIP.", css.file(), line, "Sertakan file CSS tersebut di ZIP.",
                        HtmlRules.snippetAt(code, m.start()));
            } else if (ref.isLocal()) {
                ctx.cssAssets.computeIfAbsent(css.file(), k -> new LinkedHashSet<>()).add(ref.path());
            }
        }
    }

    private static boolean isImportAt(String code, int urlStart) {
        int lineStart = code.lastIndexOf('\n', urlStart) + 1;
        return code.substring(lineStart, urlStart).toLowerCase(Locale.ROOT).contains("@import");
    }

    private void collectVariables(String code) {
        Matcher root = ROOT_BLOCK.matcher(code);
        while (root.find()) {
            Matcher v = VARIABLE.matcher(root.group(1));
            while (v.find()) {
                ctx.cssVariables.putIfAbsent(v.group(1), v.group(2).strip());
            }
        }
    }

    private boolean usesResponsiveFramework() {
        for (String library : ctx.libraries) {
            String lower = library.toLowerCase(Locale.ROOT);
            if (RESPONSIVE_FRAMEWORKS.stream().anyMatch(lower::contains)) {
                return true;
            }
        }
        for (String path : ctx.files.withExtension("css")) {
            String name = Texts.fileName(path).toLowerCase(Locale.ROOT);
            if (RESPONSIVE_FRAMEWORKS.stream().anyMatch(name::contains)) {
                return true;
            }
        }
        return false;
    }

    private static int lineIn(CheckContext.CssSource css, String code, int index) {
        int line = Texts.lineOf(code, index);
        return css.line() != null ? css.line() + line - 1 : line;
    }
}
