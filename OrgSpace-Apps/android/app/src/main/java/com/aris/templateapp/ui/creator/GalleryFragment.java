package com.aris.templateapp.ui.creator;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.OrgCategories;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.repository.GalleryRepository;
import com.aris.templateapp.databinding.FragmentGalleryBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.provider.LoadingFooterAdapter;
import com.aris.templateapp.ui.workspace.WorkspaceViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Tab Template: galeri versi awal (alur-pembuatan-website.md bagian 6.2). Butuh internet. */
@AndroidEntryPoint
public class GalleryFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;


    private static final long SEARCH_DELAY_MS = 400;
    private static final int LOAD_MORE_THRESHOLD = 5;

    private static final String[] SORTS = {GalleryRepository.SORT_POPULAR, GalleryRepository.SORT_NEWEST};
    private static final int[] SORT_LABELS = {R.string.sort_popular, R.string.sort_newest};

    private FragmentGalleryBinding binding;
    private GalleryViewModel viewModel;
    private GalleryAdapter adapter;
    @Nullable private Workspace workspace;
    // Chip dipilih dari kode (mis. syncControls) tidak boleh dianggap pilihan user.
    private boolean bindingChips;
    private LoadingFooterAdapter footer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable applySearch = () -> {
        if (binding != null) {
            viewModel.setQuery(String.valueOf(binding.searchInput.getText()));
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(GalleryViewModel.class);

        adapter = GalleryAdapter.list(thumbnailLoader, template -> TemplateOpener.open(this, template));
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
        syncControls();
        binding.categoryAll.setTag(GalleryViewModel.ALL);
        binding.categoryGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (!bindingChips && !ids.isEmpty()) {
                Object value = group.findViewById(ids.get(0)).getTag();
                viewModel.setCategory(value == null ? GalleryViewModel.ALL : value.toString());
            }
        });
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> buildCategoryChips());
        new ViewModelProvider(this).get(WorkspaceViewModel.class).getActive().observe(getViewLifecycleOwner(), active -> {
            workspace = active;
            buildCategoryChips();
        });
        binding.sortButton.setOnClickListener(this::showSortMenu);

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        // Tab ini dibuat di belakang layar sebelum dibuka; ambil daftar sekarang agar saat dibuka langsung tampil,
        // bukan skeleton sekejap lalu daftar (terlihat berkedip).
        if (viewModel.getState().getValue() == null) {
            viewModel.refresh();
        }
        viewModel.getLoadMoreFailed().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error), Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_retry, v -> viewModel.loadMore())
                        .show();
            }
        });
    }

    /** Tab tampil (pertama kali, digeser/dipilih lagi, kembali dari editor): samakan kontrol lalu ambil data terbaru. */
    @Override
    public void onResume() {
        super.onResume();
        syncControls();
        viewModel.refresh();
    }

    /**
     * Chip "Semua" + kategori sesuai jenjang ruang kerja aktif (alur-pembuatan-website.md 6): SMA tidak melihat
     * BEM, kampus tidak melihat OSIS. Tamu dan ruang pribadi melihat semua kategori.
     */
    private void buildCategoryChips() {
        OrgCategories categories = viewModel.getCategories().getValue();
        if (binding == null || categories == null) {
            return;
        }
        String level = workspace == null || workspace.isPersonal() ? null : workspace.getLevel();
        List<OrgCategories.Category> shown = level == null ? categories.getCategories()
                : categories.categoriesFor(level);
        bindingChips = true;
        binding.categoryGroup.removeViews(1, binding.categoryGroup.getChildCount() - 1);
        boolean selectedShown = GalleryViewModel.ALL.equals(viewModel.getCategory());
        for (OrgCategories.Category category : shown) {
            Chip chip = new Chip(requireContext());
            chip.setId(View.generateViewId());
            chip.setTag(category.value);
            chip.setText(category.label);
            chip.setCheckable(true);
            binding.categoryGroup.addView(chip);
            selectedShown |= category.value.equals(viewModel.getCategory());
        }
        bindingChips = false;
        // Pindah ke ruang kerja berjenjang lain: kategori terpilih yang tidak ada di jenjang itu dibatalkan.
        if (!selectedShown) {
            viewModel.setCategory(GalleryViewModel.ALL);
        }
        syncControls();
    }

    private void syncControls() {
        bindingChips = true;
        for (int i = 0; i < binding.categoryGroup.getChildCount(); i++) {
            View chip = binding.categoryGroup.getChildAt(i);
            if (viewModel.getCategory().equals(chip.getTag())) {
                binding.categoryGroup.check(chip.getId());
            }
        }
        bindingChips = false;
        for (int i = 0; i < SORTS.length; i++) {
            if (SORTS[i].equals(viewModel.getSort())) {
                binding.sortButton.setText(SORT_LABELS[i]);
            }
        }
    }

    private void render(@Nullable GalleryListState state) {
        if (state == null) {
            return;
        }
        footer.setLoading(state.loadingMore);
        boolean firstLoad = state.loadingFirstPage && state.items.isEmpty();
        binding.skeleton.setVisibility(firstLoad ? View.VISIBLE : View.GONE);

        if (state.error != null) {
            binding.list.setVisibility(View.GONE);
            binding.state.showError(ErrorMessages.forError(requireContext(), state.error), viewModel::refresh);
        } else if (!state.loadingFirstPage && state.items.isEmpty()) {
            binding.list.setVisibility(View.GONE);
            if (viewModel.hasActiveFilter()) {
                binding.state.showEmpty(getString(R.string.gallery_filter_empty),
                        getString(R.string.action_clear_filter), this::clearFilters);
            } else {
                binding.state.showEmpty(getString(R.string.gallery_empty));
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
        handler.removeCallbacks(applySearch);
        binding = null;
    }
}
