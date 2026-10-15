package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.util.Map;

/** Halaman kelola website: data kartu + info terbit + permintaan terbit yang masih terbuka. */
public class WebsiteDetail {

    public final Website website;
    @Nullable public final Long publishedAt;
    @Nullable public final PublishRequest openRequest;
    @Nullable public final String suspensionReason;
    @Nullable public final String sourceTemplateId;
    @Nullable public final Integer templateVersion;
    @Nullable public final Map<String, Object> content;

    public WebsiteDetail(Website website, @Nullable Long publishedAt, @Nullable PublishRequest openRequest,
                         @Nullable String suspensionReason, @Nullable String sourceTemplateId,
                         @Nullable Integer templateVersion, @Nullable Map<String, Object> content) {
        this.website = website;
        this.publishedAt = publishedAt;
        this.openRequest = openRequest;
        this.suspensionReason = suspensionReason;
        this.sourceTemplateId = sourceTemplateId;
        this.templateVersion = templateVersion;
        this.content = content;
    }

    public WebsiteDetail(Website website, @Nullable Long publishedAt, @Nullable PublishRequest openRequest,
                         @Nullable String suspensionReason) {
        this(website, publishedAt, openRequest, suspensionReason, null, null, null);
    }
}
