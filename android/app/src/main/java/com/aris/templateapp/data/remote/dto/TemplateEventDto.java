package com.aris.templateapp.data.remote.dto;

import androidx.annotation.Nullable;

/** Body POST /templates/{id}/events. projectId wajib untuk event download (satu project dihitung sekali). */
public class TemplateEventDto {
    public static final String VIEW = "view";
    public static final String DOWNLOAD = "download";

    public final String type;
    @Nullable
    public final String projectId;
    public final String installId;
    /** Waktu ISO-8601 di HP saat kejadian. */
    public final String occurredAt;

    public TemplateEventDto(String type, String installId, String occurredAt) {
        this(type, null, installId, occurredAt);
    }

    public TemplateEventDto(String type, @Nullable String projectId, String installId, String occurredAt) {
        this.type = type;
        this.projectId = projectId;
        this.installId = installId;
        this.occurredAt = occurredAt;
    }
}
