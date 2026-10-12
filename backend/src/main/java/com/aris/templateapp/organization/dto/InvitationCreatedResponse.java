package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;

import java.time.Instant;
import java.util.UUID;

/** Kode undangan hanya dikirim sekali di sini; server hanya menyimpan hash-nya. */
public record InvitationCreatedResponse(UUID id, String code, MemberRole role, Instant expiresAt) {
}
