package com.aris.templateapp.ui.provider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.CategoryLabels;
import com.aris.templateapp.data.model.OrgCategories;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto.TemplateSummaryDto;
import com.aris.templateapp.databinding.FragmentProviderTemplatesBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.guide.Guide;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Tab "Template Anda" (alur-provider.md bagian 6) bergaya Google Drive: folder di atas daftar, tombol + di kanan
 * bawah untuk "Folder baru" dan pilihan status, menu ⋮ di kartu untuk memindah template ke folder.
 * Filter & folder yang dibuka disimpan di ViewModel milik Activity.
 */
@AndroidEntryPoint
public class ProviderTemplatesFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;

    /** Jeda setelah berhenti mengetik sebelum mencari, agar tidak mengirim request setiap huruf. */
    private static final long SEARCH_DELAY_MS = 400;
    /** Mulai memuat halaman berikutnya saat tersisa sebanyak ini baris di bawah layar. */
    private static final int LOAD_MORE_THRESHOLD = 5;

    private static final String[] SORTS = {"updated", "downloads", "views", "name"};
    private static final int[] SORT_LABELS = {R.string.sort_updated, R.string.sort_downloads, R.string.sort_views,
            R.string.sort_name};

    private static final String[] STATUSES = {ProviderTemplatesViewModel.STATUS_PUBLISHED,
            ProviderTemplatesViewModel.STATUS_NEEDS_FIX, "checking", "draft", "disabled"};
    private static final int[] STATUS_LABELS = {R.string.filter_published, R.string.filter_needs_fix,
            R.string.filter_checking, R.string.filter_draft, R.string.filter_disabled};

    private static final int MENU_RENAME = 1;
    private static final int MENU_DELETE = 2;
    private static final int MENU_MOVE = 3;
    private static final int MENU_MOVE_OUT = 4;

    private FragmentProviderTemplatesBinding binding;
    private ProviderTemplatesViewModel viewModel;
    private SectionHeaderAdapter folderHeader;
    private FolderAdapter folderAdapter;
    private SectionHeaderAdapter templateHeader;
    private TemplateAdapter adapter;
    private LoadingFooterAdapter footer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable applySearch = () -> {
        if (binding != null) {
            viewModel.setQuery(String.valueOf(binding.searchInput.getText()));
        }
    };

    /**
     * Tombol Back HP saat folder terbuka: kembali ke halaman utama Template Anda dulu (seperti Drive).
     * Hanya aktif saat tab ini yang tampil; tab lain di ViewPager2 tetap hidup (STARTED) di belakang layar.
     */
    private final OnBackPressedCallback closeFolderOnBack = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            viewModel.closeFolder();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderTemplatesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(ProviderTemplatesViewModel.class);

        folderHeader = new SectionHeaderAdapter(R.string.templates_section_folders);
        folderAdapter = new FolderAdapter(viewModel::openFolder, this::showFolderMenu);
        templateHeader = new SectionHeaderAdapter(R.string.templates_section_templates);
        adapter = new TemplateAdapter(thumbnailLoader, item -> ProviderNav.openTemplate(this, item.id),
                this::showTemplateMenu);
        footer = new LoadingFooterAdapter();
        ConcatAdapter concat = new ConcatAdapter(folderHeader, folderAdapter, templateHeader, adapter, footer);
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(concat);
        binding.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layout = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (dy > 0 && layout != null
                        && layout.findLastVisibleItemPosition() >= concat.getItemCount() - LOAD_MORE_THRESHOLD) {
                    viewModel.loadMore();
                }
            }
        });

        setUpSearch();
        binding.fab.setOnClickListener(v -> showMenuSheet());
        binding.statusChip.setOnClickListener(v -> showMenuSheet());
        binding.statusChip.setCloseIconContentDescription(getString(R.string.cd_status_chip_clear));
        binding.statusChip.setOnCloseIconClickListener(v -> {
            viewModel.setStatus(ProviderTemplatesViewModel.ALL);
            syncControls();
        });
        binding.categoryButton.setOnClickListener(this::showCategoryMenu);
        binding.sortButton.setOnClickListener(this::showSortMenu);
        binding.folderBack.setOnClickListener(v -> viewModel.closeFolder());
        binding.folderMenu.setOnClickListener(v -> {
            TemplateFolderDto folder = viewModel.getFolder();
            if (folder != null) {
                showFolderMenu(folder, v);
            }
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), closeFolderOnBack);
        syncControls();

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getLoadMoreFailed().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error), Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_retry, v -> viewModel.loadMore())
                        .show();
            }
        });
        viewModel.getActionFailed().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error), Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        viewModel.getNotice().observe(getViewLifecycleOwner(), event -> {
            ProviderTemplatesViewModel.Notice notice = event.getContentIfNotHandled();
            if (notice != null) {
                String text = notice.arg == null ? getString(notice.message) : getString(notice.message, notice.arg);
                Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Tab ini tampil (pertama kali, dibuka lagi lewat bottom navigation/geser, kembali dari detail, atau app kembali
     * dari latar belakang): samakan kontrol filter (mis. setelah "Lihat semua" dari Beranda) lalu ambil data terbaru.
     */
    @Override
    public void onResume() {
        super.onResume();
        syncControls();
        viewModel.refresh();
        closeFolderOnBack.setEnabled(viewModel.getFolder() != null);
    }

    @Override
    public void onPause() {
        super.onPause();
        closeFolderOnBack.setEnabled(false);
    }

    private void setUpSearch() {
        binding.searchInput.setText(viewModel.getQuery());
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(applySearch);
                handler.postDelayed(applySearch, SEARCH_DELAY_MS);
            }
        });
        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handler.removeCallbacks(applySearch);
                applySearch.run();
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    /** Menyamakan chip status & dropdown dengan filter di ViewModel (mis. setelah "Lihat semua" dari Beranda). */
    private void syncControls() {
        String status = viewModel.getStatus();
        int label = 0;
        for (int i = 0; i < STATUSES.length; i++) {
            if (STATUSES[i].equals(status)) {
                label = STATUS_LABELS[i];
            }
        }
        binding.statusChip.setVisibility(label == 0 ? View.GONE : View.VISIBLE);
        if (label != 0) {
            binding.statusChip.setText(label);
        }

        String category = viewModel.getCategory();
        binding.categoryButton.setText(ProviderTemplatesViewModel.ALL.equals(category)
                ? getString(R.string.filter_category_all) : TemplateUi.categoryLabel(category));
        for (int i = 0; i < SORTS.length; i++) {
            if (SORTS[i].equals(viewModel.getSort())) {
                binding.sortButton.setText(SORT_LABELS[i]);
            }
        }
    }

    /** Keyboard ditutup saat user beralih ke filter lain, supaya hasilnya langsung terlihat. */
    private void hideKeyboard() {
        binding.searchInput.clearFocus();
        InputMethodManager imm = ContextCompat.getSystemService(requireContext(), InputMethodManager.class);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.searchInput.getWindowToken(), 0);
        }
    }

    private void showMenuSheet() {
        hideKeyboard();
        if (getChildFragmentManager().findFragmentByTag(TemplatesMenuSheet.TAG) == null) {
            new TemplatesMenuSheet().show(getChildFragmentManager(), TemplatesMenuSheet.TAG);
        }
    }

    // ---------- folder ----------

    /** @param moveTemplateId template yang langsung dipindah ke folder baru; null = hanya membuat folder */
    void showCreateFolderDialog(@Nullable String moveTemplateId) {
        FolderDialogs.name(requireContext(), R.string.folder_new, R.string.folder_create, null,
                name -> viewModel.createFolder(name, moveTemplateId));
    }

    private void showFolderMenu(TemplateFolderDto folder, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(Menu.NONE, MENU_RENAME, 0, R.string.folder_rename);
        popup.getMenu().add(Menu.NONE, MENU_DELETE, 1, R.string.folder_delete);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == MENU_RENAME) {
                FolderDialogs.name(requireContext(), R.string.folder_rename, R.string.action_save, folder.name,
                        name -> viewModel.renameFolder(folder, name));
            } else {
                FolderDialogs.confirmDelete(requireContext(), folder, () -> viewModel.deleteFolder(folder));
            }
            return true;
        });
        popup.show();
    }

    private void showTemplateMenu(TemplateSummaryDto template, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(Menu.NONE, MENU_MOVE, 0, R.string.folder_move);
        if (template.folderId != null) {
            popup.getMenu().add(Menu.NONE, MENU_MOVE_OUT, 1, R.string.folder_move_out);
        }
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == MENU_MOVE_OUT) {
                viewModel.moveTemplate(template.id, null);
            } else {
                FolderDialogs.pickFolder(requireContext(), viewModel.getFolders(), template.folderId,
                        target -> viewModel.moveTemplate(template.id, target),
                        () -> showCreateFolderDialog(template.id));
            }
            return true;
        });
        popup.show();
    }

    // ---------- dropdown ----------

    private void showCategoryMenu(View anchor) {
        hideKeyboard();
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        // Daftar kategori dari server (salinan terakhir di HP); "Semua kategori" selalu paling atas.
        List<String> values = new ArrayList<>();
        values.add(ProviderTemplatesViewModel.ALL);
        OrgCategories categories = CategoryLabels.current();
        if (categories != null) {
            for (OrgCategories.Category category : categories.getCategories()) {
                values.add(category.value);
            }
        }
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            popup.getMenu().add(Menu.NONE, i, i, ProviderTemplatesViewModel.ALL.equals(value)
                    ? getString(R.string.filter_category_all) : TemplateUi.categoryLabel(value));
        }
        popup.setOnMenuItemClickListener(item -> {
            viewModel.setCategory(values.get(item.getItemId()));
            syncControls();
            return true;
        });
        popup.show();
    }

    private void showSortMenu(View anchor) {
        hideKeyboard();
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        for (int i = 0; i < SORTS.length; i++) {
            popup.getMenu().add(Menu.NONE, i, i, SORT_LABELS[i]);
        }
        popup.setOnMenuItemClickListener(item -> {
            viewModel.setSort(SORTS[item.getItemId()]);
            syncControls();
            return true;
        });
        popup.show();
    }

    // ---------- tampilan ----------

    private void render(@Nullable TemplateListState state) {
        if (state == null) {
            return;
        }
        TemplateFolderDto folder = state.currentFolder;
        binding.folderHeader.setVisibility(folder == null ? View.GONE : View.VISIBLE);
        if (folder != null) {
            binding.folderTitle.setText(folder.name);
        }
        closeFolderOnBack.setEnabled(folder != null && isResumed());
        // Status bisa berubah dari sheet tombol +, jadi chip status ikut disamakan setiap ada data baru.
        syncControls();

        footer.setLoading(state.loadingMore);
        boolean firstLoad = state.loadingFirstPage && state.isEmpty();
        binding.skeleton.setVisibility(firstLoad ? View.VISIBLE : View.GONE);

        if (state.error != null) {
            binding.list.setVisibility(View.GONE);
            binding.state.showError(ErrorMessages.forError(requireContext(), state.error), viewModel::refresh);
        } else if (!state.loadingFirstPage && state.isEmpty()) {
            binding.list.setVisibility(View.GONE);
            showEmpty(state);
        } else {
            binding.state.hide();
            binding.list.setVisibility(firstLoad ? View.GONE : View.VISIBLE);
            boolean hasFolders = !state.folders.isEmpty();
            folderHeader.setVisible(hasFolders);
            folderAdapter.submitList(state.folders);
            // Judul "Template" hanya perlu saat ada folder di atasnya, sebagai pemisah dua kelompok.
            templateHeader.setVisible(hasFolders && !state.items.isEmpty());
            adapter.submitList(state.items);
        }
    }

    private void showEmpty(TemplateListState state) {
        if (state.currentFolder != null && !viewModel.hasActiveFilter()) {
            binding.state.showEmpty(getString(R.string.folder_empty));
        } else if (state.currentFolder != null) {
            showFilterEmpty(R.string.folder_filter_empty);
        } else if (state.hasNoTemplates() && !viewModel.hasActiveFilter()) {
            // Jumlah dari server ikut tersaring kategori & pencarian, jadi "belum punya template" hanya
            // bisa dipastikan saat tidak ada filter yang aktif.
            binding.state.showEmpty(getString(R.string.templates_empty), getString(R.string.action_open_guide),
                    () -> ProviderNav.openGuide(this, Guide.PREPARE_TEMPLATE));
        } else {
            showFilterEmpty(R.string.templates_filter_empty);
        }
    }

    private void showFilterEmpty(@StringRes int message) {
        binding.state.showEmpty(getString(message), getString(R.string.action_clear_filter), this::clearFilters);
    }

    private void clearFilters() {
        hideKeyboard();
        handler.removeCallbacks(applySearch);
        viewModel.clearFilters();
        binding.searchInput.setText("");
        handler.removeCallbacks(applySearch);
        syncControls();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(applySearch);
        binding = null;
    }
}
