package com.aris.templateapp.ui.website;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsiteStatus;

import org.junit.Test;

/**
 * Pengujian behavior untuk logika tampilan dan aksi buka website di {@link WebsiteUi}.
 */
public class WebsiteUiTest {

    @Test
    public void shortAddress_removesScheme() {
        assertNull(WebsiteUi.shortAddress(null));
        assertEquals("localhost:8080/sites/web-dinamis/",
                WebsiteUi.shortAddress("http://localhost:8080/sites/web-dinamis/"));
        assertEquals("osis-smansa.orgspace.site",
                WebsiteUi.shortAddress("https://osis-smansa.orgspace.site"));
        assertEquals("smansa.sch.id",
                WebsiteUi.shortAddress("smansa.sch.id"));
    }

    @Test
    public void normalizeUrl_handlesSchemesAndWhitespace() {
        assertNull(WebsiteUi.normalizeUrl(null));
        assertNull(WebsiteUi.normalizeUrl(""));
        assertNull(WebsiteUi.normalizeUrl("   "));

        // Menambahkan http:// jika skema belum ada
        assertEquals("http://localhost:8080/sites/web-dinamis/",
                WebsiteUi.normalizeUrl("localhost:8080/sites/web-dinamis/"));
        assertEquals("http://osis-smansa.orgspace.site",
                WebsiteUi.normalizeUrl("osis-smansa.orgspace.site"));

        // Mempertahankan skema yang sudah ada
        assertEquals("http://localhost:8080/sites/web-dinamis/",
                WebsiteUi.normalizeUrl("http://localhost:8080/sites/web-dinamis/"));
        assertEquals("https://osis-smansa.orgspace.site",
                WebsiteUi.normalizeUrl("https://osis-smansa.orgspace.site"));

        // Membersihkan spasi di awal/akhir
        assertEquals("http://localhost:8080/sites/test/",
                WebsiteUi.normalizeUrl("   http://localhost:8080/sites/test/  "));
    }

    @Test
    public void canOpenWebsite_checksSiteUrlValidity() {
        assertFalse(WebsiteUi.canOpenWebsite(null));

        Website draft = createWebsite(null);
        assertFalse(WebsiteUi.canOpenWebsite(draft));

        Website emptyUrl = createWebsite("   ");
        assertFalse(WebsiteUi.canOpenWebsite(emptyUrl));

        Website online = createWebsite("http://localhost:8080/sites/web-dinamis-organisasi/");
        assertTrue(WebsiteUi.canOpenWebsite(online));

        Website domainOnly = createWebsite("osis.orgspace.site");
        assertTrue(WebsiteUi.canOpenWebsite(domainOnly));
    }

    @Test
    public void openWebsite_handlesNullGracefully() {
        // Tidak melempar exception saat dipanggil dengan null atau string kosong
        WebsiteUi.openWebsite(null, null);
        WebsiteUi.openWebsite(null, "");
        WebsiteUi.openWebsite(null, "   ");
    }

    private static Website createWebsite(String siteUrl) {
        return new Website(
                "web-1",
                "org-1",
                "Web Organisasi",
                ProjectMode.TEMPLATE,
                siteUrl == null ? WebsiteStatus.DRAFT : WebsiteStatus.ONLINE,
                false,
                siteUrl,
                0,
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "Admin",
                null
        );
    }
}
