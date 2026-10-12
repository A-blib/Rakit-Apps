package com.aris.templateapp.organization.dto;

import jakarta.validation.constraints.NotBlank;

/** Konfirmasi hapus: user mengetik ulang nama organisasi (F5). */
public record DeleteOrganizationRequest(@NotBlank(message = "Ketik nama organisasi") String confirmName) {
}
