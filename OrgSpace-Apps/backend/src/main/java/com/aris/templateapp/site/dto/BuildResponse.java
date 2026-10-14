package com.aris.templateapp.site.dto;

import java.util.List;
import java.util.UUID;

public record BuildResponse(
        UUID id,
        UUID siteId,
        String kind,
        String status,
        int queuePosition,
        String errorCode,
        List<String> problems,
        Integer releaseVersion) {
}
