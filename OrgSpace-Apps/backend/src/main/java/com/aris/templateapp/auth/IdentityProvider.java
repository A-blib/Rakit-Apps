package com.aris.templateapp.auth;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/** Metode login. {@code local} = email + password. */
public enum IdentityProvider implements PersistableEnum {

    LOCAL("local", "email"),
    GOOGLE("google", "Google"),
    GITHUB("github", "GitHub");

    private final String value;
    private final String label;

    IdentityProvider(String value, String label) {
        this.value = value;
        this.label = label;
    }

    /** Nama yang enak dibaca manusia, untuk pesan error seperti "Silakan masuk dengan Google." */
    public String label() {
        return label;
    }

    // Nilai teks yang sama dipakai di database dan di JSON.
    @Override
    @JsonValue
    public String value() {
        return value;
    }

    // autoApply: semua field bertipe IdentityProvider di Entity otomatis memakai converter ini.
    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<IdentityProvider> {
        public JpaConverter() {
            super(IdentityProvider.class);
        }
    }
}
