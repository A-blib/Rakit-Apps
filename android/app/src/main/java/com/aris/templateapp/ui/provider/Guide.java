package com.aris.templateapp.ui.provider;

import androidx.annotation.StringRes;

import com.aris.templateapp.R;

/**
 * Daftar panduan provider (alur-provider.md bagian 3.9). Isi disimpan di strings.xml (versi awal).
 * Panduan yang bergantung pada aturan Upload belum ditulis ({@code available = false}) dan tampil "Segera hadir".
 */
public enum Guide {
    PREPARE_TEMPLATE(R.string.guide_prepare_title, R.string.guide_prepare_body, true),
    MARK_EDITABLE(R.string.guide_mark_title, 0, false),
    CHECK_RULES(R.string.guide_rules_title, 0, false),
    FIX_FAILED(R.string.guide_fix_title, 0, false),
    READ_NUMBERS(R.string.guide_numbers_title, R.string.guide_numbers_body, true);

    @StringRes
    public final int title;
    @StringRes
    public final int body;
    public final boolean available;

    Guide(int title, int body, boolean available) {
        this.title = title;
        this.body = body;
        this.available = available;
    }
}
