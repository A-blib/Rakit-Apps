package com.aris.templateapp.site;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PortalConfigTest {

    private static final Map<String, Map<String, String>> MODULES = Map.of(
            "agenda", Map.of("nama", "teks", "tanggal", "tanggal", "foto", "gambar"),
            "struktur", Map.of("nama", "teks", "jabatan", "teks"));

    private static PortalConfig.Result read(String json) {
        return PortalConfig.read(json, MODULES, path -> path.equals("img/header.jpg"));
    }

    @Test
    void validConfigIsKeptAsIs() {
        PortalConfig.Result result = read("""
                {"version": 1,
                 "theme": {"primary": "#1e40af", "accent": "#b45309", "radius": 10},
                 "layout": {"sidebar": {"position": "right", "width": 280, "resizable": true},
                            "header": {"variant": "banner", "image": "img/header.jpg", "title": "Portal Tunas Rimba"}},
                 "nav": [{"builtin": "overview"},
                         {"group": "Kegiatan", "items": [{"module": "agenda", "label": "Agenda", "icon": "calendar"}]},
                         {"module": "struktur"}, {"builtin": "submissions", "label": "Pendaftar"}],
                 "modules": {"agenda": {"view": "kartu", "title": "nama", "subtitle": "tanggal", "image": "foto",
                             "fields": {"nama": {"label": "Nama kegiatan", "help": "Maks. 60 karakter"}},
                             "groups": [{"title": "Info utama", "fields": ["nama", "tanggal"]}]}},
                 "mobile": {"nav": "bottom", "bottom": ["overview", "agenda"]}}
                """);

        assertThat(result.errors()).isEmpty();
        assertThat(result.config()).containsKeys("theme", "layout", "nav", "modules", "mobile");
        assertThat(((Map<?, ?>) result.config().get("theme")).get("primary")).isEqualTo("#1E40AF");
        assertThat((List<?>) result.config().get("nav")).hasSize(4);
    }

    @Test
    void mistakesAreReportedAndDroppedFromConfig() {
        PortalConfig.Result result = read("""
                {"theme": {"primary": "#FDE047"},
                 "layout": {"sidebar": {"posisi": "kanan"}, "header": {"image": "img/tidak-ada.jpg"}},
                 "nav": [{"module": "kegiatan"}, {"builtin": "overview"}, {"builtin": "overview"}],
                 "modules": {"agenda": {"image": "nama", "fields": {"lokasi": {"label": "Lokasi"}}}},
                 "mobile": {"bottom": ["overview", "agenda", "struktur", "news", "submissions"]}}
                """);

        assertThat(result.errors()).anyMatch(e -> e.contains("terlalu terang"))
                .anyMatch(e -> e.contains("\"posisi\" tidak dikenal"))
                .anyMatch(e -> e.contains("img/tidak-ada.jpg tidak ada"))
                .anyMatch(e -> e.contains("\"kegiatan\" tidak ada di HTML"))
                .anyMatch(e -> e.contains("\"overview\" sudah ada"))
                .anyMatch(e -> e.contains("bukan gambar"))
                .anyMatch(e -> e.contains("\"lokasi\" tidak ada"))
                .anyMatch(e -> e.contains("maksimal 4"));
        assertThat(result.config()).doesNotContainKey("theme");
        assertThat((List<?>) result.config().get("nav")).hasSize(1);
        assertThat((List<?>) ((Map<?, ?>) result.config().get("mobile")).get("bottom")).hasSize(4);
    }

    @Test
    void brokenJsonGivesEmptyConfig() {
        PortalConfig.Result result = read("{ nav: ");

        assertThat(result.config()).isEmpty();
        assertThat(result.errors()).singleElement().asString().contains("bukan JSON yang valid");
    }

    @Test
    void pathOutsideTemplateIsRejected() {
        PortalConfig.Result result = read("{\"layout\": {\"header\": {\"image\": \"../rahasia.jpg\"}}}");

        assertThat(result.errors()).anyMatch(e -> e.contains("path gambar relatif"));
    }

    @Test
    void contrastFollowsWcag() {
        assertThat(PortalConfig.contrastWithWhite("#000000")).isCloseTo(21.0, org.assertj.core.data.Offset.offset(0.01));
        assertThat(PortalConfig.contrastWithWhite("#FFFFFF")).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.01));
    }
}
