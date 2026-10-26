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
    MARK_EDITABLE(Audience.PROVIDER, R.string.guide_mark_title, R.string.guide_mark_body),
    CHECK_RULES(Audience.PROVIDER, R.string.guide_rules_title, R.string.guide_rules_body),
    FIX_FAILED(Audience.PROVIDER, R.string.guide_fix_title, R.string.guide_fix_body),
    READ_NUMBERS(Audience.PROVIDER, R.string.guide_numbers_title, R.string.guide_numbers_body),

    // Dashboard Pembuat Website (alur-pembuatan-website.md bagian 3.4).
    PUBLISH(Audience.CREATOR, R.string.guide_publish_title, R.string.guide_publish_body),
    CHOOSE_MODE(Audience.CREATOR, R.string.guide_choose_mode_title, R.string.guide_choose_mode_body),
    INVITE(Audience.CREATOR, R.string.guide_invite_title, R.string.guide_invite_body),
    HANDOVER(Audience.CREATOR, R.string.guide_handover_title, R.string.guide_handover_body),
    DOMAIN(Audience.CREATOR, R.string.guide_domain_title, R.string.guide_domain_body),
    HOST_ZIP(Audience.CREATOR, R.string.guide_host_zip_title, R.string.guide_host_zip_body);

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
