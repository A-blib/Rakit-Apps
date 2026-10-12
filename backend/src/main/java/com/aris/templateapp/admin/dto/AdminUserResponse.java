package com.aris.templateapp.admin.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Baris tabel pengguna di dashboard.
 *
 * @param loginMethods   metode login terhubung: local (email), google, github
 * @param providerStatus null jika bukan penyedia template; active / suspended
 * @param lastActiveDay  hari terakhir memakai app/web (null jika belum tercatat sejak monitoring aktif)
 */
public record AdminUserResponse(UUID id, String displayName, String email, String avatarUrl, boolean admin,
                                boolean onboardingCompleted, List<String> loginMethods, String providerStatus,
                                long organizations, long websites, LocalDate lastActiveDay, Instant createdAt) {
}
