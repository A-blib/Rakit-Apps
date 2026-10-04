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
import com.aris.templateapp.data.remote.dto.TemplateListDto.StatusCountsDto;
import com.aris.templateapp.databinding.FragmentProviderTemplatesBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tab "Template Anda" (alur-provider.md bagian 6). Filter disimpan di ViewModel milik Activity. */
public class ProviderTemplatesFragment extends Fragment {

    /** Jeda setelah berhenti mengetik sebelum mencari, agar tidak mengirim request setiap huruf. */
    private static final long SEARCH_DELAY_MS = 400;
    /** Mulai memuat halaman berikutnya saat tersisa sebanyak ini kartu di bawah layar. */
    private static final int LOAD_MORE_THRESHOLD = 5;

    private static final String[] CATEGORIES = {ProviderTemplatesViewModel.ALL, "sekolah", "organisasi", "umkm",
            "instansi", "pribadi", "lainnya"};
    private static final String[] SORTS = {"updated", "downloads", "views", "name"};
    private static final int[] SORT_LABELS = {R.string.sort_updated, R.string.sort_downloads, R.string.sort_views,
            R.string.sort_name};

    private FragmentProviderTemplatesBinding binding;
    private ProviderTemplatesViewModel viewModel;
    private TemplateAdapter adapter;
    private LoadingFooterAdapter footer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable applySearch = () -> {
        if (binding != null) {
            viewModel.setQuery(String.valueOf(binding.searchInput.getText()));
        }
    };
    /** id chip → nilai filter status di backend. LinkedHashMap agar urutannya sama dengan chip di layar. */
    private final Map<Integer, String> statusByChip = new LinkedHashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderTemplatesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(ProviderTemplatesViewModel.class);

        adapter = new TemplateAdapter(item -> ProviderNav.openTemplate(this, item.id));
        footer = new LoadingFooterAdapter();
        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(new ConcatAdapter(adapter, footer));
        binding.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layout = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (dy > 0 && layout != null
                        && layout.findLastVisibleItemPosition() >= adapter.getItemCount() - LOAD_MORE_THRESHOLD) {
                    viewModel.loadMore();
                }
            }
        });

        setUpSearch();
        setUpStatusChips();
        binding.categoryButton.setOnClickListener(this::showCategoryMenu);
        binding.sortButton.setOnClickListener(this::showSortMenu);
        bindDropdownLabels();

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getLoadMoreFailed().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error), Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_retry, v -> viewModel.loadMore())
                        .show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Tampil pertama, kembali dari detail, atau app kembali dari latar belakang: ambil data terbaru.
        if (!isHidden()) {
            viewModel.refresh();
        }
    }

    /** Tab ini dibuka lagi lewat bottom navigation (Fragment hanya disembunyikan, bukan dibuat ulang). */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && binding != null) {
            syncControls();
            viewModel.refresh();
        }
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

    private void setUpStatusChips() {
        statusByChip.put(R.id.status_all, ProviderTemplatesViewModel.ALL);
        statusByChip.put(R.id.status_published, "published");
        statusByChip.put(R.id.status_needs_fix, ProviderTemplatesViewModel.STATUS_NEEDS_FIX);
        statusByChip.put(R.id.status_checking, "checking");
        statusByChip.put(R.id.status_draft, "draft");
        statusByChip.put(R.id.status_disabled, "disabled");
        bindChipLabels(null);
        syncControls();
        binding.statusGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                viewModel.setStatus(statusByChip.get(checkedIds.get(0)));
            }
        });
    }

    /** Menyamakan chip & dropdown dengan filter di ViewModel (mis. setelah "Lihat semua" dari Beranda). */
    private void syncControls() {
        for (Map.Entry<Integer, String> entry : statusByChip.entrySet()) {
            if (entry.getValue().equals(viewModel.getStatus())) {
                binding.statusGroup.check(entry.getKey());
            }
        }
        bindDropdownLabels();
    }

    private void bindChipLabels(@Nullable StatusCountsDto counts) {
        setChip(R.id.status_all, R.string.filter_all, counts == null ? null : counts.all);
        setChip(R.id.status_published, R.string.filter_published, counts == null ? null : counts.published);
        setChip(R.id.status_needs_fix, R.string.filter_needs_fix, counts == null ? null : counts.needsFix);
        setChip(R.id.status_checking, R.string.filter_checking, counts == null ? null : counts.checking);
        setChip(R.id.status_draft, R.string.filter_draft, counts == null ? null : counts.draft);
        setChip(R.id.status_disabled, R.string.filter_disabled, counts == null ? null : counts.disabled);
    }

    private void setChip(int chipId, @StringRes int label, @Nullable Long count) {
        Chip chip = binding.statusGroup.findViewById(chipId);
        chip.setText(count == null ? getString(label) : getString(R.string.chip_with_count, getString(label), count));
    }

    private void bindDropdownLabels() {
        String category = viewModel.getCategory();
        binding.categoryButton.setText(ProviderTemplatesViewModel.ALL.equals(category)
                ? getString(R.string.filter_category_all) : getString(TemplateUi.categoryLabel(category)));
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

    private void showCategoryMenu(View anchor) {
        hideKeyboard();
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        for (int i = 0; i < CATEGORIES.length; i++) {
            String value = CATEGORIES[i];
            popup.getMenu().add(Menu.NONE, i, i, ProviderTemplatesViewModel.ALL.equals(value)
                    ? getString(R.string.filter_category_all) : getString(TemplateUi.categoryLabel(value)));
        }
        popup.setOnMenuItemClickListener(item -> {
            viewModel.setCategory(CATEGORIES[item.getItemId()]);
            bindDropdownLabels();
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
            bindDropdownLabels();
            return true;
        });
        popup.show();
    }

    private void render(@Nullable TemplateListState state) {
        if (state == null) {
            return;
        }
        bindChipLabels(state.counts);
        footer.setLoading(state.loadingMore);
        boolean firstLoad = state.loadingFirstPage && state.items.isEmpty();
        binding.skeleton.setVisibility(firstLoad ? View.VISIBLE : View.GONE);

        if (state.error != null) {
            binding.list.setVisibility(View.GONE);
            binding.state.showError(ErrorMessages.forError(requireContext(), state.error), viewModel::refresh);
        } else if (!state.loadingFirstPage && state.items.isEmpty()) {
            binding.list.setVisibility(View.GONE);
            // Jumlah dari server ikut tersaring kategori & pencarian, jadi "belum punya template" hanya
            // bisa dipastikan saat tidak ada filter yang aktif.
            if (state.hasNoTemplates() && !viewModel.hasActiveFilter()) {
                binding.state.showEmpty(getString(R.string.templates_empty), getString(R.string.action_open_guide),
                        () -> ProviderNav.openGuide(this, Guide.PREPARE_TEMPLATE));
            } else {
                binding.state.showEmpty(getString(R.string.templates_filter_empty),
                        getString(R.string.action_clear_filter), this::clearFilters);
            }
        } else {
            binding.state.hide();
            binding.list.setVisibility(firstLoad ? View.GONE : View.VISIBLE);
            adapter.submitList(state.items);
        }
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
