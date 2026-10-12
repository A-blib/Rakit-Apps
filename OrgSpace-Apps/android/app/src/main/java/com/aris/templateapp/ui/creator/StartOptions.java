package com.aris.templateapp.ui.creator;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.ViewStartOptionBinding;

/** Isi dua kartu pilihan "Pakai template" / "Custom" (dipakai Beranda dan bottom sheet +). */
final class StartOptions {

    private StartOptions() {
    }

    static void bind(ViewStartOptionBinding template, ViewStartOptionBinding custom) {
        template.icon.setImageResource(R.drawable.ic_grid_view);
        template.title.setText(R.string.start_template_title);
        template.body.setText(R.string.start_template_body);
        custom.icon.setImageResource(R.drawable.ic_edit);
        custom.title.setText(R.string.start_custom_title);
        custom.body.setText(R.string.start_custom_body);
    }
}
