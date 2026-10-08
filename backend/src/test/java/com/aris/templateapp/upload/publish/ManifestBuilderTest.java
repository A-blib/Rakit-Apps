package com.aris.templateapp.upload.publish;

import com.aris.templateapp.upload.check.TechInfo;
import com.aris.templateapp.upload.marking.MarkingData;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** manifest.json dari paket ber-data-key (alur-buat-website-via-template.md bagian 11.3). */
class ManifestBuilderTest {

    private static final String INDEX = """
            <html><body><h1 data-key="judul">  Toko   Kue </h1>
            <div data-key="latar" style="background-image: url('img/bg.jpg')">x</div>
            <a data-key="pesan" href="https://wa.me/62812">Pesan</a></body></html>""";
    private static final String BLOG = """
            <html><body><img data-key="foto" src="../img/foto.jpg?v=2"><h1 data-key="judul">Toko Kue</h1></body></html>""";

    @Test
    void samplesComeFromFirstElementAndImagePathsAreFromPackageRoot() {
        MarkingData marking = new MarkingData(
                List.of(new MarkingData.Page("index.html", "Beranda"), new MarkingData.Page("blog/a.html", "Blog")),
                List.of(new MarkingData.Section("s1", "index.html", "Hero", 1)),
                List.of(field("foto", "image", "blog/a.html", 2),
                        field("judul", "text", "index.html", 3, "blog/a.html"),
                        field("latar", "image", "index.html", 4),
                        field("pesan", "button", "index.html", 5)),
                List.of(new MarkingData.ThemeVar("--primary", "Warna utama", "color")));
        TechInfo tech = new TechInfo(List.of(), List.of(), 0, true, List.of(new TechInfo.CssVariable("--primary", "#2563eb")));
        Map<String, String> pages = Map.of("index.html", INDEX, "blog/a.html", BLOG);

        TemplateManifest manifest = ManifestBuilder.build(new ManifestBuilder.Info(UUID.randomUUID(), 1, tech), marking,
                pages::get);

        // Urutan: halaman pertama dulu (index.html), lalu blog.
        assertThat(manifest.fields()).extracting(TemplateManifest.Field::key).containsExactly("judul", "latar", "pesan", "foto");
        TemplateManifest.Field judul = manifest.fields().get(0);
        assertThat(judul.sample()).isEqualTo("Toko Kue");
        assertThat(judul.pages()).containsExactly("index.html", "blog/a.html");
        assertThat(manifest.fields().get(1).sample()).isEqualTo("img/bg.jpg");
        assertThat(manifest.fields().get(2).sample()).isEqualTo("Pesan");
        assertThat(manifest.fields().get(2).sampleHref()).isEqualTo("https://wa.me/62812");
        assertThat(manifest.fields().get(3).sample()).isEqualTo("img/foto.jpg");
        assertThat(manifest.theme().get(0).defaultValue()).isEqualTo("#2563eb");
    }

    @Test
    void externalImageStaysAsIs() {
        var element = Jsoup.parse("<img src=\"https://cdn.example.com/a.jpg\">").selectFirst("img");
        assertThat(ManifestBuilder.imageSample("index.html", element)).isEqualTo("https://cdn.example.com/a.jpg");
    }

    private static MarkingData.Field field(String key, String type, String page, int tplId, String... morePages) {
        List<MarkingData.Element> elements = new java.util.ArrayList<>();
        elements.add(new MarkingData.Element(page, tplId, List.of("mobile", "desktop")));
        for (String p : morePages) {
            elements.add(new MarkingData.Element(p, tplId + 10, List.of("mobile", "desktop")));
        }
        return new MarkingData.Field(key, key, type, null, null, true, 0, null, null, "s1", elements);
    }
}
