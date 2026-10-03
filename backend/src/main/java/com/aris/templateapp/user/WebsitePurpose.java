package com.aris.templateapp.user;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/** Tujuan website yang dipilih saat onboarding pembuat website. */
public enum WebsitePurpose implements PersistableEnum {

    SEKOLAH("sekolah"),
    ORGANISASI("organisasi"),
    UMKM("umkm"),
    INSTANSI("instansi"),
    PRIBADI("pribadi"),
    LAINNYA("lainnya");

    private final String value;

    WebsitePurpose(String value) {
        this.value = value;
    }

    // Nilai teks yang sama dipakai di database dan di JSON.
    @Override
    @JsonValue
    public String value() {
        return value;
    }

    // autoApply: semua field bertipe WebsitePurpose di Entity otomatis memakai converter ini.
    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<WebsitePurpose> {
        public JpaConverter() {
            super(WebsitePurpose.class);
        }
    }
}
