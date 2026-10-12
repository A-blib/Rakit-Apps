package com.aris.templateapp.organization.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LastOrganizationRequest(@NotNull(message = "Pilih ruang kerja") UUID organizationId) {
}
