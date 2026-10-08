package com.aris.templateapp.upload.check;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/**
 * Tahap pengecekan yang sedang berjalan. App menampilkannya sebagai daftar yang dicentang satu per satu
 * (bagian 5.8): Mengunggah file → Membuka ZIP → Memeriksa struktur & file → Memeriksa HTML & library → Memeriksa ukuran.
 */
public enum CheckStage implements PersistableEnum {

    UPLOADED("uploaded"),
    OPENING_ZIP("opening_zip"),
    STRUCTURE("structure"),
    HTML_LIBRARY("html_library"),
    SIZE("size"),
    DONE("done");

    private final String value;

    CheckStage(String value) {
        this.value = value;
    }

    // Nilai teks yang sama dipakai di database dan di JSON.
    @Override
    @JsonValue
    public String value() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<CheckStage> {
        public JpaConverter() {
            super(CheckStage.class);
        }
    }
}
