package com.aris.templateapp.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** Filter tab Website di HP (dipakai saat offline) harus sama dengan aturan server. */
public class WebsiteQueryTest {

    private final List<Website> websites = List.of(
            website("1", "Pensi 2026", WebsiteStatus.DRAFT, false, 300, 100),
            website("2", "Website OSIS 2026", WebsiteStatus.ONLINE, true, 200, 200),
            website("3", "Profil OSIS", WebsiteStatus.ONLINE, false, 100, 300),
            website("4", "Arsip OSIS 2025", WebsiteStatus.OFFLINE, true, 50, 400));

    @Test
    public void filtersMatchServerRules() {
        assertEquals(List.of("1"), ids(WebsiteQuery.initial().withFilter(WebsiteQuery.Filter.DRAFT).apply(websites)));
        assertEquals(List.of("2", "3"),
                ids(WebsiteQuery.initial().withFilter(WebsiteQuery.Filter.ONLINE).apply(websites)));
        // Dimatikan dengan perubahan tidak termasuk "Ada perubahan" (hanya website online).
        assertEquals(List.of("2"),
                ids(WebsiteQuery.initial().withFilter(WebsiteQuery.Filter.CHANGED).apply(websites)));
    }

    @Test
    public void searchIsCaseInsensitiveAndCountsIgnoreChip() {
        WebsiteQuery query = WebsiteQuery.initial().withSearch("  osis ").withFilter(WebsiteQuery.Filter.DRAFT);

        assertTrue(query.apply(websites).isEmpty());
        WebsitePage.Counts counts = query.count(websites);
        assertEquals(3, counts.all);
        assertEquals(0, counts.draft);
        assertEquals(2, counts.online);
        assertEquals(1, counts.changed);
    }

    @Test
    public void sortOrders() {
        assertEquals(List.of("1", "2", "3", "4"), ids(WebsiteQuery.initial().apply(websites)));
        assertEquals(List.of("4", "3", "2", "1"),
                ids(WebsiteQuery.initial().withSort(WebsiteQuery.Sort.NEWEST).apply(websites)));
        assertEquals(List.of("4", "1", "3", "2"),
                ids(WebsiteQuery.initial().withSort(WebsiteQuery.Sort.NAME).apply(websites)));
    }

    @Test
    public void onlyUnfilteredQueryIsCached() {
        assertTrue(WebsiteQuery.initial().withSort(WebsiteQuery.Sort.NAME).isUnfiltered());
        assertFalse(WebsiteQuery.initial().withSearch("a").isUnfiltered());
        assertFalse(WebsiteQuery.initial().withFilter(WebsiteQuery.Filter.ONLINE).isUnfiltered());
    }

    private static Website website(String id, String name, WebsiteStatus status, boolean changed, long updatedAt,
                                   long createdAt) {
        return new Website(id, "org", name, ProjectMode.TEMPLATE, status, changed, null, 0, null, createdAt,
                updatedAt, null, null);
    }

    private static List<String> ids(List<Website> list) {
        List<String> ids = new ArrayList<>();
        for (Website website : list) {
            ids.add(website.id);
        }
        return ids;
    }
}
