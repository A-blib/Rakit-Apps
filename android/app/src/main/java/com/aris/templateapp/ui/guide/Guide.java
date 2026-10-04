package com.aris.templateapp.ui.guide;

import androidx.annotation.StringRes;

import com.aris.templateapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Panduan singkat yang disimpan di app (teks di strings.xml), sehingga bisa dibaca offline.
 * Panduan yang isinya bergantung pada fitur yang belum dirancang ({@code available = false}) tampil "Segera hadir".
 */
public enum Guide {
    // Dashboard Provider (alur-provider.md bagian 3.9)
    PREPARE_TEMPLATE(Audience.PROVIDER, R.string.guide_prepare_title, R.string.guide_prepare_body),
    MARK_EDITABLE(Audience.PROVIDER, R.string.guide_mark_title, 0),
    CHECK_RULES(Audience.PROVIDER, R.string.guide_rules_title, 0),
    FIX_FAILED(Audience.PROVIDER, R.string.guide_fix_title, 0),
    READ_NUMBERS(Audience.PROVIDER, R.string.guide_numbers_title, R.string.guide_numbers_body),

    // Dashboard Pembuat Website (alur-pembuatan-website.md bagian 3.4)
    HOST_ZIP(Audience.CREATOR, R.string.guide_host_zip_title, R.string.guide_host_zip_body),
    MODES(Audience.CREATOR, R.string.guide_modes_title, R.string.guide_modes_body),
    FIRST_WEBSITE(Audience.CREATOR, R.string.guide_first_website_title, 0);

    /** Dashboard tempat panduan ditampilkan. */
    public enum Audience { PROVIDER, CREATOR }

    public final Audience audience;
    @StringRes
    public final int title;
    /** 0 = isi belum ditulis (segera hadir). */
    @StringRes
    public final int body;
    public final boolean available;

    Guide(Audience audience, int title, int body) {
        this.audience = audience;
        this.title = title;
        this.body = body;
        this.available = body != 0;
    }

    public static List<Guide> forAudience(Audience audience) {
        List<Guide> guides = new ArrayList<>();
        for (Guide guide : values()) {
            if (guide.audience == audience) {
                guides.add(guide);
            }
        }
        return guides;
    }
}
