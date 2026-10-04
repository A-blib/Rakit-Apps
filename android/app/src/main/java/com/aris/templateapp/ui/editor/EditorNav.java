package com.aris.templateapp.ui.editor;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectMode;

/**
 * Pintu masuk ke editor (alur-pembuatan-website.md bagian 2.1). Editor mana yang dibuka ditentukan kolom
 * {@code mode} project; template yang diklik selalu membuka editor template mode. Versi awal kedua editor masih
 * "Segera hadir", tetapi alur navigasi ini sudah final, jadi nanti cukup isi layarnya.
 */
public final class EditorNav {

    static final String ARG_TITLE = "title";

    private EditorNav() {
    }

    /** Klik kartu project (Beranda / tab Project): langsung ke editor sesuai mode, tanpa langkah tambahan. */
    public static void openProject(Fragment from, ProjectEntity project) {
        if (project.mode == ProjectMode.TEMPLATE) {
            openTemplateEditor(from, project.name);
        } else {
            openCustomEditor(from, project.name);
        }
    }

    /** @param title nama project atau nama template yang diklik */
    public static void openTemplateEditor(Fragment from, String title) {
        navigate(from, R.id.templateEditorFragment, title);
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
