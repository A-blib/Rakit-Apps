package com.aris.templateapp.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.local.WebsiteEntity;
import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsiteStatus;
import com.aris.templateapp.data.remote.dto.WebsiteSummaryDto;

import org.junit.Test;

public class WebsiteMapperTest {

    @Test
    public void mapsServerCardAndSurvivesCacheRoundTrip() {
        WebsiteSummaryDto dto = new WebsiteSummaryDto();
        dto.id = "w1";
        dto.name = "Website OSIS 2026";
        dto.mode = "assembled";
        dto.status = "online";
        dto.hasUnpublishedChanges = true;
        dto.siteUrl = "https://osis-smansa.orgspace.site";
        dto.missingCount = 3;
        dto.updatedAt = "2026-10-12T02:00:00Z";
        dto.updatedByName = "Budi";
        dto.lockedBy = new WebsiteSummaryDto.LockHolderDto();
        dto.lockedBy.name = "Rina";

        Website website = WebsiteMapper.toModel("org-1", dto);

        assertEquals(ProjectMode.CUSTOM, website.mode);
        assertEquals(WebsiteStatus.ONLINE, website.status);
        assertTrue(website.isOnlineWithChanges());
        assertEquals("Rina", website.lockedByName);
        assertEquals(1791770400000L, website.updatedAt);

        WebsiteEntity entity = WebsiteMapper.toEntity(website, 99);
        assertEquals("assembled", entity.mode);
        assertEquals(website, WebsiteMapper.toModel(entity));
    }

    @Test
    public void unknownValuesFallBackSafely() {
        WebsiteSummaryDto dto = new WebsiteSummaryDto();
        dto.id = "w2";
        dto.name = "Baru";
        dto.mode = "template";
        dto.status = "status-baru-dari-server";
        dto.updatedAt = "bukan-tanggal";

        Website website = WebsiteMapper.toModel("org-1", dto);

        assertEquals(WebsiteStatus.DRAFT, website.status);
        assertEquals(0, website.updatedAt);
        assertNull(website.lockedByName);
    }

    @Test
    public void mapsWebsiteDetailWithTemplateAndContent() {
        com.aris.templateapp.data.remote.dto.WebsiteDetailDto dto = new com.aris.templateapp.data.remote.dto.WebsiteDetailDto();
        dto.website = new WebsiteSummaryDto();
        dto.website.id = "web-1";
        dto.website.name = "Web Dinamis Organisasi";
        dto.website.mode = "template";
        dto.website.status = "online";
        dto.website.sourceTemplateId = "tpl-123";
        dto.templateVersion = 2;
        dto.publishedAt = "2026-10-14T05:00:00Z";

        java.util.Map<String, Object> content = new java.util.HashMap<>();
        java.util.Map<String, Object> fields = new java.util.HashMap<>();
        java.util.Map<String, Object> titleField = new java.util.HashMap<>();
        titleField.put("text", "Halo Dunia");
        fields.put("judul", titleField);
        content.put("fields", fields);
        dto.content = content;

        com.aris.templateapp.data.model.WebsiteDetail detail = WebsiteMapper.toModel("org-1", dto);

        assertEquals("web-1", detail.website.id);
        assertEquals("Web Dinamis Organisasi", detail.website.name);
        assertEquals(ProjectMode.TEMPLATE, detail.website.mode);
        assertEquals(WebsiteStatus.ONLINE, detail.website.status);
        assertEquals("tpl-123", detail.sourceTemplateId);
        assertEquals(Integer.valueOf(2), detail.templateVersion);
        assertEquals(content, detail.content);
        assertEquals(Long.valueOf(1791954000000L), detail.publishedAt);
    }
}
