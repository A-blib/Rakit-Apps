package com.aris.templateapp.ui.profile;

import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.ItemProfileRowBinding;

/**
 * Mengisi satu baris menu kartu profil ({@code item_profile_row.xml}). Satu layout dipakai ulang untuk semua baris,
 * jadi tampilan menu di Profil pembuat website dan Profil kreator selalu sama.
 */
public final class ProfileRow {

    private ProfileRow() {
    }

    public static void bind(ItemProfileRowBinding row, @DrawableRes int icon, @StringRes int title,
                            @Nullable @StringRes Integer subtitle) {
        row.icon.setImageResource(icon);
        row.title.setText(title);
        if (subtitle == null) {
            row.subtitle.setVisibility(View.GONE);
        } else {
            row.subtitle.setText(subtitle);
            row.subtitle.setVisibility(View.VISIBLE);
        }
    }

    /** Aksi berisiko (Keluar): ikon & judul merah, tanpa panah karena tidak membuka layar baru. */
    public static void danger(ItemProfileRowBinding row) {
        int error = ContextCompat.getColor(row.getRoot().getContext(), R.color.color_error);
        row.icon.setBackgroundResource(R.drawable.bg_profile_row_icon_danger);
        row.icon.setColorFilter(error);
        row.title.setTextColor(error);
        row.chevron.setVisibility(View.GONE);
    }
}
