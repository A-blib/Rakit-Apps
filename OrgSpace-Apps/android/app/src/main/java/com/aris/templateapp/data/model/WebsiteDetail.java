package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

/** Halaman kelola website: data kartu + info terbit + permintaan terbit yang masih terbuka. */
public class WebsiteDetail {

    public final Website website;
    @Nullable public final Long publishedAt;
    @Nullable public final PublishRequest openRequest;
    @Nullable public final String suspensionReason;

    public WebsiteDetail(Website website, @Nullable Long publishedAt, @Nullable PublishRequest openRequest,
                         @Nullable String suspensionReason) {
        this.website = website;
        this.publishedAt = publishedAt;
        this.openRequest = openRequest;
        this.suspensionReason = suspensionReason;
    }
}
