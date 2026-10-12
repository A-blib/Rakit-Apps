package com.aris.templateapp.upload.check;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Helper teks kecil yang dipakai banyak aturan. */
final class Texts {

    private static final int SNIPPET_MAX = 120;

    private Texts() {
    }

    /** Membaca file teks sebagai UTF-8; byte yang tidak valid diganti tanda �, BOM di awal dibuang. */
    static String decode(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        return text.startsWith("﻿") ? text.substring(1) : text;
    }

    static boolean isValidUtf8(byte[] bytes) {
        try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    /** Nomor baris (mulai 1) dari posisi karakter di teks. */
    static int lineOf(String text, int index) {
        int line = 1;
        for (int i = 0; i < index && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    /** Potongan kode satu baris yang cukup pendek untuk ditampilkan dan disimpan di laporan. */
    static String snippet(String code) {
        if (code == null) {
            return null;
        }
        String oneLine = code.replaceAll("\\s+", " ").strip();
        return oneLine.length() <= SNIPPET_MAX ? oneLine : oneLine.substring(0, SNIPPET_MAX - 1) + "…";
    }

    /** Ukuran yang mudah dibaca, mis. "4,2 MB" / "350 KB" (format Indonesia memakai koma). */
    static String size(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format(Locale.forLanguageTag("id"), "%.1f MB", bytes / (1024.0 * 1024.0));
        }
        if (bytes >= 1024) {
            return Math.round(bytes / 1024.0) + " KB";
        }
        return bytes + " byte";
    }

    static String extension(String path) {
        String name = fileName(path);
        int dot = name.lastIndexOf('.');
        return dot <= 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    static String fileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }
}
