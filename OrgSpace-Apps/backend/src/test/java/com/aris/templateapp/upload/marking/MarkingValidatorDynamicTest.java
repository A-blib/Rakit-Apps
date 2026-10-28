package com.aris.templateapp.upload.marking;

import com.aris.templateapp.common.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Isian di elemen yang ditimpa runtime modul dinamis (data-ws-*) ditolak; teks statis di sekitarnya tetap boleh. */
class MarkingValidatorDynamicTest {

    // <html>1 <head>2 <body>3 <span org>4 <section collection>5 <article item>6 <b field>7 <small>8 <a link>9
    // <p empty>10 <strong stat>11 <a auth>12 <form>13 <label>14 <h2>15
    private static final String HTML = """
            <html><head></head><body><span data-ws-org="nama">Nama</span>\
            <section data-ws-collection="program"><article data-ws-item><b data-ws-field="judul">Judul</b>\
            <small>Penanggung jawab:</small><a data-ws-link="tautan" href="#">Buka</a></article>\
            <p data-ws-empty>Belum ada program.</p></section><strong data-ws-stat="pengurus">12</strong>\
            <a data-ws-auth="login" href="#">Masuk</a><form data-ws-form="daftar"><label>Nama</label></form>\
            <h2>Judul statis</h2></body></html>""";
    private static final Map<String, TemplateNumbering.Dynamic> DYNAMIC =
            Map.of("index.html", TemplateNumbering.dynamic(HTML));

    @Test
    void filledByRuntimeIsRejected() {
        assertRejected(field("nama", "text", 4), "data-ws-org=\"nama\"");
        assertRejected(field("judul", "text", 7), "data-ws-field=\"judul\"");
        assertRejected(field("pengurus", "text", 11), "data-ws-stat=\"pengurus\"");
    }

    @Test
    void linkSetByRuntimeCannotBeALinkField() {
        assertRejected(field("tautan", "link", 9), "data-ws-link");
        assertRejected(field("masuk", "button", 12), "data-ws-auth");
        assertAllowed(field("tautan_teks", "text", 9));
        assertAllowed(field("masuk_teks", "text", 12));
    }

    @Test
    void replacingContentAroundAModuleIsRejected() {
        assertRejected(field("program", "paragraph", 5), "membungkus");
        assertRejected(field("kartu", "text", 6), "membungkus");
        assertAllowed(field("kartu_link", "link", 6));
    }

    @Test
    void staticTextNearModulesIsAllowed() {
        assertAllowed(field("pj", "text", 8));
        assertAllowed(field("kosong", "paragraph", 10));
        assertAllowed(field("label", "text", 14));
        assertAllowed(field("judul_statis", "text", 15));
    }

    private static void assertRejected(MarkingData.Field field, String message) {
        assertThatThrownBy(() -> MarkingValidator.validateDynamic(marking(field), DYNAMIC))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(field.label())
                .hasMessageContaining(message);
    }

    private static void assertAllowed(MarkingData.Field field) {
        assertThatCode(() -> MarkingValidator.validateDynamic(marking(field), DYNAMIC)).doesNotThrowAnyException();
    }

    private static MarkingData marking(MarkingData.Field... fields) {
        return new MarkingData(List.of(new MarkingData.Page("index.html", "Beranda")), List.of(), List.of(fields),
                List.of());
    }

    private static MarkingData.Field field(String key, String type, int tplId) {
        return new MarkingData.Field(key, key, type, null, null, false, 1, null, List.of(), null,
                List.of(new MarkingData.Element("index.html", tplId, List.of("mobile", "desktop"))), null, null);
    }
}
