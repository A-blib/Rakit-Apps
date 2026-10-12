package com.aris.templateapp.organization;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * Membuat dan menormalkan kode undangan 8 karakter, ditampilkan sebagai {@code XXXX-XXXX} (F1).
 * Alfabet tanpa huruf/angka yang mirip (0/O, 1/I) supaya tidak salah ketik saat dibacakan di grup.
 */
@Component
public class InvitationCodes {

    static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    static final int LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String newCode() {
        StringBuilder code = new StringBuilder(LENGTH + 1);
        for (int i = 0; i < LENGTH; i++) {
            if (i == LENGTH / 2) {
                code.append('-');
            }
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    /**
     * Bentuk baku sebelum di-hash: huruf besar, tanpa spasi/tanda hubung. "osis 7k2p" dan "OSIS-7K2P" dianggap sama,
     * karena user sering mengetik ulang kode dari chat.
     */
    public static String normalize(String input) {
        return input == null ? "" : input.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }
}
