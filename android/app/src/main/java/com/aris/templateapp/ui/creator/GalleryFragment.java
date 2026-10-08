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
import com.aris.templateapp.data.repository.GalleryRepository;
import com.aris.templateapp.databinding.FragmentGalleryBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.provider.LoadingFooterAdapter;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Tab Template: galeri versi awal (alur-pembuatan-website.md bagian 6.2). Butuh internet. */
@AndroidEntryPoint
public class GalleryFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;


    private static final long SEARCH_DELAY_MS = 400;
    private static final int LOAD_MORE_THRESHOLD = 5;

    /** Chip kategori → nilai backend (urutan sama dengan chip di layout). */
    private static final int[] CATEGORY_CHIPS = {R.id.category_all, R.id.category_sekolah, R.id.category_organisasi,
            R.id.category_umkm, R.id.category_instansi, R.id.category_pribadi, R.id.category_lainnya};
    private static final String[] CATEGORY_VALUES = {GalleryViewModel.ALL, "sekolah", "organisasi", "umkm",
            "instansi", "pribadi", "lainnya"};
    private static final String[] SORTS = {GalleryRepository.SORT_POPULAR, GalleryRepository.SORT_NEWEST};
    private static final int[] SORT_LABELS = {R.string.sort_popular, R.string.sort_newest};

    @Inject
    GalleryRepository galleryRepository;

    private FragmentGalleryBinding binding;
    private GalleryViewModel viewModel;
    private GalleryAdapter adapter;
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

        adapter = GalleryAdapter.list(thumbnailLoader, template -> TemplateOpener.open(this, galleryRepository, template));
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
        binding.categoryGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (!ids.isEmpty()) {
                viewModel.setCategory(categoryOf(ids.get(0)));
            }
        });
        binding.sortButton.setOnClickListener(this::showSortMenu);

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

    /** Tab tampil (pertama kali, digeser/dipilih lagi, kembali dari editor): samakan kontrol lalu ambil data terbaru. */
    @Override
    public void onResume() {
        super.onResume();
        syncControls();
        viewModel.refresh();
    }

    private void syncControls() {
        for (int i = 0; i < CATEGORY_VALUES.length; i++) {
            if (CATEGORY_VALUES[i].equals(viewModel.getCategory())) {
                binding.categoryGroup.check(CATEGORY_CHIPS[i]);
            }
        }
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

    private static String categoryOf(int chipId) {
        for (int i = 0; i < CATEGORY_CHIPS.length; i++) {
            if (CATEGORY_CHIPS[i] == chipId) {
                return CATEGORY_VALUES[i];
            }
        }
        return GalleryViewModel.ALL;
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
