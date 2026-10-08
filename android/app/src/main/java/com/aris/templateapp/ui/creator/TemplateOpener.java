package com.aris.templateapp.ui.creator;

import androidx.fragment.app.Fragment;

import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;
import com.aris.templateapp.ui.template.TemplateNav;

/**
 * Klik template (tab Template / "Template untuk anda") membuka layar Unduh (alur-buat-website-via-template.md
 * bagian 5). Layar itu mencatat "Dilihat" dan langsung membuka editor jika paketnya sudah ada di HP.
 */
final class TemplateOpener {

    private TemplateOpener() {
    }

    static void open(Fragment from, GalleryTemplateDto template) {
        TemplateNav.openDownload(from, template.id, template.name);
    }
}
