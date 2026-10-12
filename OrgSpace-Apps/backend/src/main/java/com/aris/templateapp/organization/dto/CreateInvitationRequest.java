package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;
import jakarta.validation.constraints.NotNull;

/** Peran untuk anggota yang bergabung lewat kode ini: {@code editor} (bawaan, F3) atau {@code admin}. */
public record CreateInvitationRequest(@NotNull(message = "Pilih peran") MemberRole role) {
}
