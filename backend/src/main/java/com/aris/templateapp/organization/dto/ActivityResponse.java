package com.aris.templateapp.organization.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Satu baris Riwayat aktivitas. App menyusun kalimatnya dari {@code action} + {@code metadata}
 * (mis. "Rina mengubah peran Budi menjadi Admin"), sehingga teks tetap di strings.xml.
 *
 * @param actorName null untuk aksi sistem atau akun yang sudah dihapus
 */
public record ActivityResponse(UUID id, String actorName, String action, String targetType, UUID targetId,
                               Map<String, Object> metadata, Instant createdAt) {
}
