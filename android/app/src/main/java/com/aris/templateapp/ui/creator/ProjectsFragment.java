package com.aris.templateapp.ui.creator;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.data.local.ProjectCounts;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectFilter;
import com.aris.templateapp.data.model.ProjectStatus;
import com.aris.templateapp.databinding.DialogRenameProjectBinding;
import com.aris.templateapp.databinding.FragmentProjectsBinding;
import com.aris.templateapp.ui.editor.EditorNav;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Tab Project (alur-pembuatan-website.md bagian 4). Semua data dari Room di HP, jadi pencarian langsung berjalan
 * setiap huruf diketik (tanpa jeda seperti galeri yang memanggil server) dan tidak ada keadaan offline.
 */
@AndroidEntryPoint
public class ProjectsFragment extends Fragment {

    private static final int NAME_MAX = 100;
    private static final ProjectFilter.Sort[] SORTS = {ProjectFilter.Sort.UPDATED, ProjectFilter.Sort.CREATED,
            ProjectFilter.Sort.NAME};
    private static final int[] SORT_LABELS = {R.string.sort_project_updated, R.string.sort_project_created,
            R.string.sort_name};

    private FragmentProjectsBinding binding;
    private ProjectsViewModel viewModel;
    private ProjectAdapter adapter;
    private List<ProjectEntity> lastProjects;
    private ProjectCounts lastAllCounts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProjectsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ProjectsViewModel.class);
        adapter = new ProjectAdapter(project -> EditorNav.openProject(this, project), this::showMenu);
        binding.list.setAdapter(adapter);
        // RecyclerView menjaga posisi scroll saat item baru disisipkan di atas, sehingga project hasil duplikat
        // (paling baru diedit → paling atas) tidak terlihat. Gulir ke atas agar perubahan langsung tampak.
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0 && binding != null) {
                    binding.list.scrollToPosition(0);
                }
            }
        });

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setQuery(s.toString());
            }
        });
        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });
        binding.statusGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (!ids.isEmpty()) {
                viewModel.setStatus(statusOf(ids.get(0)));
            }
        });
        binding.sortButton.setOnClickListener(this::showSortMenu);

        viewModel.getFilter().observe(getViewLifecycleOwner(), this::bindFilter);
        viewModel.getCounts().observe(getViewLifecycleOwner(), this::bindChipCounts);
        viewModel.getProjects().observe(getViewLifecycleOwner(), projects -> {
            lastProjects = projects;
            render();
        });
        viewModel.getAllCounts().observe(getViewLifecycleOwner(), counts -> {
            lastAllCounts = counts;
            render();
        });
    }

    private void bindFilter(ProjectFilter filter) {
        // Filter/urutan diganti: daftar baru dimulai dari atas.
        binding.list.scrollToPosition(0);
        binding.statusGroup.check(chipOf(filter.status));
        for (int i = 0; i < SORTS.length; i++) {
            if (SORTS[i] == filter.sort) {
                binding.sortButton.setText(SORT_LABELS[i]);
            }
        }
    }

    private void bindChipCounts(@Nullable ProjectCounts counts) {
        setChip(R.id.status_all, R.string.filter_all, counts == null ? null : counts.total);
        setChip(R.id.status_draft, R.string.filter_draft, counts == null ? null : counts.draft);
        setChip(R.id.status_ready, R.string.filter_project_ready, counts == null ? null : counts.ready);
        setChip(R.id.status_exported, R.string.filter_project_exported, counts == null ? null : counts.exported);
    }

    private void setChip(int chipId, int label, @Nullable Integer count) {
        Chip chip = binding.statusGroup.findViewById(chipId);
        chip.setText(count == null ? getString(label) : getString(R.string.chip_with_count, getString(label), count));
    }

    /** Kosong total → ajakan membuat website; kosong karena filter → "Hapus filter". */
    private void render() {
        if (lastProjects == null || lastAllCounts == null) {
            return;
        }
        if (!lastProjects.isEmpty()) {
            binding.state.hide();
            binding.list.setVisibility(View.VISIBLE);
            adapter.submitList(lastProjects);
            return;
        }
        binding.list.setVisibility(View.GONE);
        adapter.submitList(lastProjects);
        if (lastAllCounts.total == 0) {
            binding.state.showEmpty(getString(R.string.projects_empty), getString(R.string.action_create_website),
                    () -> ((CreatorDashboardFragment) requireParentFragment()).showCreateSheet());
        } else {
            binding.state.showEmpty(getString(R.string.projects_filter_empty), getString(R.string.action_clear_filter),
                    () -> {
                        hideKeyboard();
                        binding.searchInput.setText("");
                        viewModel.clearFilters();
                    });
        }
    }

    // ---- Menu ⋮ ---------------------------------------------------------------------------------

    private void showMenu(View anchor, ProjectEntity project) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        Menu menu = popup.getMenu();
        menu.add(Menu.NONE, R.string.project_rename, 0, R.string.project_rename);
        menu.add(Menu.NONE, R.string.project_duplicate, 1, R.string.project_duplicate);
        // Export hanya untuk project yang isiannya sudah lengkap (bagian 4.3).
        menu.add(Menu.NONE, R.string.project_export, 2, R.string.project_export)
                .setEnabled(project.status != ProjectStatus.DRAFT);
        menu.add(Menu.NONE, R.string.project_delete, 3, R.string.project_delete);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.string.project_rename) {
                showRenameDialog(project);
            } else if (id == R.string.project_duplicate) {
                viewModel.duplicate(project.id, getString(R.string.project_copy_name, project.name));
                Snackbar.make(binding.getRoot(), R.string.project_duplicated, Snackbar.LENGTH_SHORT).show();
            } else if (id == R.string.project_export) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.export_dialog_title)
                        .setMessage(R.string.export_dialog_body)
                        .setPositiveButton(R.string.action_ok, null)
                        .show();
            } else if (id == R.string.project_delete) {
                confirmDelete(project);
            }
            return true;
        });
        popup.show();
    }

    private void showRenameDialog(ProjectEntity project) {
        DialogRenameProjectBinding dialogBinding = DialogRenameProjectBinding.inflate(getLayoutInflater());
        dialogBinding.nameInput.setText(project.name);
        dialogBinding.nameInput.selectAll();
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.project_rename_title)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, null)
                .create();
        // Tombol Simpan dipasang sendiri agar dialog tidak tertutup saat nama tidak valid.
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = OnboardingUi.text(dialogBinding.nameInput).trim();
            if (name.isEmpty()) {
                dialogBinding.nameLayout.setError(getString(R.string.error_project_name_required));
            } else if (name.length() > NAME_MAX) {
                dialogBinding.nameLayout.setError(getString(R.string.error_name_too_long));
            } else {
                viewModel.rename(project.id, name);
                dialog.dismiss();
            }
        }));
        dialog.show();
    }

    private void confirmDelete(ProjectEntity project) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.project_delete_title)
                .setMessage(getString(R.string.project_delete_body, project.name))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.project_delete, (d, which) -> {
                    viewModel.delete(project.id);
                    Snackbar.make(binding.getRoot(), R.string.project_deleted, Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }

    // ---- Pembantu -------------------------------------------------------------------------------

    private void showSortMenu(View anchor) {
        hideKeyboard();
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        for (int i = 0; i < SORTS.length; i++) {
            popup.getMenu().add(Menu.NONE, i, i, SORT_LABELS[i]);
        }
        popup.setOnMenuItemClickListener(item -> {
            viewModel.setSort(SORTS[item.getItemId()]);
            return true;
        });
        popup.show();
    }

    @Nullable
    private static ProjectStatus statusOf(int chipId) {
        if (chipId == R.id.status_draft) return ProjectStatus.DRAFT;
        if (chipId == R.id.status_ready) return ProjectStatus.READY;
        if (chipId == R.id.status_exported) return ProjectStatus.EXPORTED;
        return null;
    }

    private static int chipOf(@Nullable ProjectStatus status) {
        if (status == ProjectStatus.DRAFT) return R.id.status_draft;
        if (status == ProjectStatus.READY) return R.id.status_ready;
        if (status == ProjectStatus.EXPORTED) return R.id.status_exported;
        return R.id.status_all;
    }

    private void hideKeyboard() {
        binding.searchInput.clearFocus();
        InputMethodManager imm = ContextCompat.getSystemService(requireContext(), InputMethodManager.class);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.searchInput.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
