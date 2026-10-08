package com.aris.templateapp.ui.editor;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectMode;

/**
 * Pintu masuk ke editor (alur-pembuatan-website.md bagian 2.1). Editor mana yang dibuka ditentukan kolom
 * {@code mode} project; template yang diklik selalu membuka editor template mode. Editor custom mode masih
 * "Segera hadir"; editor template mode sudah sungguhan (alur-buat-website-via-template.md).
 */
public final class EditorNav {

    static final String ARG_TITLE = "title";
    /** Project yang dibuka; null = project baru dari paket template (belum tersimpan sampai ada perubahan). */
    static final String ARG_PROJECT_ID = "projectId";
    static final String ARG_TEMPLATE_ID = "templateId";
    static final String ARG_TEMPLATE_VERSION = "templateVersion";

    private EditorNav() {
    }

    /** Klik kartu project (Beranda / tab Project): langsung ke editor sesuai mode, tanpa langkah tambahan. */
    public static void openProject(Fragment from, ProjectEntity project) {
        if (project.mode == ProjectMode.TEMPLATE) {
            Bundle args = new Bundle();
            args.putString(ARG_TITLE, project.name);
            args.putString(ARG_PROJECT_ID, project.id);
            NavHostFragment.findNavController(from).navigate(R.id.templateEditorFragment, args);
        } else {
            openCustomEditor(from, project.name);
        }
    }

    /**
     * Paket template siap (alur-buat-website-via-template.md bagian 5): buka editor dengan project baru yang belum
     * tersimpan. Layar Unduh dikeluarkan dari tumpukan, jadi tombol kembali di editor langsung ke galeri.
     */
    public static void openNewFromPackage(Fragment from, String templateId, int version, String templateName) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, templateName);
        args.putString(ARG_TEMPLATE_ID, templateId);
        args.putInt(ARG_TEMPLATE_VERSION, version);
        NavHostFragment.findNavController(from).navigate(R.id.templateEditorFragment, args,
                new NavOptions.Builder().setPopUpTo(R.id.templateDownloadFragment, true).build());
    }

    /** @param title nama project; null untuk project custom baru ("Project tanpa nama") */
    public static void openCustomEditor(Fragment from, @Nullable String title) {
        navigate(from, R.id.customEditorFragment, title);
    }

    private static void navigate(Fragment from, int destination, @Nullable String title) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        NavHostFragment.findNavController(from).navigate(destination, args);
    }
}
