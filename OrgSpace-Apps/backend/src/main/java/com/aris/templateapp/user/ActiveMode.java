package com.aris.templateapp.user;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/** Mode yang sedang dipakai user; app membuka mode ini saat dibuka lagi. */
public enum ActiveMode implements PersistableEnum {

    CREATOR("creator"),
    PROVIDER("provider");

    private final String value;

    ActiveMode(String value) {
        this.value = value;
    }

    // Nilai teks yang sama dipakai di database dan di JSON.
    @Override
    @JsonValue
    public String value() {
        return value;
    }

    // autoApply: semua field bertipe ActiveMode di Entity otomatis memakai converter ini.
    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<ActiveMode> {
        public JpaConverter() {
            super(ActiveMode.class);
        }
    }
}
