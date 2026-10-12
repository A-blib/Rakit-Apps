package com.aris.templateapp.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InvitationCodeRequest(
        @NotBlank(message = "Masukkan kode undangan") @Size(max = 20, message = "Kode undangan tidak valid") String code) {
}
