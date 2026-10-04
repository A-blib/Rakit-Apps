package com.aris.templateapp.data.remote.dto;

/** Body POST /templates/{id}/events. projectId hanya untuk event download (belum dipakai di versi awal). */
public class TemplateEventDto {
    public static final String VIEW = "view";

    public final String type;
    public final String installId;
    /** Waktu ISO-8601 di HP saat kejadian. */
    public final String occurredAt;

    public TemplateEventDto(String type, String installId, String occurredAt) {
        this.type = type;
        this.installId = installId;
        this.occurredAt = occurredAt;
    }
}
