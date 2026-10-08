package com.aris.templateapp.seed;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.upload.UploadStorage;
import com.aris.templateapp.upload.check.TechInfo;
import com.aris.templateapp.upload.check.TemplateChecker;
import com.aris.templateapp.upload.check.TemplateFiles;
import com.aris.templateapp.upload.marking.MarkingData;
import com.aris.templateapp.upload.marking.TemplateNumbering;
import com.aris.templateapp.upload.publish.ManifestBuilder;
import com.aris.templateapp.upload.publish.PackageBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Paket template contoh untuk mencoba editor pembuat website tanpa harus upload dulu
 * (alur-buat-website-via-template.md bagian 12). Sumbernya situs biasa di
 * {@code resources/seed/template-packages/{slug}/site/} ditambah {@code marking.json}.
 * <p>
 * {@code marking.json} menunjuk elemen dengan selector CSS (mudah dibaca dan diubah manusia), lalu di sini diubah
 * menjadi nomor {@code data-tpl-id} seperti hasil mode Tandai di HP. Setelah itu paket dibuat dengan
 * {@link PackageBuilder} dan {@link ManifestBuilder} yang sama dengan Kirim sungguhan.
 */
@Component
public class DemoPackages {

    /** Slug folder paket contoh, dipakai seeder dan test. */
    public static final String KULINER = "umkm-kuliner";
    public static final String SEKOLAH = "profil-sekolah";
    public static final String PORTOFOLIO = "portofolio-minimal";

    private static final String ROOT = "seed/template-packages/";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final AppProperties.Upload settings;
    private final UploadStorage storage;
    private final TemplateChecker checker;

    public DemoPackages(AppProperties properties, UploadStorage storage, TemplateChecker checker) {
        this.settings = properties.upload();
        this.storage = storage;
        this.checker = checker;
    }

    record SeedMarking(String name, String description, List<String> keywords, String thumbnail,
                       List<MarkingData.Page> pages, List<SeedSection> sections, List<MarkingData.ThemeVar> theme,
                       List<SeedField> fields) {
    }

    record SeedSection(String id, String page, String name, String selector) {
    }

    record SeedField(String key, String label, String type, String hint, Integer maxLength, boolean required,
                     String aspectRatio, List<MarkingData.Style> styles, String sectionId, List<SeedElement> elements) {
    }

    record SeedElement(String page, String selector) {
    }

    /** Hasil pembuatan paket contoh. */
    public record Built(String description, List<String> keywords, MarkingData marking, TechInfo techInfo,
                        byte[] sourceZip, byte[] packageZip, byte[] thumbnail) {
    }

    /**
     * Mengisi template (yang sudah tersimpan, jadi sudah punya id) dengan paket contoh: ZIP sumber, tandaan, info,
     * thumbnail, dan package.zip, sehingga template tampil di galeri dan bisa dipakai di editor.
     */
    public void attach(Template template, String slug, Instant now) {
        Built built = build(slug, template);
        storage.writeSource(template.getId(), built.sourceZip());
        storage.writePackage(template.getId(), built.packageZip());
        storage.writeThumbnail(template.getId(), built.thumbnail(), "jpg");
        template.setDescription(built.description());
        template.setKeywords(new ArrayList<>(built.keywords()));
        template.setMarking(built.marking());
        template.setMarkingFieldCount(built.marking().fields().size());
        template.setTechInfo(built.techInfo());
        template.setSourceFileName(slug + ".zip");
        template.setSourceSize((long) built.sourceZip().length);
        template.setWizardStep(6);
        template.setThumbnailSource("custom");
        template.setThumbnailUrl("/api/templates/" + template.getId() + "/thumbnail?v=" + now.toEpochMilli());
        template.setPackageSize((long) built.packageZip().length);
    }

    public Built build(String slug, Template template) {
        try {
            SeedMarking seed = readMarking(slug);
            Map<String, byte[]> files = readSite(slug);
            byte[] sourceZip = zip(files);

            Map<String, Document> numbered = new HashMap<>();
            for (MarkingData.Page page : seed.pages()) {
                numbered.put(page.file(), TemplateNumbering.number(new String(files.get(page.file()), StandardCharsets.UTF_8)));
            }
            List<MarkingData.Section> sections = new ArrayList<>();
            for (SeedSection s : seed.sections()) {
                sections.add(new MarkingData.Section(s.id(), s.page(), s.name(), tplId(numbered, s.page(), s.selector())));
            }
            List<MarkingData.Field> fields = new ArrayList<>();
            for (SeedField f : seed.fields()) {
                List<MarkingData.Element> elements = new ArrayList<>();
                for (SeedElement e : f.elements()) {
                    elements.add(new MarkingData.Element(e.page(), tplId(numbered, e.page(), e.selector()),
                            List.of("mobile", "desktop")));
                }
                fields.add(new MarkingData.Field(f.key(), f.label(), f.type(), f.hint(), f.maxLength(), f.required(), 0,
                        f.aspectRatio(), f.styles(), f.sectionId(), elements));
            }
            MarkingData marking = new MarkingData(seed.pages(), sections, fields, seed.theme()).inPageOrder();

            TechInfo techInfo = checker.check(sourceZip, slug + ".zip", stage -> { }).techInfo();
            TemplateFiles templateFiles = TemplateFiles.readZip(sourceZip, settings.limits());
            // Situs contoh tidak memakai CDN, jadi pengunduh library tidak pernah dipanggil.
            byte[] packageZip = new PackageBuilder(settings, (url, max) -> {
                throw new IOException("Paket contoh tidak boleh memakai CDN: " + url);
            }).build(templateFiles, marking);
            packageZip = ManifestBuilder.addTo(packageZip,
                    new ManifestBuilder.Info(template.getId(), template.getPackageVersion(), techInfo), marking);
            return new Built(seed.description(), seed.keywords(), marking, techInfo, sourceZip, packageZip,
                    files.get(seed.thumbnail()));
        } catch (IOException | PackageBuilder.LibraryCopyException e) {
            throw new IllegalStateException("Paket contoh " + slug + " gagal dibuat", e);
        }
    }

    private static int tplId(Map<String, Document> numbered, String page, String selector) {
        Document doc = numbered.get(page);
        Element element = doc == null ? null : doc.selectFirst(selector);
        if (element == null) {
            throw new IllegalStateException("Selector '" + selector + "' tidak ditemukan di " + page);
        }
        return Integer.parseInt(element.attr(TemplateNumbering.ATTRIBUTE));
    }

    private static SeedMarking readMarking(String slug) throws IOException {
        Resource resource = new PathMatchingResourcePatternResolver().getResource("classpath:" + ROOT + slug + "/marking.json");
        try (InputStream in = resource.getInputStream()) {
            return JSON.readValue(in, SeedMarking.class);
        }
    }

    /** Semua file situs contoh, path relatif dari folder site/ (mis. "css/style.css"). */
    private static Map<String, byte[]> readSite(String slug) throws IOException {
        String marker = ROOT + slug + "/site/";
        Map<String, byte[]> files = new TreeMap<>();
        for (Resource resource : new PathMatchingResourcePatternResolver().getResources("classpath*:" + marker + "**")) {
            String url = resource.getURL().toString();
            if (url.endsWith("/") || !resource.isReadable()) {
                continue; // folder
            }
            String path = url.substring(url.indexOf(marker) + marker.length());
            try (InputStream in = resource.getInputStream()) {
                files.put(path, in.readAllBytes());
            }
        }
        if (!files.containsKey("index.html")) {
            throw new IOException("Situs contoh " + slug + " tidak punya index.html");
        }
        return files;
    }

    private static byte[] zip(Map<String, byte[]> files) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue());
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }
}
