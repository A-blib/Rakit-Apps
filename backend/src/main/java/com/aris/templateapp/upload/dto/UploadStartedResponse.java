package com.aris.templateapp.upload.dto;

import java.util.UUID;

/** Upload selesai dan pengecekan dimulai; app lalu memantau {@code GET /uploads/{templateId}/check}. */
public record UploadStartedResponse(UUID templateId, int checkVersion) {
}
