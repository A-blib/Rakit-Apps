package com.aris.templateapp.upload.marking;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Pengecekan tandaan saat Simpan (alur-fitur-upload.md bagian 5.2, 7.7, 7.12): label tidak kosong, {@code data-key}
 * tidak ganda, setiap elemen benar-benar ada di HTML asli, gaya hanya dari daftar yang boleh. Syarat minimal 3 isian
 * diperiksa saat Kirim, bukan di sini (bagian 7.12).
 */
public final class MarkingValidator {

    public static final Set<String> TYPES = Set.of("text", "paragraph", "image", "link", "button");
    public static final Set<String> STYLE_PROPS = Set.of("color", "background-color", "font-size", "border-radius");
    private static final Set<String> VIEWS = Set.of("mobile", "desktop");
    private static final Set<String> THEME_TYPES = Set.of("color", "size");
    private static final Pattern KEY = Pattern.compile("^[a-z][a-z0-9_]{0,39}$");
    private static final Pattern RATIO = Pattern.compile("^\\d{1,2}:\\d{1,2}$");
    private static final int MAX_FIELDS = 200;

    private MarkingValidator() {
    }

    /**
     * @param idsByPage  nomor elemen yang ada di setiap halaman
     * @param cssVariables nama variabel CSS {@code :root} hasil pengecekan file
     */
    public static void validate(MarkingData data, Map<String, Set<Integer>> idsByPage, Set<String> cssVariables) {
        require(data.pages() != null && !data.pages().isEmpty(), "Data halaman kosong.");
        Set<String> pages = new HashSet<>();
        for (MarkingData.Page page : data.pages()) {
            require(page.file() != null && idsByPage.containsKey(page.file()), "Halaman tidak dikenal: " + page.file());
            require(notBlank(page.name(), 40), "Nama halaman wajib diisi (maks 40 karakter).");
            pages.add(page.file());
        }

        Set<String> sectionIds = new HashSet<>();
        for (MarkingData.Section section : nullSafe(data.sections())) {
            require(section.id() != null && sectionIds.add(section.id()), "Id section kosong atau ganda.");
            require(pages.contains(section.page()), "Section menunjuk halaman yang tidak ada.");
            require(notBlank(section.name(), 40), "Nama section wajib diisi (maks 40 karakter).");
            require(section.tplId() == null || idsByPage.get(section.page()).contains(section.tplId()),
                    "Section menunjuk elemen yang tidak ada.");
        }

        List<MarkingData.Field> fields = nullSafe(data.fields());
        require(fields.size() <= MAX_FIELDS, "Maksimal " + MAX_FIELDS + " isian.");
        Set<String> keys = new HashSet<>();
        Set<String> usedElements = new HashSet<>();
        for (MarkingData.Field field : fields) {
            String name = field.label() == null ? field.key() : field.label();
            require(field.key() != null && KEY.matcher(field.key()).matches(), "Kunci isian tidak valid: " + field.key());
            require(keys.add(field.key()), "Kunci isian ganda: " + field.key());
            require(notBlank(field.label(), 60), "Label isian wajib diisi (maks 60 karakter).");
            require(TYPES.contains(field.type()), "Jenis isian tidak dikenal: " + field.type());
            require(field.hint() == null || field.hint().length() <= 120, "Petunjuk isian maksimal 120 karakter: " + name);
            require(field.maxLength() == null || (field.maxLength() >= 1 && field.maxLength() <= 5000),
                    "Maks karakter harus 1–5000: " + name);
            require(field.aspectRatio() == null || RATIO.matcher(field.aspectRatio()).matches(),
                    "Rasio gambar tidak valid: " + name);
            require(field.sectionId() == null || sectionIds.contains(field.sectionId()), "Section isian tidak ada: " + name);
            validateStyles(field, name);
            require(field.elements() != null && !field.elements().isEmpty() && field.elements().size() <= 20,
                    "Isian harus menunjuk 1–20 elemen: " + name);
            for (MarkingData.Element element : field.elements()) {
                require(pages.contains(element.page()), "Elemen di halaman yang tidak ada: " + name);
                require(idsByPage.get(element.page()).contains(element.tplId()),
                        "Elemen tidak ada di HTML asli (mungkin dibuat JavaScript): " + name);
                require(usedElements.add(element.page() + "#" + element.tplId()),
                        "Satu elemen dipakai dua isian: " + name);
                require(element.visibleIn() != null && !element.visibleIn().isEmpty()
                        && VIEWS.containsAll(element.visibleIn()), "Tampilan elemen tidak valid: " + name);
            }
        }

        Set<String> vars = new HashSet<>();
        for (MarkingData.ThemeVar theme : nullSafe(data.theme())) {
            require(theme.var() != null && cssVariables.contains(theme.var()) && vars.add(theme.var()),
                    "Variabel tema tidak dikenal atau ganda: " + theme.var());
            require(notBlank(theme.label(), 40), "Label tema wajib diisi (maks 40 karakter).");
            require(THEME_TYPES.contains(theme.type()), "Jenis tema tidak dikenal: " + theme.type());
        }
    }

    private static void validateStyles(MarkingData.Field field, String name) {
        Set<String> props = new HashSet<>();
        for (MarkingData.Style style : nullSafe(field.styles())) {
            require(STYLE_PROPS.contains(style.prop()) && props.add(style.prop()), "Gaya tidak dikenal atau ganda: " + name);
            if ("font-size".equals(style.prop())) {
                require(style.min() != null && style.max() != null && style.min() >= 6 && style.max() <= 200
                        && style.min() <= style.max(), "Rentang ukuran huruf tidak valid: " + name);
            }
        }
    }

    private static boolean notBlank(String value, int max) {
        return value != null && !value.isBlank() && value.strip().length() <= max;
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, message);
        }
    }
}
