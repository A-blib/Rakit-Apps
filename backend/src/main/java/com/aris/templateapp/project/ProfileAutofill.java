package com.aris.templateapp.project;

import com.aris.templateapp.organization.Organization;
import com.aris.templateapp.upload.marking.MarkingData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Isi awal website dari Profil organisasi (alur-buat-website-via-template.md 11.6, K10 v1): setiap isian yang oleh
 * kreator dihubungkan ke kolom profil ({@code profileKey}) langsung terisi, dengan penanda {@code fromProfile}.
 * Kolom profil yang kosong dilewati (isian tetap memakai contoh template, atau disembunyikan bila
 * {@code hideWhenEmpty}).
 * <p>
 * Media sosial disimpan sebagai username (lihat {@code SocialLinks}), jadi di sini disusun menjadi link lengkap.
 */
final class ProfileAutofill {

    private ProfileAutofill() {
    }

    /**
     * @param logoAsset dipanggil hanya jika ada isian logo dan organisasi punya logo; mengembalikan id aset salinan
     *                  logo milik project ini (atau null jika gagal)
     */
    static Map<String, Object> content(MarkingData marking, Organization org, Supplier<UUID> logoAsset) {
        Map<String, Object> fields = new HashMap<>();
        if (marking != null && marking.fields() != null) {
            for (MarkingData.Field field : marking.fields()) {
                if (field.profileKey() == null) {
                    continue;
                }
                Map<String, Object> value = value(field, org, logoAsset);
                if (value != null) {
                    value.put("fromProfile", true);
                    fields.put(field.key(), value);
                }
            }
        }
        Map<String, Object> content = new HashMap<>();
        content.put("fields", fields);
        content.put("styles", new HashMap<>());
        content.put("theme", new HashMap<>());
        return content;
    }

    private static Map<String, Object> value(MarkingData.Field field, Organization org, Supplier<UUID> logoAsset) {
        String key = field.profileKey();
        if ("logo".equals(key)) {
            if (org.getLogoUrl() == null) {
                return null;
            }
            UUID asset = logoAsset.get();
            return asset == null ? null : map("image", ContentAssets.PREFIX + asset);
        }
        String text = text(key, org);
        if (text != null) {
            return map("text", text);
        }
        String href = href(key, org);
        if (href == null) {
            return null;
        }
        // Tombol: teks tombol tetap dari template (mis. "Chat WhatsApp"), hanya tujuannya yang diisi.
        return map("href", href);
    }

    static String text(String key, Organization org) {
        return switch (key) {
            case "name" -> blankToNull(org.getName());
            case "short_name" -> blankToNull(org.getShortName() != null ? org.getShortName() : org.getName());
            case "institution_name" -> blankToNull(org.getInstitutionName());
            case "address" -> blankToNull(org.getAddress());
            default -> null;
        };
    }

    static String href(String key, Organization org) {
        return switch (key) {
            case "email" -> org.getEmail() == null ? null : "mailto:" + org.getEmail();
            case "whatsapp" -> org.getWhatsapp() == null ? null : "https://wa.me/" + org.getWhatsapp();
            case "instagram" -> org.getInstagram() == null ? null : "https://instagram.com/" + org.getInstagram();
            case "tiktok" -> org.getTiktok() == null ? null : "https://www.tiktok.com/@" + org.getTiktok();
            case "youtube" -> org.getYoutube() == null ? null : "https://www.youtube.com/" + org.getYoutube();
            default -> null;
        };
    }

    private static Map<String, Object> map(String prop, String value) {
        Map<String, Object> result = new HashMap<>();
        result.put(prop, value);
        return result;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
