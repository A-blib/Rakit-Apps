package com.aris.templateapp.organization;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Menormalkan isian kontak Profil organisasi. Pengurus bisa menempel link lengkap atau mengetik "@username";
 * yang disimpan selalu bentuk pendek yang sama, sehingga template tinggal menyusun link-nya.
 * Nilai yang tidak bisa dipahami menghasilkan null; pemanggil yang menolak dengan pesan validasi.
 */
public final class SocialLinks {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._]{1,60}");
    private static final Pattern YOUTUBE_PATH = Pattern.compile("(@[A-Za-z0-9._-]{1,60}|(channel|c|user)/[A-Za-z0-9._-]{1,60})");

    private SocialLinks() {
    }

    /** "https://www.instagram.com/osis.smansa/" atau "@osis.smansa" → "osis.smansa". */
    public static String instagram(String input) {
        return username(input, "instagram.com");
    }

    /** "https://www.tiktok.com/@osis" atau "@osis" → "osis". */
    public static String tiktok(String input) {
        return username(input, "tiktok.com");
    }

    /** "https://youtube.com/@osis" → "@osis"; "youtube.com/channel/UC.." → "channel/UC..". */
    public static String youtube(String input) {
        String value = stripUrl(input, "youtube.com");
        if (value == null) {
            return null;
        }
        if (!value.startsWith("@") && !value.contains("/")) {
            value = "@" + value;
        }
        return YOUTUBE_PATH.matcher(value).matches() ? value : null;
    }

    /** Nomor Indonesia dalam format internasional tanpa "+": "0812-3456-7890" → "6281234567890". */
    public static String whatsapp(String input) {
        if (input == null) {
            return null;
        }
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.startsWith("0")) {
            digits = "62" + digits.substring(1);
        } else if (digits.startsWith("8")) {
            digits = "62" + digits;
        }
        return digits.length() >= 10 && digits.length() <= 15 ? digits : null;
    }

    private static String username(String input, String host) {
        String value = stripUrl(input, host);
        if (value == null) {
            return null;
        }
        if (value.startsWith("@")) {
            value = value.substring(1);
        }
        int slash = value.indexOf('/');
        if (slash >= 0) {
            value = value.substring(0, slash);
        }
        return USERNAME.matcher(value).matches() ? value : null;
    }

    /** Membuang "https://", "www.", nama host, query, dan garis miring di ujung. */
    private static String stripUrl(String input, String host) {
        if (input == null) {
            return null;
        }
        String value = input.trim();
        Matcher url = Pattern.compile("^(https?://)?(www\\.|m\\.)?" + Pattern.quote(host) + "/?", Pattern.CASE_INSENSITIVE)
                .matcher(value);
        if (url.find()) {
            value = value.substring(url.end());
        }
        int query = value.indexOf('?');
        if (query >= 0) {
            value = value.substring(0, query);
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.isEmpty() || value.toLowerCase(Locale.ROOT).contains("http") ? null : value;
    }
}
