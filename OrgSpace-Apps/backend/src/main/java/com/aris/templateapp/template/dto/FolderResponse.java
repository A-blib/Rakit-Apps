package com.aris.templateapp.template.dto;

import java.time.Instant;
import java.util.UUID;

/** Satu folder di "Template Anda" + jumlah template di dalamnya (semua status). */
public record FolderResponse(UUID id, String name, long templateCount, Instant updatedAt) {
}
