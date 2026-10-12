package com.aris.templateapp.admin.dto;

import java.time.Instant;
import java.util.List;

/**
 * Kesehatan backend untuk dashboard.
 *
 * @param schemaVersion versi migrasi Flyway terakhir yang terpasang (mis. "21")
 */
public record SystemInfoResponse(String application, List<String> activeProfiles, String javaVersion,
                                 Instant startedAt, long uptimeSeconds, Instant serverTime, String timezone,
                                 boolean databaseOk, String schemaVersion) {
}
