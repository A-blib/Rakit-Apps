package com.aris.templateapp.data.remote.dto;

import java.util.Map;

/** GET /projects/{id}: data kartu + isi editor + info terbit. */
public class WebsiteDetailDto {
    public WebsiteSummaryDto website;
    public Map<String, Object> content;
    public int contentVersion;
    public String publishedAt;
    public PublishRequestDto openRequest;
    public String suspensionReason;
    public Integer templateVersion;
}
