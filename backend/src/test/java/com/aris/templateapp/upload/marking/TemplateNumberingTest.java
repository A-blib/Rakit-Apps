package com.aris.templateapp.upload.marking;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Nomor data-tpl-id harus sama setiap kali HTML yang sama diberi nomor (alur-fitur-upload.md bagian 7.8). */
class TemplateNumberingTest {

    private static final String HTML = """
            <!DOCTYPE html><html lang="id"><head><title>Toko</title></head>
            <body><header><h1>Toko Kue</h1></header>
            <p>Kue <b>enak</b> setiap hari.</p><table><tr><td>Brownies</td></tr></table></body></html>""";

    @Test
    void everyElementGetsStableNumber() {
        Document first = TemplateNumbering.number(HTML);
        Document second = TemplateNumbering.number(HTML);
        assertThat(first.outerHtml()).isEqualTo(second.outerHtml());
        assertThat(first.selectFirst("h1").attr(TemplateNumbering.ATTRIBUTE)).isEqualTo("6"); // html, head, title, body, header, h1
        assertThat(first.select("[data-tpl-id]")).hasSize(first.getAllElements().size() - 1);
        // jsoup menambahkan <tbody> saat parse; nomornya tetap konsisten karena server selalu memakai parse yang sama.
        assertThat(first.selectFirst("tbody").hasAttr(TemplateNumbering.ATTRIBUTE)).isTrue();
    }

    @Test
    void parentsCoverAllNumberedElements() {
        int count = TemplateNumbering.number(HTML).getAllElements().size() - 1;
        var parents = TemplateNumbering.parents(HTML);
        assertThat(parents.keySet()).hasSize(count).contains(1, count).doesNotContain(0, count + 1);
        assertThat(parents.get(1)).isNull(); // <html> paling luar
        assertThat(parents.get(6)).isEqualTo(5); // <h1> di dalam <header>
    }

    @Test
    void originalWhitespaceIsKept() {
        assertThat(TemplateNumbering.number("<p>a  <b>b</b>\n c</p>").body().html())
                .isEqualTo("<p data-tpl-id=\"4\">a  <b data-tpl-id=\"5\">b</b>\n c</p>");
    }
}
