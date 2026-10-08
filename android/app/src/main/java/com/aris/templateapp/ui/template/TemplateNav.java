package com.aris.templateapp.ui.template;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;

/** Pintu masuk ke layar Unduh paket template (alur-buat-website-via-template.md bagian 5). */
public final class TemplateNav {

    static final String ARG_TEMPLATE_ID = "templateId";
    static final String ARG_NAME = "name";

    private TemplateNav() {
    }

    public static void openDownload(Fragment from, String templateId, String name) {
        Bundle args = new Bundle();
        args.putString(ARG_TEMPLATE_ID, templateId);
        args.putString(ARG_NAME, name);
        NavHostFragment.findNavController(from).navigate(R.id.templateDownloadFragment, args);
    }
}
