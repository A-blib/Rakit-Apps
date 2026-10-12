package com.aris.templateapp.ui.workspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentWorkspaceChoiceBinding;
import com.aris.templateapp.databinding.ItemChoiceCardBinding;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * "Mulai jejak digital organisasimu dari sini" (revisi-fondasi-orgspace.md bagian 5). Dibuka dari pilih peran,
 * dari dashboard saat user belum punya ruang kerja, dan dari pemilih ruang kerja ("Buat atau gabung organisasi").
 */
@AndroidEntryPoint
public class WorkspaceChoiceFragment extends Fragment {

    private FragmentWorkspaceChoiceBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkspaceChoiceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        NavController nav = NavHostFragment.findNavController(this);
        // Tanpa layar sebelumnya (dibuka dari dashboard yang kosong), tombol kembali disembunyikan. Ikon dihapus
        // lewat null: setNavigationIcon(0) membuat Toolbar mencari resource #0 lalu crash.
        if (nav.getPreviousBackStackEntry() == null) {
            binding.toolbar.setNavigationIcon(null);
        } else {
            binding.toolbar.setNavigationOnClickListener(v -> nav.navigateUp());
        }

        bindCard(binding.createCard, R.drawable.ic_groups, R.string.workspace_choice_create_title,
                R.string.workspace_choice_create_body, R.id.action_workspace_choice_to_organization_form);
        bindCard(binding.joinCard, R.drawable.ic_group_add, R.string.workspace_choice_join_title,
                R.string.workspace_choice_join_body, R.id.action_workspace_choice_to_join);
        bindCard(binding.personalCard, R.drawable.ic_person, R.string.workspace_choice_personal_title,
                R.string.workspace_choice_personal_body, R.id.action_workspace_choice_to_personal);
    }

    private void bindCard(ItemChoiceCardBinding card, @DrawableRes int icon, @StringRes int title, @StringRes int body,
                          @IdRes int action) {
        card.icon.setImageResource(icon);
        card.title.setText(title);
        card.body.setText(body);
        card.card.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(action));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
