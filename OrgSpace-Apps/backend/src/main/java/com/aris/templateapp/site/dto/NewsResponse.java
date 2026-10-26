package com.aris.templateapp.site.dto;

import com.aris.templateapp.site.SiteNews;

import java.time.Instant;
import java.util.UUID;

/** Berita untuk Portal Pengurus: hanya field yang ditampilkan (tanpa organizationId/createdBy internal). */
public record NewsResponse(UUID id, String title, String summary, String content, String imageUrl, Instant publishedAt) {

    public static NewsResponse of(SiteNews news) {
        return new NewsResponse(news.getId(), news.getTitle(), news.getSummary(), news.getContent(), news.getImageUrl(),
                news.getPublishedAt());
    }
}
