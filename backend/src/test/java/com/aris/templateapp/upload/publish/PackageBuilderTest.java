package com.aris.templateapp.upload.publish;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.upload.check.CheckRuleFixturesTestSupport;
import com.aris.templateapp.upload.marking.MarkingData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Paket template: salin library CDN + sisipkan atribut penandaan (alur-fitur-upload.md bagian 9 & 10.5). */
class PackageBuilderTest {

    private static final AppProperties.Upload SETTINGS = CheckRuleFixturesTestSupport.settings();

    private final List<String> fetched = new ArrayList<>();
    private final LibraryFetcher fake = (url, max) -> {
        fetched.add(url);
        if (url.endsWith("all.min.css")) {
            return "@font-face{src:url(../webfonts/fa-solid-900.woff2)}".getBytes(StandardCharsets.UTF_8);
        }
        return ("/* " + url + " */").getBytes(StandardCharsets.UTF_8);
    };

    private static final String HTML = """
            <!DOCTYPE html><html lang="id"><head>
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" crossorigin="anonymous">
            <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter">
            </head><body><section><h1>Toko Kue</h1><p>Kue enak</p></section>
            <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script></body></html>""";

    @Test
    void cdnLibrariesAreCopiedWithTheirFontsAndLinksBecomeLocal() throws Exception {
        PackageBuilder builder = new PackageBuilder(SETTINGS, fake);
        String page = builder.buildPage("blog/index.html", HTML, marking("blog/index.html"));

        assertThat(page).contains("href=\"../vendor/font-awesome@6.5.2/css/all.min.css\"")
                .contains("src=\"../vendor/jquery@3.7.1/jquery-3.7.1.min.js\"")
                .contains("https://fonts.googleapis.com/css2?family=Inter")
                .doesNotContain("crossorigin");
        assertThat(fetched).containsExactlyInAnyOrder(
                "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css",
                "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/webfonts/fa-solid-900.woff2",
                "https://code.jquery.com/jquery-3.7.1.min.js");
    }

    @Test
    void markingAttributesAreInsertedAndNumbersRemoved() throws Exception {
        String page = new PackageBuilder(SETTINGS, fake).buildPage("index.html", HTML, marking("index.html"));
        // Nomor: html 1, head 2, link 3, link 4, body 5, section 6, h1 7, p 8.
        assertThat(page).contains("<section data-section=\"Hero\">")
                .contains("<h1 data-edit=\"text\" data-key=\"judul\" data-label=\"Judul utama\">Toko Kue</h1>")
                .doesNotContain("data-tpl-id");
    }

    @Test
    void failedDownloadStopsPublishing() {
        LibraryFetcher broken = (url, max) -> {
            throw new IOException("timeout");
        };
        assertThatThrownBy(() -> new PackageBuilder(SETTINGS, broken).buildPage("index.html", HTML, marking("index.html")))
                .isInstanceOf(PackageBuilder.LibraryCopyException.class).hasMessageContaining("gagal disalin");
    }

    @Test
    void localPathFollowsCdnFolders() {
        assertThat(PackageBuilder.localPath("cdn.jsdelivr.net", "/npm/@fortawesome/fontawesome-free@6.5.2/css/all.css"))
                .isEqualTo("vendor/fortawesome-fontawesome-free@6.5.2/css/all.css");
        assertThat(PackageBuilder.localPath("unpkg.com", "/alpinejs@3.14.1/dist/cdn.min.js"))
                .isEqualTo("vendor/alpinejs@3.14.1/dist/cdn.min.js");
        assertThat(PackageBuilder.localPath("cdn.tailwindcss.com", "")).isEqualTo("vendor/tailwindcss-play/tailwind.js");
        assertThat(PackageBuilder.localPath("example.com", "/x.js")).isNull();
        assertThat(PackageBuilder.relativeFrom("a/b/c.html", "vendor/x.js")).isEqualTo("../../vendor/x.js");
    }

    private static MarkingData marking(String page) {
        return new MarkingData(List.of(new MarkingData.Page(page, "Beranda")),
                List.of(new MarkingData.Section("s1", page, "Hero", 6)),
                List.of(new MarkingData.Field("judul", "Judul utama", "text", null, 30, true, 1, null, List.of(), "s1",
                        List.of(new MarkingData.Element(page, 7, List.of("mobile", "desktop"))))),
                List.of());
    }
}
