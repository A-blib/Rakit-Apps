package com.aris.templateapp.upload;

import java.util.UUID;

/** Diterbitkan saat ZIP selesai diupload; memicu pengecekan di latar belakang. */
public record UploadCompletedEvent(UUID templateId, UUID checkId) {
}
