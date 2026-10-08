package com.aris.templateapp.core.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.TemplateManifest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Export ZIP dengan paket contoh (alur-buat-website-via-template.md bagian 8.1). */
public class ProjectExporterTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private TemplateManifest manifest;
    private File projectDir;

    @Before
    public void setUp() throws Exception {
        manifest = TestPackages.manifest("toko-kue");
        projectDir = temp.newFolder("project");
        File images = new File(projectDir, "images");
        assertTrue(images.mkdirs());
        Files.write(new File(images, "foto_hero-1.webp").toPath(), "WEBP".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void valuesAreAppliedAndHtmlIsClean() throws Exception {
        ProjectValues values = new ProjectValues();
        values.field("nama_toko").text = "Dapur <b>Mama</b> Rina";
        values.field("deskripsi").text = "Kue lezat.";
        values.field("foto_hero").image = "images/foto_hero-1.webp";
        values.field("tombol_pesan").text = "Pesan sekarang";
        values.field("tombol_pesan").href = "https://wa.me/6289876543210";
        values.field("link_ig").href = "javascript:alert(1)";
        values.style("nama_toko").put("color", "#1e3a8a");
        values.theme.put("--primary", "#16a34a");

        Map<String, String> zip = export(values);

        String index = zip.get("index.html");
        // Teks selalu teks biasa: tag yang diketik user ditulis sebagai &lt;b&gt;, bukan elemen.
        assertTrue(index, index.contains("Dapur &lt;b&gt;Mama&lt;/b&gt; Rina"));
        assertTrue(index.contains("class=\"brand ws-nama_toko\""));
        assertTrue(index.contains("src=\"img/user/foto_hero-1.webp\""));
        assertFalse(index.contains("srcset"));
        // Ikon di dalam tombol dipertahankan, link diganti.
        assertTrue(index, index.contains("href=\"https://wa.me/6289876543210\"><i class=\"ikon\"></i> Pesan sekarang</a>"));
        // Link tidak valid tidak dipasang: link template tetap.
        assertTrue(index.contains("href=\"https://instagram.com/contoh\""));
        // custom.css dimuat paling akhir di <head>.
        assertTrue(index, index.contains("<link rel=\"stylesheet\" href=\"custom.css\"></head>"));
        for (String attribute : ProjectExporter.APP_ATTRIBUTES) {
            assertFalse(attribute, index.contains(attribute));
        }

        String blog = zip.get("blog/a.html");
        assertTrue(blog.contains("Dapur &lt;b&gt;Mama&lt;/b&gt; Rina"));
        assertTrue(blog.contains("href=\"../custom.css\""));

        assertEquals("WEBP", zip.get("img/user/foto_hero-1.webp"));
        assertTrue(zip.get("custom.css").contains(".ws-nama_toko { color: #1e3a8a !important; }"));
        assertTrue(zip.get("custom.css").contains("--primary: #16a34a;"));
        // manifest & foto contoh yang sudah diganti tidak ikut; file lain tetap.
        assertFalse(zip.containsKey("manifest.json"));
        assertFalse(zip.containsKey("img/hero.jpg"));
        assertTrue(zip.containsKey("img/logo.png"));
        assertTrue(zip.containsKey("css/style.css"));
    }

    @Test
    public void untouchedProjectHasNoCustomCss() throws Exception {
        Map<String, String> zip = export(new ProjectValues());

        assertFalse(zip.containsKey("custom.css"));
        assertFalse(zip.get("index.html").contains("custom.css"));
        assertTrue(zip.containsKey("img/hero.jpg"));
        assertTrue(zip.get("index.html").contains("Toko Kue Bu Ani"));
    }

    @Test
    public void compressorOnlyReplacesWhenSmaller() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProjectExporter.export(TestPackages.dir("toko-kue"), projectDir, manifest, new ProjectValues(),
                (path, content) -> path.endsWith(".jpg") ? "J".getBytes(StandardCharsets.UTF_8) : null, out, null);

        assertEquals("J", unzip(out.toByteArray()).get("img/hero.jpg"));
    }

    private Map<String, String> export(ProjectValues values) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProjectExporter.export(TestPackages.dir("toko-kue"), projectDir, manifest, values, null, out, null);
        return unzip(out.toByteArray());
    }

    private static Map<String, String> unzip(byte[] bytes) throws Exception {
        Map<String, String> files = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                ByteArrayOutputStream content = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int n;
                while ((n = zip.read(buffer)) > 0) {
                    content.write(buffer, 0, n);
                }
                files.put(entry.getName(), content.toString("UTF-8"));
            }
        }
        return files;
    }
}
