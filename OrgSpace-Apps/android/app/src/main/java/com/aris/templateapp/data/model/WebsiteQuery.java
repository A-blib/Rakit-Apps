package com.aris.templateapp.data.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Pilihan di tab Website: chip status, pencarian, dan urutan. Saat offline, aturan yang sama dengan server
 * diterapkan ke cache Room lewat {@link #apply} dan {@link #count}, supaya tampilan tetap konsisten.
 */
public final class WebsiteQuery {

    public enum Filter {
        ALL("all"), DRAFT("draft"), ONLINE("online"), CHANGED("changed");

        public final String value;

        Filter(String value) {
            this.value = value;
        }
    }

    public enum Sort {
        UPDATED("updated"), NEWEST("newest"), NAME("name");

        public final String value;

        Sort(String value) {
            this.value = value;
        }
    }

    public final Filter filter;
    public final String search;
    public final Sort sort;

    public WebsiteQuery(Filter filter, String search, Sort sort) {
        this.filter = filter;
        this.search = search == null ? "" : search.trim();
        this.sort = sort;
    }

    public static WebsiteQuery initial() {
        return new WebsiteQuery(Filter.ALL, "", Sort.UPDATED);
    }

    /** Daftar lengkap tanpa filter: hanya hasil inilah yang disimpan ke cache. */
    public boolean isUnfiltered() {
        return filter == Filter.ALL && search.isEmpty();
    }

    public boolean matches(Website website) {
        if (!search.isEmpty() && !website.name.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
            return false;
        }
        switch (filter) {
            case DRAFT:
                return website.status == WebsiteStatus.DRAFT;
            case ONLINE:
                return website.status == WebsiteStatus.ONLINE;
            case CHANGED:
                return website.isOnlineWithChanges();
            case ALL:
            default:
                return true;
        }
    }

    public List<Website> apply(List<Website> websites) {
        List<Website> result = new ArrayList<>();
        for (Website website : websites) {
            if (matches(website)) {
                result.add(website);
            }
        }
        result.sort(comparator());
        return result;
    }

    /** Jumlah per chip dari cache: pencarian ikut dihitung, chip status tidak (sama seperti server). */
    public WebsitePage.Counts count(List<Website> websites) {
        WebsiteQuery searchOnly = new WebsiteQuery(Filter.ALL, search, sort);
        long all = 0, draft = 0, online = 0, changed = 0;
        for (Website website : websites) {
            if (!searchOnly.matches(website)) {
                continue;
            }
            all++;
            if (website.status == WebsiteStatus.DRAFT) draft++;
            if (website.status == WebsiteStatus.ONLINE) online++;
            if (website.isOnlineWithChanges()) changed++;
        }
        return new WebsitePage.Counts(all, draft, online, changed);
    }

    private Comparator<Website> comparator() {
        switch (sort) {
            case NEWEST:
                return Comparator.comparingLong((Website w) -> w.createdAt).reversed();
            case NAME:
                return Comparator.comparing((Website w) -> w.name.toLowerCase(Locale.ROOT));
            case UPDATED:
            default:
                return Comparator.comparingLong((Website w) -> w.updatedAt).reversed();
        }
    }

    public WebsiteQuery withFilter(Filter newFilter) {
        return new WebsiteQuery(newFilter, search, sort);
    }

    public WebsiteQuery withSearch(String newSearch) {
        return new WebsiteQuery(filter, newSearch, sort);
    }

    public WebsiteQuery withSort(Sort newSort) {
        return new WebsiteQuery(filter, search, newSort);
    }
}
