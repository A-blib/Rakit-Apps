package com.aris.templateapp.project;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.upload.marking.MarkingData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Isi website dari HP hanya boleh mengubah yang diizinkan kreator (alur-buat-website-via-template.md 12.2). */
class ContentValidatorTest {

    private static final UUID OWN = UUID.randomUUID();
    private static final MarkingData MARKING = new MarkingData(List.of(new MarkingData.Page("index.html", "Beranda")),
            List.of(),
            List.of(field("judul", "text", 10, List.of(new MarkingData.Style("color", null, null, null),
                            new MarkingData.Style("font-size", 24, 40, "px"))),
                    field("foto", "image", null, List.of()),
                    field("wa", "button", null, List.of())),
            List.of(new MarkingData.ThemeVar("--primary", "Warna utama", "color"),
                    new MarkingData.ThemeVar("--radius", "Sudut", "size")));

    @Test
    void validContentPasses() {
        Map<String, Object> content = Map.of(
                "fields", Map.of("judul", Map.of("text", "Halo OSIS", "fromProfile", true),
                        "foto", Map.of("image", "asset:" + OWN),
                        "wa", Map.of("text", "Chat kami", "href", "https://wa.me/62812")),
                "styles", Map.of("judul", Map.of("color", "#1A2150", "font-size", 32)),
                "theme", Map.of("--primary", "#16A34A", "--radius", "12px"));
        assertThatCode(() -> ContentValidator.validate(content, MARKING, OWN::equals)).doesNotThrowAnyException();
    }

    @Test
    void rejectsWhatTheCreatorDidNotAllow() {
        reject(Map.of("fields", Map.of("judul", Map.of("text", "x".repeat(11)))));
        reject(Map.of("fields", Map.of("wa", Map.of("href", "javascript:alert(1)"))));
        reject(Map.of("fields", Map.of("foto", Map.of("image", "local:abc"))));
        reject(Map.of("fields", Map.of("foto", Map.of("image", "asset:" + UUID.randomUUID()))));
        reject(Map.of("styles", Map.of("judul", Map.of("font-size", 60))));
        reject(Map.of("styles", Map.of("judul", Map.of("color", "red; background:url(x)"))));
        reject(Map.of("styles", Map.of("judul", Map.of("border-radius", 4))));
        reject(Map.of("theme", Map.of("--primary", "</style><script>")));
        reject(Map.of("script", Map.of()));
    }

    private static void reject(Map<String, Object> content) {
        assertThatThrownBy(() -> ContentValidator.validate(content, MARKING, OWN::equals))
                .isInstanceOf(ApiException.class);
    }

    private static MarkingData.Field field(String key, String type, Integer max, List<MarkingData.Style> styles) {
        return new MarkingData.Field(key, key, type, null, max, false, 1, null, styles, null,
                List.of(new MarkingData.Element("index.html", 1, List.of("mobile"))), null, null);
    }
}
