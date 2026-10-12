package com.aris.templateapp.ui.website;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsitePage;
import com.aris.templateapp.data.model.WebsiteQuery;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.databinding.FragmentWebsitesBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.StatusBannerView;
import com.aris.templateapp.ui.creator.CreatorDashboardFragment;
import com.aris.templateapp.ui.workspace.WorkspaceViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Tab Website (alur-pembuatan-website.md bagian 4): website milik ruang kerja aktif, diambil dari server. Pencarian
 * dikirim setelah jeda singkat (tidak tiap huruf); offline memakai cache dengan banner. Ketuk kartu → halaman kelola.
 */
@AndroidEntryPoint
public class WebsitesFragment extends Fragment {

    private static final long SEARCH_DELAY_MS = 400;
    private static final WebsiteQuery.Sort[] SORTS = {WebsiteQuery.Sort.UPDATED, WebsiteQuery.Sort.NEWEST,
            WebsiteQuery.Sort.NAME};
    private static final int[] SORT_LABELS = {R.string.sort_website_updated, R.string.sort_project_created,
            R.string.sort_name};

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentWebsitesBinding binding;
    private WebsitesViewModel viewModel;
    private WebsiteAdapter adapter;
    @Nullable private Workspace workspace;
    // Chip diubah dari kode (mis. Hapus filter) tidak boleh dianggap pilihan user.
    private boolean bindingQuery;
    private boolean resumedBefore;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable applySearch = () -> {
        if (binding != null) {
            viewModel.setSearch(String.valueOf(binding.searchInput.getText()));
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWebsitesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(WebsitesViewModel.class);
        adapter = new WebsiteAdapter(thumbnailLoader, new WebsiteAdapter.Listener() {
            @Override
            public void onOpen(Website website) {
                NavHostFragment.findNavController(WebsitesFragment.this)
                        .navigate(R.id.websiteManageFragment, WebsiteArgs.of(website));
            }

            @Override
            public void onMenu(View anchor, Website website) {
                showMenu(anchor, website);
            }
        });
        binding.list.setAdapter(adapter);
        binding.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layout = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (dy > 0 && layout != null && layout.findLastVisibleItemPosition() >= adapter.getItemCount() - 4) {
                    viewModel.loadMore();
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
                if (!bindingQuery) {
                    handler.removeCallbacks(applySearch);
                    handler.postDelayed(applySearch, SEARCH_DELAY_MS);
                }
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
        binding.statusGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (!bindingQuery && !ids.isEmpty()) {
                viewModel.setFilter(filterOf(ids.get(0)));
            }
        });
        binding.sortButton.setOnClickListener(this::showSortMenu);

        new ViewModelProvider(this).get(WorkspaceViewModel.class).getActive().observe(getViewLifecycleOwner(), active -> {
            workspace = active;
            viewModel.setOrganization(active == null ? null : active.getId());
        });
        viewModel.getQuery().observe(getViewLifecycleOwner(), this::bindQuery);
        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getMessage().observe(getViewLifecycleOwner(), event -> {
            String text = event.getContentIfNotHandled();
            if (text != null) {
                Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_SHORT).show();
            }
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            ApiError error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), message(error), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tab dibuka lagi / kembali dari halaman kelola: anggota lain mungkin sudah mengubah website. Resume pertama
        // dilewati karena daftar baru saja dimuat saat ruang kerja aktif diketahui.
        if (resumedBefore) {
            viewModel.refresh();
        }
        resumedBefore = true;
    }

    private void bindQuery(WebsiteQuery query) {
        bindingQuery = true;
        binding.list.scrollToPosition(0);
        binding.statusGroup.check(chipOf(query.filter));
        if (!query.search.equals(String.valueOf(binding.searchInput.getText()).trim())) {
            binding.searchInput.setText(query.search);
        }
        for (int i = 0; i < SORTS.length; i++) {
            if (SORTS[i] == query.sort) {
                binding.sortButton.setText(SORT_LABELS[i]);
            }
        }
        bindingQuery = false;
    }

    private void render(WebsitesViewModel.State state) {
        bindCounts(state.counts);
        binding.limit.setVisibility(state.sitesLimit > 0 ? View.VISIBLE : View.GONE);
        if (state.sitesLimit > 0) {
            binding.limit.setText(getString(R.string.websites_limit, state.sitesUsed, state.sitesLimit));
        }
        binding.offlineBanner.setVisibility(state.offlineSince == null ? View.GONE : View.VISIBLE);
        if (state.offlineSince != null) {
            binding.offlineBanner.bind(StatusBannerView.Kind.WARNING, getString(R.string.websites_offline,
                    WebsiteUi.ago(requireContext(), state.offlineSince)));
        }
        binding.loadingMore.setVisibility(state.loadingMore ? View.VISIBLE : View.GONE);
        adapter.submitList(state.items);

        if (workspace == null) {
            binding.list.setVisibility(View.GONE);
            binding.state.showEmpty(getString(R.string.websites_need_workspace));
        } else if (state.loadingFirstPage && state.items.isEmpty()) {
            binding.list.setVisibility(View.GONE);
            binding.state.showLoading();
        } else if (state.error != null) {
            binding.list.setVisibility(View.GONE);
            binding.state.showError(ErrorMessages.forError(requireContext(), state.error), viewModel::refresh);
        } else if (!state.items.isEmpty()) {
            binding.state.hide();
            binding.list.setVisibility(View.VISIBLE);
        } else {
            binding.list.setVisibility(View.GONE);
            WebsiteQuery query = viewModel.getQuery().getValue();
            if (query == null || query.isUnfiltered()) {
                // Belum ada website (bagian 4.4): ajakan memulai, membuka sheet + yang sama dengan tombol tengah.
                binding.state.showEmpty(getString(R.string.websites_empty_title, workspace.getGreetingName())
                                + "\n" + getString(R.string.websites_empty_body), getString(R.string.action_start_now),
                        () -> ((CreatorDashboardFragment) requireParentFragment()).showCreateSheet());
            } else {
                binding.state.showEmpty(getString(R.string.websites_filter_empty),
                        getString(R.string.action_clear_filter), () -> {
                            hideKeyboard();
                            handler.removeCallbacks(applySearch);
                            viewModel.clearFilters();
                        });
            }
        }
    }

    private void bindCounts(@Nullable WebsitePage.Counts counts) {
        setChip(R.id.status_all, R.string.filter_all, counts == null ? null : counts.all);
        setChip(R.id.status_draft, R.string.filter_draft, counts == null ? null : counts.draft);
        setChip(R.id.status_online, R.string.filter_online, counts == null ? null : counts.online);
        setChip(R.id.status_changed, R.string.filter_changed, counts == null ? null : counts.changed);
    }

    private void setChip(int chipId, @StringRes int label, @Nullable Long count) {
        Chip chip = binding.statusGroup.findViewById(chipId);
        chip.setText(count == null ? getString(label)
                : getString(R.string.chip_with_count, getString(label), count.intValue()));
    }

    // ---- Menu ⋮ (bagian 4.3) ---------------------------------------------------------------------

    private void showMenu(View anchor, Website website) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        Menu menu = popup.getMenu();
        menu.add(Menu.NONE, R.string.project_rename, 0, R.string.project_rename);
        menu.add(Menu.NONE, R.string.project_duplicate, 1, R.string.project_duplicate);
        menu.add(Menu.NONE, R.string.website_export, 2, R.string.website_export);
        // Hapus hanya untuk Pemilik (G3); backend tetap menolak peran lain.
        if (workspace != null && workspace.getRole() == WorkspaceRole.OWNER) {
            menu.add(Menu.NONE, R.string.project_delete, 3, R.string.project_delete);
        }
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.string.project_rename) {
                WebsiteDialogs.rename(requireContext(), website,
                        name -> viewModel.rename(website, name, getString(R.string.website_renamed)));
            } else if (id == R.string.project_duplicate) {
                viewModel.duplicate(website, getString(R.string.website_duplicated, "%1$s"));
            } else if (id == R.string.website_export) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.website_export)
                        .setMessage(R.string.website_export_soon)
                        .setPositiveButton(R.string.action_ok, null)
                        .show();
            } else if (id == R.string.project_delete) {
                WebsiteDialogs.delete(requireContext(), website, confirmName -> viewModel.delete(website,
                        confirmName, getString(R.string.website_deleted, website.name)));
            }
            return true;
        });
        popup.show();
    }

    private String message(ApiError error) {
        // Pesan umum CONFIRM_NAME_MISMATCH menyebut organisasi; di sini yang diketik nama website.
        return "CONFIRM_NAME_MISMATCH".equals(error.getCode()) ? getString(R.string.error_website_name_mismatch)
                : ErrorMessages.forError(requireContext(), error);
    }

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

    private static WebsiteQuery.Filter filterOf(int chipId) {
        if (chipId == R.id.status_draft) return WebsiteQuery.Filter.DRAFT;
        if (chipId == R.id.status_online) return WebsiteQuery.Filter.ONLINE;
        if (chipId == R.id.status_changed) return WebsiteQuery.Filter.CHANGED;
        return WebsiteQuery.Filter.ALL;
    }

    private static int chipOf(WebsiteQuery.Filter filter) {
        switch (filter) {
            case DRAFT:
                return R.id.status_draft;
            case ONLINE:
                return R.id.status_online;
            case CHANGED:
                return R.id.status_changed;
            case ALL:
            default:
                return R.id.status_all;
        }
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
        resumedBefore = false;
        binding = null;
    }
}
