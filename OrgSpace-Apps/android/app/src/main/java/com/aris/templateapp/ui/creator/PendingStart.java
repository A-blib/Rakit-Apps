package com.aris.templateapp.ui.creator;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

/**
 * Pilihan "Pakai template" / "Rakit sendiri" dari tamu yang harus masuk dulu (alur-pembuatan-website.md G1).
 * Disimpan di ViewModel milik Activity sehingga tetap ada melewati layar Masuk dan onboarding, lalu dijalankan
 * dashboard saat user sudah masuk dan punya ruang kerja.
 */
public class PendingStart extends ViewModel {

    public enum Option { TEMPLATE, ASSEMBLED }

    @Nullable private Option option;

    public void set(Option option) {
        this.option = option;
    }

    /** Diambil sekali; setelah itu kosong lagi. */
    @Nullable
    public Option take() {
        Option value = option;
        option = null;
        return value;
    }
}
