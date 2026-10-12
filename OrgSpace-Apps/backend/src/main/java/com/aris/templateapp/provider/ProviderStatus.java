package com.aris.templateapp.provider;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/**
 * Status akun penyedia template: langsung {@code active} setelah mengisi form; {@code suspended} adalah rem darurat
 * untuk provider yang mengunggah konten berbahaya. Diubah manual lewat SQL karena belum ada panel admin.
 */
public enum ProviderStatus implements PersistableEnum {

    ACTIVE("active"),
    SUSPENDED("suspended");

    private final String value;

    ProviderStatus(String value) {
        this.value = value;
    }

    // Nilai teks yang sama dipakai di database dan di JSON.
    @Override
    @JsonValue
    public String value() {
        return value;
    }

    // autoApply: semua field bertipe ProviderStatus di Entity otomatis memakai converter ini.
    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<ProviderStatus> {
        public JpaConverter() {
            super(ProviderStatus.class);
        }
    }
}
