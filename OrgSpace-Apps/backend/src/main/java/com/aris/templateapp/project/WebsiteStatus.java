package com.aris.templateapp.project;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Status website di tab Website (alur-pembuatan-website.md 4.2), dihitung server dari {@code sites}.
 * "Online · ada perubahan" bukan status tersendiri: {@code ONLINE} + {@code hasUnpublishedChanges}.
 */
public enum WebsiteStatus {

    /** Belum pernah diterbitkan (belum punya baris {@code sites}). */
    DRAFT("draft"),
    ONLINE("online"),
    /** Dimatikan sementara oleh Admin/Pemilik. */
    OFFLINE("offline"),
    /** Ditangguhkan tim OrgSpace. */
    SUSPENDED("suspended");

    private final String value;

    WebsiteStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    /** Dari kolom {@code sites.status}; null = belum punya alamat = draft. */
    static WebsiteStatus fromSiteStatus(String siteStatus) {
        if (siteStatus == null) {
            return DRAFT;
        }
        return switch (siteStatus) {
            case "online" -> ONLINE;
            case "offline" -> OFFLINE;
            case "suspended" -> SUSPENDED;
            default -> throw new IllegalArgumentException("Status situs tidak dikenal: " + siteStatus);
        };
    }
}
