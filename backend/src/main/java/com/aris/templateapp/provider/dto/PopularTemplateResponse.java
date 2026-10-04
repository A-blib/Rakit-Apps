package com.aris.templateapp.provider.dto;

import java.util.UUID;

/** Satu baris "Template populer" (bagian 3.8). Angka berlaku untuk periode yang dipilih. */
public record PopularTemplateResponse(int rank, UUID id, String name, String thumbnailUrl, long downloads, long views) {
}
