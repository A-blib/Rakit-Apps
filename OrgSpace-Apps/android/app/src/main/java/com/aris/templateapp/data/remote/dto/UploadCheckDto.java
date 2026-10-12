package com.aris.templateapp.data.remote.dto;

/**
 * Status pengecekan satu upload (GET /providers/me/uploads/{id}/check).
 * {@code status}: checking (masih berjalan) · draft (lolos) · check_failed (ada error).
 */
public class UploadCheckDto {
    public String templateId;
    public String status;
    public String fileName;
    public Long fileSize;
    public int wizardStep;
    public TemplateDetailDto.CheckDto check;
    public TechInfoDto techInfo;
}
