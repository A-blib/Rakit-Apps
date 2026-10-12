package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;

import java.time.Instant;
import java.util.UUID;

/** Undangan aktif (tanpa kode) untuk daftar "Undangan" yang bisa dicabut. */
public record InvitationResponse(UUID id, MemberRole role, String createdByName, Instant createdAt, Instant expiresAt) {
}
