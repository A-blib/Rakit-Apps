package com.aris.templateapp.data.remote.dto;

/** Body POST /providers/me/uploads/sessions. {@code templateId} diisi untuk "Upload file perbaikan". */
public class CreateUploadSessionDto {
    public final String fileName;
    public final long totalSize;
    public final String templateId;

    public CreateUploadSessionDto(String fileName, long totalSize, String templateId) {
        this.fileName = fileName;
        this.totalSize = totalSize;
        this.templateId = templateId;
    }
}
