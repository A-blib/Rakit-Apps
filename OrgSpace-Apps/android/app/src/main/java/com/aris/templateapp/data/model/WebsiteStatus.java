package com.aris.templateapp.data.model;

/**
 * Status website dari server (alur-pembuatan-website.md 4.2). "Online · ada perubahan" = {@link #ONLINE} +
 * {@link Website#hasUnpublishedChanges}.
 */
public enum WebsiteStatus {
    DRAFT("draft"),
    ONLINE("online"),
    OFFLINE("offline"),
    SUSPENDED("suspended");

    private final String value;

    WebsiteStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /** Nilai tidak dikenal (server lebih baru dari app) dianggap draft agar app tidak crash. */
    public static WebsiteStatus fromValue(String value) {
        for (WebsiteStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return DRAFT;
    }
}
