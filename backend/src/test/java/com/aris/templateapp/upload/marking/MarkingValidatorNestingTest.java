package com.aris.templateapp.upload.marking;

import com.aris.templateapp.common.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Isian Teks/Paragraf/Tombol tidak boleh membungkus isian lain, karena isinya akan tertimpa. */
class MarkingValidatorNestingTest {

    // <html>1 <head>2 <body>3 <article>4 <h3>5 <p>6 <a>7 <img>8
    private static final String HTML = """
            <html><head></head><body><article><h3>Judul</h3><p>Isi</p></article>\
            <a href="x.html"><img src="a.png" alt="a"></a></body></html>""";
    private static final Map<String, Map<Integer, Integer>> PARENTS =
            Map.of("index.html", TemplateNumbering.parents(HTML));

    @Test
    void paragraphWrappingAnotherFieldIsRejected() {
        MarkingData data = marking(field("card", "paragraph", 4), field("judul", "text", 5));
        assertThatThrownBy(() -> MarkingValidator.validateNesting(data, PARENTS))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("card")
                .hasMessageContaining("judul");
    }

    @Test
    void linkWrappingImageIsAllowed() {
        MarkingData data = marking(field("tautan", "link", 7), field("foto", "image", 8));
        assertThatCode(() -> MarkingValidator.validateNesting(data, PARENTS)).doesNotThrowAnyException();
    }

    @Test
    void siblingsAreAllowed() {
        MarkingData data = marking(field("judul", "text", 5), field("isi", "paragraph", 6));
        assertThatCode(() -> MarkingValidator.validateNesting(data, PARENTS)).doesNotThrowAnyException();
    }

    private static MarkingData marking(MarkingData.Field... fields) {
        return new MarkingData(List.of(new MarkingData.Page("index.html", "Beranda")), List.of(), List.of(fields),
                List.of());
    }

    private static MarkingData.Field field(String key, String type, int tplId) {
        return new MarkingData.Field(key, key, type, null, null, false, 1, null, List.of(), null,
                List.of(new MarkingData.Element("index.html", tplId, List.of("mobile", "desktop"))));
    }
}
