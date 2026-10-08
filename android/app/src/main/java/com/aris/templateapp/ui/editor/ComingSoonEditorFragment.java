package com.aris.templateapp.ui.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentEditorComingSoonBinding;
import com.aris.templateapp.ui.common.LottieTint;

/**
 * Halaman editor "Segera hadir" (alur-pembuatan-website.md bagian 6.1). Kini hanya dipakai editor custom mode;
 * editor template mode sudah sungguhan ({@link TemplateEditorFragment}). Membuka halaman ini TIDAK membuat project.
 */
public abstract class ComingSoonEditorFragment extends Fragment {

    private FragmentEditorComingSoonBinding binding;

    @StringRes
    protected abstract int modeLabel();

    @StringRes
    protected abstract int titleText();

    @StringRes
    protected abstract int bodyText();

    @RawRes
    protected abstract int animation();

    /** Judul app bar jika tidak ada nama project/template. */
    @StringRes
    protected abstract int fallbackTitle();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditorComingSoonBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String title = getArguments() == null ? null : getArguments().getString(EditorNav.ARG_TITLE);
        binding.toolbar.setTitle(title == null ? getString(fallbackTitle()) : title);
        binding.toolbar.setNavigationOnClickListener(v -> close());
        binding.modeLabel.setText(modeLabel());
        binding.title.setText(titleText());
        binding.body.setText(bodyText());
        binding.illustration.setAnimation(animation());
        LottieTint.applyForeground(binding.illustration);
        binding.backButton.setOnClickListener(v -> close());
    }

    /** Kembali ke layar asal (Beranda, tab Project, atau galeri). */
    private void close() {
        NavHostFragment.findNavController(this).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /** Editor custom mode: dibuka dari pilihan "Custom" atau project bermode custom. */
    public static class CustomEditor extends ComingSoonEditorFragment {
        @Override
        protected int modeLabel() {
            return R.string.editor_label_custom;
        }

        @Override
        protected int titleText() {
            return R.string.editor_title_custom;
        }

        @Override
        protected int bodyText() {
            return R.string.editor_body_custom;
        }

        @Override
        protected int animation() {
            return R.raw.intro_build;
        }

        @Override
        protected int fallbackTitle() {
            return R.string.project_untitled;
        }
    }
}
