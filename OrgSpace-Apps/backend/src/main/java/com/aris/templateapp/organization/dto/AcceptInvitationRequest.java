package com.aris.templateapp.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(
        @NotBlank(message = "Masukkan kode undangan") @Size(max = 20, message = "Kode undangan tidak valid") String code,
        @Size(max = 60, message = "Jabatan maksimal 60 karakter") String positionTitle) {
}
