package com.aris.templateapp.ui.creator;

import androidx.fragment.app.Fragment;

import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;
import com.aris.templateapp.data.repository.GalleryRepository;
import com.aris.templateapp.ui.editor.EditorNav;

/**
 * Klik template (tab Template / "Template untuk anda"): catat "Dilihat" (keputusan Aris), lalu buka Editor
 * Template Mode. Versi awal tidak membuat project karena editornya masih "Segera hadir".
 */
final class TemplateOpener {

    private TemplateOpener() {
    }

    static void open(Fragment from, GalleryRepository repository, GalleryTemplateDto template) {
        repository.recordView(template.id);
        EditorNav.openTemplateEditor(from, template.name);
    }
}
