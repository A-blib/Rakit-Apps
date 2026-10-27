package com.aris.templateapp.portal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/** Daftar lengkap template unggulan landing page, berurutan. Daftar kosong = tidak ada yang dipajang. */
public record LandingTemplatesRequest(
        @NotNull @Size(max = 12, message = "Maksimal 12 template unggulan") List<@NotNull UUID> templateIds) {
}
