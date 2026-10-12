package com.aris.templateapp.project.dto;

import jakarta.validation.constraints.Size;

/** @param name kosong = "{nama asli} (2)", "(3)", dst. */
public record DuplicateWebsiteRequest(
        @Size(max = 100, message = "Nama website maksimal 100 karakter")
        String name) {
}
