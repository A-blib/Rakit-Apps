package com.aris.templateapp.site.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PublishSiteRequest(
        @Size(min = 3, max = 40, message = "Nama subdomain 3–40 karakter")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Format subdomain hanya huruf kecil, angka, dan strip tunggal")
        String slug) {
}
