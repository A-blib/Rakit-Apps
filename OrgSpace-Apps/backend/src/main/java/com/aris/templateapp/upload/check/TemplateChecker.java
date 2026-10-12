package com.aris.templateapp.upload.check;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.template.IssueSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mesin pengecekan file template (alur-fitur-upload.md bagian 5). Class Java biasa tanpa Spring/database,
 * sehingga bisa diuji cepat dengan ZIP uji (bagian 5.10).
 * <p>
 * Urutan: tahap A (struktur ZIP) → jika ada error fatal, berhenti → tahap B (baca kode). Tahap C (jalankan halaman)
 * dikerjakan WebView di HP dan hanya menambah Peringatan.
 */
public class TemplateChecker {

    /** Dipanggil setiap kali pengecekan pindah tahap, agar app bisa mencentang tahap yang selesai. */
    @FunctionalInterface
    public interface StageListener {
        void onStage(CheckStage stage);
    }

    /** Hasil pengecekan: semua masalah dan info teknis (null jika berhenti karena error fatal). */
    public record Result(List<Finding> findings, TechInfo techInfo) {

        public boolean passed() {
            return findings.stream().noneMatch(f -> f.rule().severity() == IssueSeverity.ERROR);
        }

        public long warningCount() {
            return findings.stream().filter(f -> f.rule().severity() == IssueSeverity.WARNING)
                    .count();
        }
    }

    private final AppProperties.Upload settings;

    public TemplateChecker(AppProperties.Upload settings) {
        this.settings = settings;
    }

    public Result check(byte[] zipBytes, String zipName, StageListener listener) {
        Findings findings = new Findings();

        listener.onStage(CheckStage.OPENING_ZIP);
        TemplateFiles files = new ZipReader(settings.limits(), findings).read(zipBytes, zipName);
        if (files == null) {
            return new Result(findings.toList(), null);
        }

        listener.onStage(CheckStage.STRUCTURE);
        CheckContext ctx = new CheckContext(settings, files, findings);
        ctx.parsePages();
        recognizeKnownLibraries(ctx);
        new StructureRules(ctx).check();

        listener.onStage(CheckStage.HTML_LIBRARY);
        new HtmlRules(ctx).check();
        ExternalRules external = new ExternalRules(ctx);
        external.check();
        new ReferenceRules(ctx).check();
        new CssRules(ctx, external).check();
        new ScriptRules(ctx).check();

        listener.onStage(CheckStage.SIZE);
        new SizeRules(ctx).check();

        return new Result(findings.toList(), techInfo(ctx));
    }

    /** File library terkenal di ZIP dikenali dari hash-nya: tidak dipindai ulang dan masuk info teknis. */
    private void recognizeKnownLibraries(CheckContext ctx) {
        for (String js : ctx.files.withExtension("js", "mjs")) {
            byte[] content = ctx.files.content(js);
            if (content == null) {
                continue;
            }
            String hash = CheckContext.sha256(content);
            for (AppProperties.KnownLibrary library : settings.knownLibraries()) {
                if (library.sha256().equalsIgnoreCase(hash)) {
                    ctx.knownLibraryFiles.add(js);
                    ctx.libraries.add(library.name() + " " + library.version());
                }
            }
        }
    }

    private static TechInfo techInfo(CheckContext ctx) {
        boolean viewport = ctx.pages.values().stream().allMatch(d -> d.selectFirst("meta[name=viewport]") != null);
        List<TechInfo.CssVariable> variables = new ArrayList<>();
        ctx.cssVariables.forEach((name, value) -> variables.add(new TechInfo.CssVariable(name, value)));
        List<String> libraries = new ArrayList<>(ctx.libraries);
        libraries.sort(String.CASE_INSENSITIVE_ORDER);
        return new TechInfo(ctx.files.pages(), libraries.stream().distinct().toList(),
                ctx.files.totalSize(), ctx.responsive && viewport, variables);
    }

    /** "toko-kue.zip" → "Toko Kue": nama awal template (bagian 6.1), bisa diganti provider. */
    public static String nameFromFile(String fileName) {
        String base = fileName.replaceAll("(?i)\\.zip$", "").replaceAll("[-_.]+", " ").strip();
        StringBuilder name = new StringBuilder();
        for (String word : base.split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            name.append(name.isEmpty() ? "" : " ")
                    .append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        String result = name.isEmpty() ? "Template baru" : name.toString();
        return result.length() > 60 ? result.substring(0, 60).strip() : result;
    }
}
