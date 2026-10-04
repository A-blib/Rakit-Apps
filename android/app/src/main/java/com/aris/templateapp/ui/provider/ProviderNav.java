package com.aris.templateapp.ui.provider;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;

/**
 * Pindah dari tab Dashboard Provider ke layar penuh (detail template, panduan, edit profil).
 * Tab adalah Fragment anak dari shell; {@code findNavController} mencari ke atas sampai NavHostFragment
 * utama, jadi layar tujuan terbuka di atas shell dan tombol kembali kembali ke tab yang sama.
 */
public final class ProviderNav {

    static final String ARG_TEMPLATE_ID = "templateId";
    static final String ARG_GUIDE = "guide";

    private ProviderNav() {
    }

    public static void openTemplate(Fragment from, String templateId) {
        Bundle args = new Bundle();
        args.putString(ARG_TEMPLATE_ID, templateId);
        NavHostFragment.findNavController(from).navigate(R.id.templateDetailFragment, args);
    }

    public static void openGuide(Fragment from, Guide guide) {
        Bundle args = new Bundle();
        args.putString(ARG_GUIDE, guide.name());
        NavHostFragment.findNavController(from).navigate(R.id.guideFragment, args);
    }

    public static void openEditProfile(Fragment from) {
        NavHostFragment.findNavController(from).navigate(R.id.providerProfileEditFragment);
    }
}
