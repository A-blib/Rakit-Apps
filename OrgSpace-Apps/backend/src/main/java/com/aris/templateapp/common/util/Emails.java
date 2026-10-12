package com.aris.templateapp.common.util;

import java.util.Locale;

public final class Emails {

    private Emails() {
    }

    /**
     * Email selalu disimpan dan dicari dalam huruf kecil tanpa spasi di ujung, agar
     * " Aris@Mail.com" dan "aris@mail.com" dianggap akun yang sama.
     * Locale.ROOT dipakai supaya hasil tidak berubah mengikuti bahasa sistem (mis. huruf "I" di locale Turki).
     */
    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
