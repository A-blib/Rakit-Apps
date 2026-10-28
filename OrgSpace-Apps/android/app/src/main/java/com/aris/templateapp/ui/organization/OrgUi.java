package com.aris.templateapp.ui.organization;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.databinding.DialogTextInputBinding;
import com.aris.templateapp.databinding.ViewOrgLogoBinding;
import com.aris.templateapp.ui.common.AppBarAccount;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Consumer;

/** Pembantu tampilan bersama layar kelola organisasi. */
public final class OrgUi {

    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");

    private OrgUi() {
    }

    /** Logo jika ada; jika belum, huruf depan nama di kotak biru muda. */
    static void bindLogo(ViewOrgLogoBinding logo, ThumbnailLoader loader, String name, @Nullable String url) {
        logo.initial.setText(AppBarAccount.initialOf(name));
        logo.initial.setVisibility(View.VISIBLE);
        loader.load(url, logo.image, () -> logo.initial.setVisibility(View.INVISIBLE));
    }

    /** "12 Okt 2026" dalam zona waktu HP. */
    static String date(@Nullable Instant instant) {
        if (instant == null) {
            return "";
        }
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(INDONESIA)
                .withZone(ZoneId.systemDefault()).format(instant);
    }

    /** "28 Okt 2026 14.30" dalam zona waktu HP, untuk kode undangan yang hanya berlaku 1 jam. */
    static String dateTime(@Nullable Instant instant) {
        if (instant == null) {
            return "";
        }
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(INDONESIA)
                .withZone(ZoneId.systemDefault()).format(instant);
    }

    /** Dialog berisi satu isian teks; {@code onConfirm} menerima teks yang diketik. */
    public static void textDialog(Context context, String title, @Nullable String message, @StringRes int hint,
                           @Nullable String initial, @StringRes int confirm, Consumer<String> onConfirm) {
        DialogTextInputBinding input = DialogTextInputBinding.inflate(LayoutInflater.from(context));
        input.inputLayout.setHint(context.getString(hint));
        if (initial != null) {
            input.input.setText(initial);
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(input.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(confirm, (dialog, which) ->
                        onConfirm.accept(input.input.getText() == null ? "" : input.input.getText().toString()))
                .show();
    }

    /** Kembali ke dashboard pembuat website (mis. setelah keluar atau menghapus organisasi). */
    static void backToDashboard(Fragment fragment) {
        NavController nav = NavHostFragment.findNavController(fragment);
        if (!nav.popBackStack(R.id.creatorDashboardFragment, false)) {
            nav.navigate(R.id.creatorDashboardFragment);
        }
    }
}
