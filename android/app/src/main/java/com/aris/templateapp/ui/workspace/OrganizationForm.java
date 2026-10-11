package com.aris.templateapp.ui.workspace;

import com.aris.templateapp.R;

import java.util.EnumMap;
import java.util.Map;

/**
 * Isian form "Buat ruang organisasi" (revisi-fondasi-orgspace.md 5.1) dan validasinya. Aturannya sama dengan
 * backend (CreateOrganizationRequest), ditulis tanpa kelas Android agar mudah diuji.
 */
public final class OrganizationForm {

    public enum Field { NAME, SHORT_NAME, LEVEL, CATEGORY, INSTITUTION, CITY, PERIOD_START, PERIOD_END, POSITION }

    static final int NAME_MAX = 100;
    static final int SHORT_NAME_MAX = 40;
    static final int INSTITUTION_MAX = 150;
    static final int CITY_MAX = 100;
    static final int POSITION_MAX = 60;
    static final int YEAR_MIN = 2000;
    static final int YEAR_MAX = 2100;

    public String name;
    public String shortName;
    public String level;
    public String category;
    public String institution;
    public String city;
    public String periodStart;
    public String periodEnd;
    public String position;

    /** Kosong jika form valid; isinya id string pesan error per isian. */
    public Map<Field, Integer> validate() {
        Map<Field, Integer> errors = new EnumMap<>(Field.class);
        if (isBlank(name)) {
            errors.put(Field.NAME, R.string.org_error_name_required);
        } else if (name.trim().length() > NAME_MAX) {
            errors.put(Field.NAME, R.string.org_error_name_too_long);
        }
        if (length(shortName) > SHORT_NAME_MAX) {
            errors.put(Field.SHORT_NAME, R.string.org_error_short_name_too_long);
        }
        if (isBlank(level)) {
            errors.put(Field.LEVEL, R.string.org_error_level_required);
        }
        if (isBlank(category)) {
            errors.put(Field.CATEGORY, R.string.org_error_category_required);
        }
        if (isBlank(institution)) {
            errors.put(Field.INSTITUTION, R.string.org_error_institution_required);
        } else if (institution.trim().length() > INSTITUTION_MAX) {
            errors.put(Field.INSTITUTION, R.string.org_error_institution_too_long);
        }
        if (length(city) > CITY_MAX) {
            errors.put(Field.CITY, R.string.org_error_city_too_long);
        }
        Integer start = year(periodStart);
        Integer end = year(periodEnd);
        if (!isBlank(periodStart) && start == null) {
            errors.put(Field.PERIOD_START, R.string.org_error_year_invalid);
        }
        if (!isBlank(periodEnd) && end == null) {
            errors.put(Field.PERIOD_END, R.string.org_error_year_invalid);
        }
        if (start != null && end != null && end < start) {
            errors.put(Field.PERIOD_END, R.string.org_error_period_order);
        }
        if (length(position) > POSITION_MAX) {
            errors.put(Field.POSITION, R.string.org_error_position_too_long);
        }
        return errors;
    }

    /** Tahun 4 angka dalam rentang yang diterima backend; null jika kosong atau tidak valid. */
    public static Integer year(String value) {
        if (isBlank(value) || !value.trim().matches("\\d{4}")) {
            return null;
        }
        int year = Integer.parseInt(value.trim());
        return year >= YEAR_MIN && year <= YEAR_MAX ? year : null;
    }

    public static String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static int length(String value) {
        return value == null ? 0 : value.trim().length();
    }
}
