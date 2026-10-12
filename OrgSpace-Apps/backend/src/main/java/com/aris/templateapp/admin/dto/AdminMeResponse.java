package com.aris.templateapp.admin.dto;

import java.util.UUID;

/** Dipakai portal web setelah login untuk memastikan akun ini tim internal (selain itu 403 ADMIN_ONLY). */
public record AdminMeResponse(UUID id, String displayName, String email, String avatarUrl) {
}
