package com.aris.templateapp.site.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

public record DirectPublishRequest(
        @NotBlank(message = "Nama website wajib diisi")
        String name,
        UUID sourceTemplateId,
        Integer templateVersion,
        Map<String, Object> content,
        Integer missingCount,
        String slug
) {
}
