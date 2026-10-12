package com.aris.templateapp.core.template;

import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Aturan link isian (alur-buat-website-via-template.md bagian 6.4): hanya {@code https://}, {@code http://},
 * {@code mailto:}, {@code tel:}, dan {@code https://wa.me/...}. Skema lain, terutama {@code javascript:}, ditolak
 * karena bisa menjalankan kode di website hasil export.
 */
public final class LinkRules {

    private static final Pattern WEB = Pattern.compile("^https?://[^\\s/?#]+\\.[^\\s]+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAIL = Pattern.compile("^mailto:[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEL = Pattern.compile("^tel:\\+?[0-9][0-9 -]{4,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern WA = Pattern.compile("^https://wa\\.me/[0-9]{8,15}(\\?.*)?$", Pattern.CASE_INSENSITIVE);

    private LinkRules() {
    }

    public static boolean isValid(@Nullable String href) {
        if (href == null) {
            return false;
        }
        String value = href.trim();
        return WEB.matcher(value).matches() || MAIL.matcher(value).matches() || TEL.matcher(value).matches()
                || WA.matcher(value).matches();
    }

    /**
     * Link bawaan provider yang menunjuk ke luar website (perlu diganti pemakai, mis. nomor WhatsApp contoh).
     * Link ke halaman sendiri seperti {@code kontak.html} atau {@code #menu} tidak perlu diganti.
     */
    public static boolean isExternal(@Nullable String href) {
        if (href == null) {
            return false;
        }
        String value = href.trim().toLowerCase(Locale.ROOT);
        return value.startsWith("http://") || value.startsWith("https://") || value.startsWith("mailto:")
                || value.startsWith("tel:") || value.startsWith("//");
    }

    /**
     * Nomor WhatsApp → link {@code https://wa.me/62…}. Menerima "0812-3456-789", "+62 812…", atau "62812…".
     *
     * @return null jika bukan nomor yang masuk akal
     */
    @Nullable
    public static String whatsappLink(@Nullable String number) {
        if (number == null) {
            return null;
        }
        String digits = number.replaceAll("[^0-9]", "");
        if (digits.startsWith("0")) {
            digits = "62" + digits.substring(1);
        } else if (digits.startsWith("8")) {
            digits = "62" + digits;
        }
        return digits.length() >= 9 && digits.length() <= 15 ? "https://wa.me/" + digits : null;
    }

    /** Nomor dari link wa.me (untuk mengisi ulang kolom nomor WhatsApp), atau null. */
    @Nullable
    public static String whatsappNumber(@Nullable String href) {
        if (href == null || !WA.matcher(href.trim()).matches()) {
            return null;
        }
        String digits = href.trim().substring("https://wa.me/".length()).replaceAll("\\?.*$", "");
        return digits.startsWith("62") ? "0" + digits.substring(2) : digits;
    }
}
