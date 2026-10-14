package com.aris.templateapp.data.remote.dto;

/** Folder di "Template Anda" (GET /providers/me/template-folders). */
public class TemplateFolderDto {
    public String id;
    public String name;
    public long templateCount;
    /** Waktu ISO-8601. */
    public String updatedAt;
}
