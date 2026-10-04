package com.aris.templateapp.data.model;

/** Status akun penyedia template: langsung aktif setelah mengisi form; ditangguhkan sebagai rem darurat. */
public enum ProviderStatus {
    ACTIVE("active"),
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
