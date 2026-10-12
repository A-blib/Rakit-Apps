package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Satu halaman tab Website. Jika diambil dari cache karena offline, {@link #offlineSince} berisi waktu data
 * terakhir disimpan (banner "Kamu sedang offline. Data terakhir disimpan ...").
 */
public class WebsitePage {

    public final List<Website> items;
    public final Counts counts;
    public final int sitesUsed;
    public final int sitesLimit;
    public final int page;
    public final boolean hasMore;
    @Nullable public final Long offlineSince;

    public WebsitePage(List<Website> items, Counts counts, int sitesUsed, int sitesLimit, int page, boolean hasMore,
                       @Nullable Long offlineSince) {
        this.items = Collections.unmodifiableList(items);
        this.counts = counts;
        this.sitesUsed = sitesUsed;
        this.sitesLimit = sitesLimit;
        this.page = page;
        this.hasMore = hasMore;
        this.offlineSince = offlineSince;
    }

    /** Jumlah untuk chip filter (sudah memperhitungkan kata pencarian). */
    public static class Counts {
        public final long all;
        public final long draft;
        public final long online;
        public final long changed;

        public Counts(long all, long draft, long online, long changed) {
            this.all = all;
            this.draft = draft;
            this.online = online;
            this.changed = changed;
        }
    }
}
