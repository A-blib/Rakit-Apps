package com.aris.templateapp.template;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/** Jenis kejadian template: detail dibuka (view) atau website hasil template diexport (download). */
public enum TemplateEventType implements PersistableEnum {

    VIEW("view"),
    DOWNLOAD("download");

    private final String value;

    TemplateEventType(String value) {
        this.value = value;
    }

    // Nilai teks yang sama dipakai di database dan di JSON.
    @Override
    @JsonValue
    public String value() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<TemplateEventType> {
        public JpaConverter() {
            super(TemplateEventType.class);
        }
    }
}
