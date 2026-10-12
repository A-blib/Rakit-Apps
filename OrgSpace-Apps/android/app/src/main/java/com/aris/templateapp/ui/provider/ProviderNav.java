package com.aris.templateapp.ui.provider;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.ui.guide.Guide;
import com.aris.templateapp.ui.guide.GuideFragment;

/**
 * Pindah dari tab Dashboard Provider ke layar penuh (detail template, panduan, edit profil).
 * Tab adalah Fragment anak dari shell; {@code findNavController} mencari ke atas sampai NavHostFragment
 * utama, jadi layar tujuan terbuka di atas shell dan tombol kembali kembali ke tab yang sama.
 */
public final class ProviderNav {

    static final String ARG_TEMPLATE_ID = "templateId";

    private ProviderNav() {
    }

    public static void openTemplate(Fragment from, String templateId) {
        Bundle args = new Bundle();
        args.putString(ARG_TEMPLATE_ID, templateId);
        NavHostFragment.findNavController(from).navigate(R.id.templateDetailFragment, args);
    }

    public static void openGuide(Fragment from, Guide guide) {
        GuideFragment.open(from, guide);
    }

    public static void openEditProfile(Fragment from) {
        NavHostFragment.findNavController(from).navigate(R.id.providerProfileEditFragment);
    }
}
