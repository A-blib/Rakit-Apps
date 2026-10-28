package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;

import java.time.Instant;

/**
 * Peran yang sudah punya kredensial Portal. Password (hash-nya pun) tidak pernah dikirim.
 *
 * @param changeRecommended diatur sebelum serah terima terakhir, jadi pengurus lama masih tahu password-nya
 */
public record PortalCredentialResponse(MemberRole role, String username, Instant updatedAt, boolean changeRecommended) {
}
