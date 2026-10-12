package com.aris.templateapp.template;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/** Tingkat masalah: error = template tidak tayang; warning = tetap tayang tetapi disarankan diperbaiki. */
public enum IssueSeverity implements PersistableEnum {

    ERROR("error"),
    WARNING("warning");

    private final String value;

    IssueSeverity(String value) {
        this.value = value;
    }

    // Nilai teks yang sama dipakai di database dan di JSON.
    @Override
    @JsonValue
    public String value() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<IssueSeverity> {
        public JpaConverter() {
            super(IssueSeverity.class);
        }
    }
}
