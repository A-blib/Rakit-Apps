package com.aris.templateapp.upload.dto;

import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.template.dto.CheckResponse;
import com.aris.templateapp.upload.check.TechInfo;

import java.util.UUID;

/**
 * Status pengecekan satu upload untuk layar "Pengecekan file" (bagian 5.8) dan "Belum memenuhi standar" (bagian 5.4).
 *
 * @param status   checking → masih berjalan; draft → lolos (boleh ada peringatan); check_failed → ada error
 * @param techInfo info teknis otomatis, terisi jika ZIP bisa dibaca sampai akhir
 */
public record UploadCheckResponse(UUID templateId, TemplateStatus status, String fileName, Long fileSize,
                                  int wizardStep, CheckResponse check, TechInfo techInfo) {
}
