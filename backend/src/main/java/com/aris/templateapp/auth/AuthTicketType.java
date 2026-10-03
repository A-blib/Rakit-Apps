package com.aris.templateapp.auth;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

/** Jenis tiket sekali pakai beserta masa berlakunya. */
public enum AuthTicketType implements PersistableEnum {

    /** State OAuth GitHub: dibuat saat membuat URL login, dicek saat callback (mencegah serangan CSRF). */
    GITHUB_STATE(Duration.ofMinutes(10)),
    /** Hasil login GitHub yang ditukar app menjadi token lewat {@code /auth/github/exchange}. */
    LOGIN_RESULT(Duration.ofMinutes(2)),
    /** Identitas yang menunggu disambungkan ke akun lama setelah user membuktikan kepemilikan akun itu. */
    LINK(Duration.ofMinutes(10));

    private final Duration ttl;

    AuthTicketType(Duration ttl) {
        this.ttl = ttl;
    }

    public Duration ttl() {
        return ttl;
    }

    // Di database nilainya sama dengan nama konstanta (huruf besar), sesuai CHECK constraint V4.
    @Override
    public String value() {
        return name();
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<AuthTicketType> {
        public JpaConverter() {
            super(AuthTicketType.class);
        }
    }
}
