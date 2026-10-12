package com.aris.templateapp.template;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/** Status template (alur-provider.md bagian 4.3). */
public enum TemplateStatus implements PersistableEnum {

    DRAFT("draft"),
    CHECKING("checking"),
    PUBLISHED("published"),
    CHECK_FAILED("check_failed"),
    DISABLED("disabled");

    private final String value;

    TemplateStatus(String value) {
        this.value = value;
    }

    // Nilai teks yang sama dipakai di database dan di JSON.
    @Override
    @JsonValue
    public String value() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<TemplateStatus> {
        public JpaConverter() {
            super(TemplateStatus.class);
        }
    }
}
