package com.aris.templateapp.data.mapper;

import androidx.annotation.Nullable;

import com.aris.templateapp.data.local.WebsiteEntity;
import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.PublishRequest;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsiteDetail;
import com.aris.templateapp.data.model.WebsitePage;
import com.aris.templateapp.data.model.WebsiteStatus;
import com.aris.templateapp.data.remote.dto.PublishRequestDto;
import com.aris.templateapp.data.remote.dto.WebsiteDetailDto;
import com.aris.templateapp.data.remote.dto.WebsiteListDto;
import com.aris.templateapp.data.remote.dto.WebsiteSummaryDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** DTO server ↔ model app ↔ cache Room untuk tab Website. */
public final class WebsiteMapper {

    /** Nilai mode di server untuk "Rakit sendiri"; di app tetap memakai {@link ProjectMode#CUSTOM}. */
    static final String MODE_ASSEMBLED = "assembled";

    private WebsiteMapper() {
    }

    public static Website toModel(String organizationId, WebsiteSummaryDto dto) {
        return new Website(dto.id, organizationId, dto.name, modeOf(dto.mode), WebsiteStatus.fromValue(dto.status),
                dto.hasUnpublishedChanges, dto.siteUrl, dto.missingCount, dto.thumbnailUrl, millis(dto.createdAt),
                millis(dto.updatedAt), dto.updatedByName, dto.lockedBy == null ? null : dto.lockedBy.name);
    }

    public static WebsitePage toModel(String organizationId, WebsiteListDto dto) {
        List<Website> items = new ArrayList<>();
        if (dto.items != null) {
            for (WebsiteSummaryDto item : dto.items) {
                items.add(toModel(organizationId, item));
            }
        }
        WebsitePage.Counts counts = dto.counts == null ? new WebsitePage.Counts(0, 0, 0, 0)
                : new WebsitePage.Counts(dto.counts.all, dto.counts.draft, dto.counts.online, dto.counts.changed);
        return new WebsitePage(items, counts, dto.sitesUsed, dto.sitesLimit, dto.page, dto.hasMore, null);
    }

    public static WebsiteDetail toModel(String organizationId, WebsiteDetailDto dto) {
        Instant published = WorkspaceMapper.instant(dto.publishedAt);
        return new WebsiteDetail(toModel(organizationId, dto.website),
                published == null ? null : published.toEpochMilli(),
                dto.openRequest == null ? null : toModel(dto.openRequest), dto.suspensionReason,
                dto.website != null ? dto.website.sourceTemplateId : null,
                dto.templateVersion, dto.content);
    }

    public static PublishRequest toModel(PublishRequestDto dto) {
        return new PublishRequest(dto.id, dto.projectId, dto.projectName, dto.requestedByName, dto.note,
                millis(dto.createdAt));
    }

    public static WebsiteEntity toEntity(Website website, long cachedAt) {
        WebsiteEntity entity = new WebsiteEntity();
        entity.organizationId = website.organizationId;
        entity.id = website.id;
        entity.name = website.name;
        entity.mode = website.mode == ProjectMode.CUSTOM ? MODE_ASSEMBLED : ProjectMode.TEMPLATE.value();
        entity.status = website.status.value();
        entity.hasUnpublishedChanges = website.hasUnpublishedChanges;
        entity.siteUrl = website.siteUrl;
        entity.missingCount = website.missingCount;
        entity.thumbnailUrl = website.thumbnailUrl;
        entity.createdAt = website.createdAt;
        entity.updatedAt = website.updatedAt;
        entity.updatedByName = website.updatedByName;
        entity.lockedByName = website.lockedByName;
        entity.cachedAt = cachedAt;
        return entity;
    }

    public static Website toModel(WebsiteEntity entity) {
        return new Website(entity.id, entity.organizationId, entity.name, modeOf(entity.mode),
                WebsiteStatus.fromValue(entity.status), entity.hasUnpublishedChanges, entity.siteUrl,
                entity.missingCount, entity.thumbnailUrl, entity.createdAt, entity.updatedAt, entity.updatedByName,
                entity.lockedByName);
    }

    static ProjectMode modeOf(@Nullable String value) {
        return MODE_ASSEMBLED.equals(value) ? ProjectMode.CUSTOM : ProjectMode.TEMPLATE;
    }

    private static long millis(@Nullable String value) {
        Instant instant = WorkspaceMapper.instant(value);
        return instant == null ? 0 : instant.toEpochMilli();
    }
}
