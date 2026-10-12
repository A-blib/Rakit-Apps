package com.aris.templateapp.project;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.upload.marking.MarkingData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Pemeriksaan isi website dari HP sebelum disimpan (alur-buat-website-via-template.md 12.2 "Aturan"). HP juga
 * memeriksa, tetapi server tidak boleh percaya begitu saja: isi ini nanti disusun menjadi HTML yang diterbitkan.
 * <ul>
 *   <li>hanya kunci isian, gaya, dan variabel tema yang ada di tandaan template;</li>
 *   <li>panjang teks sesuai {@code maxLength};</li>
 *   <li>link hanya {@code https://, http://, mailto:, tel:};</li>
 *   <li>warna {@code #rrggbb}, angka di dalam rentang;</li>
 *   <li>gambar hanya {@code asset:{id}} milik project yang sama ({@code local:} ditolak).</li>
 * </ul>
 */
final class ContentValidator {

    private static final Set<String> TOP_LEVEL = Set.of("fields", "styles", "theme");
    private static final Set<String> VALUE_PROPS = Set.of("text", "href", "image", "fromProfile");
    private static final Pattern COLOR = Pattern.compile("^#[0-9a-fA-F]{6}$");
    private static final Pattern SIZE = Pattern.compile("^\\d{1,3}(\\.\\d{1,2})?(px|rem|em|%)$");
    private static final int DEFAULT_MAX_TEXT = 5000;
    private static final int MAX_HREF = 2000;
    private static final int RADIUS_MAX = 64;

    private ContentValidator() {
    }

    /** @param assetOwned true jika id aset milik project ini */
    static void validate(Map<String, Object> content, MarkingData marking, Predicate<UUID> assetOwned) {
        require(content != null, "Isi website kosong.");
        for (String key : content.keySet()) {
            require(TOP_LEVEL.contains(key), "Bagian isi tidak dikenal: " + key);
        }
        Map<String, MarkingData.Field> fields = new HashMap<>();
        for (MarkingData.Field field : list(marking == null ? null : marking.fields())) {
            fields.put(field.key(), field);
        }
        Map<String, String> themeTypes = new HashMap<>();
        for (MarkingData.ThemeVar theme : list(marking == null ? null : marking.theme())) {
            themeTypes.put(theme.var(), theme.type());
        }

        for (Map.Entry<String, Object> entry : map(content.get("fields"), "fields").entrySet()) {
            MarkingData.Field field = fields.get(entry.getKey());
            require(field != null, "Isian tidak ada di template: " + entry.getKey());
            validateValue(field, map(entry.getValue(), entry.getKey()), assetOwned);
        }
        for (Map.Entry<String, Object> entry : map(content.get("styles"), "styles").entrySet()) {
            MarkingData.Field field = fields.get(entry.getKey());
            require(field != null, "Gaya untuk isian yang tidak ada: " + entry.getKey());
            validateStyles(field, map(entry.getValue(), entry.getKey()));
        }
        for (Map.Entry<String, Object> entry : map(content.get("theme"), "theme").entrySet()) {
            String type = themeTypes.get(entry.getKey());
            require(type != null, "Variabel tema tidak ada di template: " + entry.getKey());
            String value = string(entry.getValue(), entry.getKey());
            require("color".equals(type) ? COLOR.matcher(value).matches() : SIZE.matcher(value).matches(),
                    "Nilai tema tidak valid: " + entry.getKey());
        }
    }

    private static void validateValue(MarkingData.Field field, Map<String, Object> value, Predicate<UUID> assetOwned) {
        for (String prop : value.keySet()) {
            require(VALUE_PROPS.contains(prop), "Bagian isian tidak dikenal: " + field.key() + "." + prop);
        }
        Object fromProfile = value.get("fromProfile");
        require(fromProfile == null || fromProfile instanceof Boolean, "fromProfile harus true/false: " + field.key());
        if (value.get("text") != null) {
            String text = string(value.get("text"), field.key());
            int max = field.maxLength() == null ? DEFAULT_MAX_TEXT : field.maxLength();
            require(text.length() <= max, "Teks terlalu panjang (maks " + max + "): " + field.label());
        }
        if (value.get("href") != null) {
            String href = string(value.get("href"), field.key());
            require(href.length() <= MAX_HREF && LinkRule.allowed(href), "Link tidak diizinkan: " + field.label());
        }
        if (value.get("image") != null) {
            require("image".equals(field.type()), "Isian ini bukan gambar: " + field.label());
            String image = string(value.get("image"), field.key());
            require(image.startsWith(ContentAssets.PREFIX), "Gambar belum terunggah: " + field.label());
            UUID id;
            try {
                id = UUID.fromString(image.substring(ContentAssets.PREFIX.length()));
            } catch (IllegalArgumentException e) {
                throw invalid("Rujukan gambar tidak valid: " + field.label());
            }
            require(assetOwned.test(id), "Gambar bukan milik website ini: " + field.label());
        }
    }

    private static void validateStyles(MarkingData.Field field, Map<String, Object> styles) {
        Map<String, MarkingData.Style> allowed = new HashMap<>();
        for (MarkingData.Style style : list(field.styles())) {
            allowed.put(style.prop(), style);
        }
        for (Map.Entry<String, Object> entry : styles.entrySet()) {
            MarkingData.Style style = allowed.get(entry.getKey());
            require(style != null, "Gaya tidak diizinkan kreator: " + field.label() + " " + entry.getKey());
            Object value = entry.getValue();
            switch (entry.getKey()) {
                case "color", "background-color" -> require(value instanceof String text && COLOR.matcher(text).matches(),
                        "Warna tidak valid: " + field.label());
                case "font-size" -> {
                    double size = number(value, field.label());
                    require(style.min() == null || size >= style.min(), "Ukuran huruf terlalu kecil: " + field.label());
                    require(style.max() == null || size <= style.max(), "Ukuran huruf terlalu besar: " + field.label());
                }
                case "border-radius" -> {
                    double radius = number(value, field.label());
                    int max = style.max() == null ? RADIUS_MAX : style.max();
                    require(radius >= 0 && radius <= max, "Sudut di luar rentang: " + field.label());
                }
                default -> throw invalid("Gaya tidak dikenal: " + entry.getKey());
            }
        }
    }

    /** Skema link yang boleh dipakai di website (sama dengan aturan HP, alur-buat-website-via-template.md 6.4). */
    static final class LinkRule {
        private LinkRule() {
        }

        static boolean allowed(String href) {
            String lower = href.strip().toLowerCase();
            return lower.startsWith("https://") || lower.startsWith("http://") || lower.startsWith("mailto:")
                    || lower.startsWith("tel:");
        }
    }

    private static double number(Object value, String label) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text.replace("px", "").strip());
            } catch (NumberFormatException e) {
                throw invalid("Angka tidak valid: " + label);
            }
        }
        throw invalid("Angka tidak valid: " + label);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value, String name) {
        if (value == null) {
            return Map.of();
        }
        require(value instanceof Map<?, ?>, "Bagian isi harus berupa objek: " + name);
        return (Map<String, Object>) value;
    }

    private static String string(Object value, String name) {
        require(value instanceof String, "Nilai harus teks: " + name);
        return (String) value;
    }

    private static <T> List<T> list(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static void require(boolean ok, String message) {
        if (!ok) {
            throw invalid(message);
        }
    }

    private static ApiException invalid(String message) {
        return new ApiException(ErrorCode.VALIDATION_ERROR, message);
    }
}
