package com.aris.templateapp.upload.publish;

import com.aris.templateapp.upload.check.TechInfo;
import com.aris.templateapp.upload.marking.MarkingData;
import com.aris.templateapp.upload.marking.TemplateNumbering;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Menambahkan {@code manifest.json} ke paket template (alur-buat-website-via-template.md bagian 11.3).
 * <p>
 * Manifest dibuat dari paket yang sudah jadi (HTML ber-{@code data-key}), bukan dari HTML asli. Dengan begitu satu
 * cara yang sama dipakai saat Kirim dan saat melengkapi paket lama yang dibuat sebelum manifest ada
 * ({@code PackageManifestBackfill}).
 */
public final class ManifestBuilder {

    public static final String FILE_NAME = "manifest.json";

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Pattern BACKGROUND_URL = Pattern.compile("background(?:-image)?\\s*:[^;]*url\\(\\s*(['\"]?)([^'\")]+)\\1\\s*\\)");

    /** Data template yang tidak ada di tandaan. */
    public record Info(UUID templateId, int version, TechInfo techInfo) {
    }

    private ManifestBuilder() {
    }

    /** @return ZIP paket baru dengan manifest.json di folder paling luar (manifest lama, jika ada, diganti) */
    public static byte[] addTo(byte[] packageZip, Info info, MarkingData marking) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(packageZip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    entries.put(entry.getName(), in.readAllBytes());
                }
            }
        }
        TemplateManifest manifest = build(info, marking, page -> {
            byte[] html = entries.get(page);
            return html == null ? null : new String(html, StandardCharsets.UTF_8);
        });
        entries.put(FILE_NAME, JSON.writeValueAsBytes(manifest));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, byte[]> e : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(e.getKey()));
                zip.write(e.getValue());
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    /** Membaca manifest dari paket; null jika paket belum punya manifest. */
    public static TemplateManifest read(byte[] packageZip) throws IOException {
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(packageZip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (entry.getName().equals(FILE_NAME)) {
                    return JSON.readValue(in.readAllBytes(), TemplateManifest.class);
                }
            }
        }
        return null;
    }

    interface PageSource {
        /** @return isi HTML halaman paket, atau null jika tidak ada */
        String html(String page);
    }

    static TemplateManifest build(Info info, MarkingData marking, PageSource source) {
        MarkingData ordered = marking.inPageOrder();
        Map<String, Document> docs = new HashMap<>();
        List<TemplateManifest.Page> pages = new ArrayList<>();
        for (MarkingData.Page page : list(ordered.pages())) {
            pages.add(new TemplateManifest.Page(page.file(), page.name()));
        }
        List<TemplateManifest.Section> sections = new ArrayList<>();
        for (MarkingData.Section section : list(ordered.sections())) {
            sections.add(new TemplateManifest.Section(section.id(), section.page(), section.name()));
        }
        List<TemplateManifest.ThemeVar> theme = new ArrayList<>();
        for (MarkingData.ThemeVar var : list(ordered.theme())) {
            theme.add(new TemplateManifest.ThemeVar(var.var(), var.label(), var.type(), themeDefault(info.techInfo(), var.var())));
        }
        List<TemplateManifest.Field> fields = new ArrayList<>();
        for (MarkingData.Field field : list(ordered.fields())) {
            Set<String> fieldPages = new LinkedHashSet<>();
            list(field.elements()).forEach(e -> fieldPages.add(e.page()));
            Found found = firstElement(field, fieldPages, docs, source);
            Element first = found == null ? null : found.element();
            String page = found == null ? null : found.page();
            String sample = null;
            String sampleHref = null;
            if (first != null) {
                switch (field.type()) {
                    case "image" -> sample = imageSample(page, first);
                    case "link" -> sample = emptyToNull(first.attr("href"));
                    case "button" -> {
                        sample = emptyToNull(first.text());
                        sampleHref = emptyToNull(first.attr("href"));
                    }
                    default -> sample = emptyToNull(first.text());
                }
            }
            List<TemplateManifest.Style> styles = new ArrayList<>();
            for (MarkingData.Style style : list(field.styles())) {
                styles.add(new TemplateManifest.Style(style.prop(), style.min(), style.max(), style.unit()));
            }
            fields.add(new TemplateManifest.Field(field.key(), field.label(), field.type(), field.hint(), field.maxLength(),
                    field.required(), field.order(), field.sectionId(), field.aspectRatio(), new ArrayList<>(fieldPages),
                    sample, sampleHref, styles.isEmpty() ? null : styles));
        }
        return new TemplateManifest(info.templateId(), info.version(), pages, sections, theme, fields);
    }

    private record Found(String page, Element element) {
    }

    /** Elemen pertama isian di halaman pertamanya (sama dengan elemen yang dipakai Coba untuk nilai asli). */
    private static Found firstElement(MarkingData.Field field, Set<String> pages, Map<String, Document> docs,
                                        PageSource source) {
        for (String page : pages) {
            Document doc = docs.computeIfAbsent(page, p -> {
                String html = source.html(p);
                return html == null ? null : TemplateNumbering.parse(html);
            });
            if (doc == null) {
                continue;
            }
            for (Element element : doc.select("[data-key]")) {
                if (element.attr("data-key").equals(field.key())) {
                    return new Found(page, element);
                }
            }
        }
        return null;
    }

    /** Path gambar contoh dari folder utama paket, mis. halaman "blog/a.html" + "../img/x.jpg" → "img/x.jpg". */
    static String imageSample(String page, Element element) {
        String ref = element.tagName().equals("img") ? element.attr("src") : null;
        if (ref == null || ref.isBlank()) {
            Matcher m = BACKGROUND_URL.matcher(element.attr("style"));
            ref = m.find() ? m.group(2) : null;
        }
        if (ref == null || ref.isBlank()) {
            return null;
        }
        ref = ref.strip();
        if (ref.contains("://") || ref.startsWith("//") || ref.startsWith("data:")) {
            return ref;
        }
        try {
            String path = URI.create("https://paket.local/" + page).resolve(ref.split("[?#]")[0]).getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String themeDefault(TechInfo techInfo, String var) {
        if (techInfo == null || techInfo.cssVariables() == null) {
            return null;
        }
        return techInfo.cssVariables().stream().filter(v -> v.name().equals(var)).map(TechInfo.CssVariable::value)
                .findFirst().orElse(null);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static <T> List<T> list(List<T> list) {
        return list == null ? List.of() : list;
    }
}
