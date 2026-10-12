package com.aris.templateapp.ui.upload;

import androidx.annotation.StringRes;

import com.aris.templateapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Kolom Profil organisasi yang boleh dihubungkan ke isian, beserta jenis isian yang cocok (alur-fitur-upload.md
 * 15.3; aturan yang sama diperiksa ulang backend di MarkingValidator).
 */
public enum ProfileKeys {
    NAME("name", R.string.mark_profile_name, Set.of("text", "paragraph")),
    SHORT_NAME("short_name", R.string.mark_profile_short_name, Set.of("text", "paragraph")),
    INSTITUTION_NAME("institution_name", R.string.mark_profile_institution, Set.of("text", "paragraph")),
    ADDRESS("address", R.string.mark_profile_address, Set.of("text", "paragraph")),
    LOGO("logo", R.string.mark_profile_logo, Set.of("image")),
    EMAIL("email", R.string.mark_profile_email, Set.of("link")),
    WHATSAPP("whatsapp", R.string.mark_profile_whatsapp, Set.of("link", "button")),
    INSTAGRAM("instagram", R.string.mark_profile_instagram, Set.of("link", "button")),
    TIKTOK("tiktok", R.string.mark_profile_tiktok, Set.of("link", "button")),
    YOUTUBE("youtube", R.string.mark_profile_youtube, Set.of("link", "button"));

    public final String value;
    @StringRes
    public final int label;
    private final Set<String> types;

    ProfileKeys(String value, int label, Set<String> types) {
        this.value = value;
        this.label = label;
        this.types = types;
    }

    public boolean fits(String type) {
        return types.contains(type);
    }

    /** Kolom yang cocok untuk jenis isian {@code type}. */
    public static List<ProfileKeys> forType(String type) {
        List<ProfileKeys> result = new ArrayList<>();
        for (ProfileKeys key : values()) {
            if (key.fits(type)) {
                result.add(key);
            }
        }
        return result;
    }

    public static ProfileKeys fromValue(String value) {
        for (ProfileKeys key : values()) {
            if (key.value.equals(value)) {
                return key;
            }
        }
        return null;
    }
}
