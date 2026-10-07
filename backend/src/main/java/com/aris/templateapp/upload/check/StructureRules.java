package com.aris.templateapp.upload.check;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Tahap A sisanya dan bagian B: struktur project, kode sumber yang belum di-build, dan jenis file. */
final class StructureRules {

    // Bagian 5.6 B. "json" ditambahkan untuk data statis yang dibaca lokal (mis. fetch('data/menu.json')).
    private static final Set<String> ALLOWED = Set.of("html", "htm", "css", "js", "mjs", "png", "jpg", "jpeg", "webp",
            "gif", "svg", "ico", "woff", "woff2", "ttf", "otf", "txt", "md", "json");
    private static final Set<String> SERVER_SCRIPTS = Set.of("php", "phtml", "php3", "php4", "php5", "phps");
    private static final Set<String> UNBUILT_STYLES = Set.of("scss", "sass", "less");
    private static final Set<String> SOURCE_CODE = Set.of("ts", "tsx", "jsx", "vue", "svelte");
    // File konfigurasi/build yang boleh ikut terbawa di samping hasil build: diabaikan, bukan masalah.
    private static final Set<String> CONFIG_FILES = Set.of("package.json", "package-lock.json", "yarn.lock",
            "pnpm-lock.yaml", "bun.lockb", "jsconfig.json", "composer.json", "netlify.toml", "vercel.json");
    private static final Pattern CONFIG_SCRIPT = Pattern.compile(
            "(^|/)[\\w.-]+\\.config\\.(js|ts|mjs|cjs)$|(^|/)tsconfig[\\w.-]*\\.json$");
    private static final Pattern DOC_FILE = Pattern.compile("(^|/)(readme|license|licence|changelog|authors)$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9._@/-]+");
    // import "bare" (mis. from 'vue') hanya bisa dibaca alat build, bukan browser.
    private static final Pattern BARE_IMPORT = Pattern.compile(
            "(?m)^\\s*import\\s+(?:[\\w*{}\\s,$]+\\s+from\\s+)?['\"]([^'\"./][^'\"]*)['\"]");

    private final CheckContext ctx;

    StructureRules(CheckContext ctx) {
        this.ctx = ctx;
    }

    void check() {
        boolean sourceNotBuilt = checkSourceNotBuilt();
        ignoreConfigFiles();
        checkFileTypes(sourceNotBuilt);
        checkUnbuiltIncludes();
        checkPageCount();
        checkFileNames();
    }

    /**
     * ZIP berisi kode sumber tanpa hasil build: node_modules ikut, ada file .ts/.vue/..., index.html memanggil
     * file sumber, atau JS memakai import "bare" yang hanya dipahami alat build.
     */
    private boolean checkSourceNotBuilt() {
        TemplateFiles files = ctx.files;
        boolean markers = files.hasNodeModules() || files.exists("package.json")
                || files.paths().stream().anyMatch(p -> p.startsWith("src/") || CONFIG_SCRIPT.matcher(p).find());
        boolean sourceFiles = files.paths().stream().anyMatch(p -> SOURCE_CODE.contains(Texts.extension(p)));
        boolean indexUsesSource = false;
        Document index = ctx.pages.get("index.html");
        if (index != null) {
            for (Element script : index.select("script[src]")) {
                String src = script.attr("src").toLowerCase(Locale.ROOT);
                if (SOURCE_CODE.contains(Texts.extension(src.split("[?#]")[0]))
                        || src.startsWith("/src/") || src.startsWith("src/") || src.startsWith("./src/")) {
                    indexUsesSource = true;
                }
            }
        }
        String bareImportFile = null;
        boolean hasImportMap = ctx.pages.values().stream().anyMatch(d -> !d.select("script[type=importmap]").isEmpty());
        if (!hasImportMap) {
            for (String js : files.withExtension("js", "mjs")) {
                String code = ctx.text(js);
                if (code != null && BARE_IMPORT.matcher(code).find()) {
                    bareImportFile = js;
                    break;
                }
            }
        }

        boolean unbuilt = files.hasNodeModules() || (markers && (sourceFiles || indexUsesSource)) || bareImportFile != null;
        if (unbuilt) {
            String detail = files.hasNodeModules() ? "ZIP membawa folder node_modules."
                    : bareImportFile != null ? bareImportFile + " memakai import yang hanya bisa dibaca alat build."
                    : "ZIP berisi file sumber (src/, package.json, .ts/.jsx/.vue) tanpa HTML hasil build.";
            ctx.findings.add(CheckRule.SOURCE_NOT_BUILT, "Sepertinya ini kode sumber, bukan hasil build. " + detail,
                    bareImportFile, null,
                    "Jalankan npm run build di laptopmu, lalu upload isi folder dist/ sebagai ZIP.");
        }
        return unbuilt;
    }

    private void ignoreConfigFiles() {
        for (String path : List.copyOf(ctx.files.paths())) {
            String name = Texts.fileName(path).toLowerCase(Locale.ROOT);
            boolean dotFile = path.startsWith(".") || path.contains("/.");
            if (dotFile || CONFIG_FILES.contains(name) || CONFIG_SCRIPT.matcher(path).find()
                    || Texts.extension(path).equals("map") || DOC_FILE.matcher(path).find()) {
                ctx.files.ignore(path);
            }
        }
    }

    private void checkFileTypes(boolean sourceNotBuilt) {
        for (String path : ctx.files.paths()) {
            String ext = Texts.extension(path);
            if (ALLOWED.contains(ext)) {
                continue;
            }
            if (SERVER_SCRIPTS.contains(ext)) {
                ctx.findings.add(CheckRule.SERVER_SCRIPT, "Template ini butuh server (PHP): " + path + ".", path, null,
                        "App hanya mendukung website statis HTML/CSS/JS. Ubah halaman PHP menjadi file .html.");
            } else if (UNBUILT_STYLES.contains(ext)) {
                ctx.findings.add(CheckRule.UNBUILT_RESOURCE, path + " adalah file ." + ext + " mentah yang belum di-build.",
                        path, null, "Build dulu di laptopmu (mis. npm run build) lalu upload isi folder dist/.");
            } else if (SOURCE_CODE.contains(ext) && sourceNotBuilt) {
                // Sudah dijelaskan oleh SOURCE_NOT_BUILT; tidak perlu satu error per file sumber.
                continue;
            } else {
                ctx.findings.add(CheckRule.FILE_TYPE_NOT_ALLOWED, "Jenis file ." + (ext.isEmpty() ? "(tanpa ekstensi)" : ext)
                                + " tidak diizinkan: " + path + ".", path, null,
                        "Hapus file ini. Yang boleh: HTML, CSS, JS, gambar (png, jpg, webp, gif, svg, ico), font, txt, md.");
            }
        }
    }

    /** Tag {@code <include>} atau {@code @@include(...)}: "resep" HTML yang belum di-build (bagian 5.6 A). */
    private void checkUnbuiltIncludes() {
        Pattern atInclude = Pattern.compile("@@include\\s*\\(");
        for (Map.Entry<String, Document> page : ctx.pages.entrySet()) {
            for (Element include : page.getValue().select("include")) {
                ctx.findings.add(CheckRule.UNBUILT_RESOURCE, page.getKey() + " memakai tag <include> yang belum di-build.",
                        page.getKey(), CheckContext.line(include),
                        "Gabungkan potongan HTML dengan alat build (Vite + plugin include, Eleventy) lalu upload isi dist/.",
                        include.outerHtml());
            }
            String html = ctx.text(page.getKey());
            Matcher m = atInclude.matcher(html);
            if (m.find()) {
                ctx.findings.add(CheckRule.UNBUILT_RESOURCE, page.getKey() + " memakai @@include yang belum di-build.",
                        page.getKey(), Texts.lineOf(html, m.start()),
                        "Build dulu di laptopmu lalu upload hasil build-nya.", html.substring(m.start(),
                                Math.min(html.length(), m.start() + 60)));
            }
        }
    }

    private void checkPageCount() {
        int pages = ctx.files.pages().size();
        int max = ctx.limits().maxPages();
        if (pages > max) {
            ctx.findings.add(CheckRule.TOO_MANY_PAGES, "Template berisi " + pages + " halaman HTML, maksimal " + max + ".",
                    null, null, "Gabungkan atau hapus halaman yang tidak perlu.");
        }
    }

    private void checkFileNames() {
        List<String> odd = new ArrayList<>();
        for (String path : ctx.files.paths()) {
            if (!SAFE_NAME.matcher(path).matches()) {
                odd.add(path);
            }
        }
        if (!odd.isEmpty()) {
            ctx.findings.add(CheckRule.FILE_NAME_STYLE, odd.size() + " nama file memakai spasi atau karakter khusus, mis. "
                            + String.join(", ", odd.subList(0, Math.min(3, odd.size()))) + ".", odd.get(0), null,
                    "Pakai huruf kecil, tanpa spasi, dan tanda hubung. Contoh: foto-produk-1.jpg");
        }
    }
}
