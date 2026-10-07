package com.aris.templateapp.ui.upload;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;

/**
 * Perpindahan layar wizard Upload (alur-fitur-upload.md bagian 3). Semua langkah adalah layar penuh di atas Dashboard
 * Provider, sehingga bottom navigation tersembunyi selama wizard (bagian 3.2).
 */
public final class UploadNav {

    static final String ARG_URI = "uri";
    static final String ARG_FILE_NAME = "fileName";
    static final String ARG_FILE_SIZE = "fileSize";
    static final String ARG_TEMPLATE_ID = "templateId";
    static final String ARG_CODE = "code";

    private UploadNav() {
    }

    /** Langkah 1 → 2: file sudah dipilih; cek kilat, upload, lalu pengecekan. */
    public static void startUpload(Fragment from, Uri uri, String fileName, long size, @Nullable String fixTemplateId) {
        Bundle args = new Bundle();
        args.putString(ARG_URI, uri.toString());
        args.putString(ARG_FILE_NAME, fileName);
        args.putLong(ARG_FILE_SIZE, size);
        args.putString(ARG_TEMPLATE_ID, fixTemplateId);
        NavController nav = NavHostFragment.findNavController(from);
        // Upload perbaikan dari layar hasil pengecekan menggantikan layar itu, bukan menumpuk.
        NavOptions options = nav.getCurrentDestination() != null
                && nav.getCurrentDestination().getId() == R.id.uploadCheckFragment
                ? new NavOptions.Builder().setPopUpTo(R.id.uploadCheckFragment, true).build() : null;
        nav.navigate(R.id.uploadCheckFragment, args, options);
    }

    /** Membuka hasil/progres pengecekan upload yang sudah ada (kartu "Perlu diperbaiki" atau "Sedang dicek"). */
    public static void openCheck(Fragment from, String templateId) {
        Bundle args = new Bundle();
        args.putString(ARG_TEMPLATE_ID, templateId);
        NavHostFragment.findNavController(from).navigate(R.id.uploadCheckFragment, args);
    }

    /** "Lanjutkan draft": tepat di langkah terakhir (bagian 3.1). */
    public static void resumeDraft(Fragment from, String templateId, int step) {
        Bundle args = new Bundle();
        args.putString(ARG_TEMPLATE_ID, templateId);
        int destination = step >= 4 ? R.id.uploadMarkFragment : R.id.uploadInfoFragment;
        NavHostFragment.findNavController(from).navigate(destination, args);
    }

    /** Pindah ke langkah berikutnya/sebelumnya; layar sekarang diganti agar tombol kembali tidak menumpuk langkah. */
    static void replaceStep(Fragment from, int destination, String templateId) {
        Bundle args = new Bundle();
        args.putString(ARG_TEMPLATE_ID, templateId);
        NavController nav = NavHostFragment.findNavController(from);
        int current = nav.getCurrentDestination() == null ? 0 : nav.getCurrentDestination().getId();
        nav.navigate(destination, args, new NavOptions.Builder().setPopUpTo(current, true).build());
    }

    public static void openHelp(Fragment from, String code) {
        Bundle args = new Bundle();
        args.putString(ARG_CODE, code);
        NavHostFragment.findNavController(from).navigate(R.id.helpArticleFragment, args);
    }

    /** Keluar wizard, kembali ke Dashboard Provider (tab yang tadi dibuka). */
    static void exit(Fragment from) {
        NavController nav = NavHostFragment.findNavController(from);
        if (!nav.popBackStack(R.id.providerDashboardFragment, false)) {
            nav.navigateUp();
        }
    }
}
