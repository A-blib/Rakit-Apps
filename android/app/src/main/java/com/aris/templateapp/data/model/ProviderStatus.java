package com.aris.templateapp.data.model;

/** Status verifikasi penyedia template. */
public enum ProviderStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    SUSPENDED("suspended");

    private final String value;

    ProviderStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /** null jika user belum mendaftar sebagai provider atau nilainya tidak dikenal. */
    public static ProviderStatus fromValue(String value) {
        for (ProviderStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
