package com.aris.templateapp.template.dto;

import java.util.UUID;

/** Pindahkan template ke folder; {@code folderId} null = keluarkan dari folder (kembali ke halaman utama). */
public record MoveTemplateRequest(UUID folderId) {
}
