package com.aris.templateapp.organization.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Serah terima kepemilikan.
 *
 * @param keepMeAs peran Pemilik lama setelah serah terima: {@code admin}, {@code editor}, atau {@code leave} (keluar)
 */
public record TransferOwnershipRequest(
        @NotNull(message = "Pilih anggota penerima") UUID userId,
        @NotNull(message = "Pilih peranmu setelah serah terima")
        @Pattern(regexp = "admin|editor|leave", message = "Pilihan tidak valid")
        String keepMeAs) {
}
