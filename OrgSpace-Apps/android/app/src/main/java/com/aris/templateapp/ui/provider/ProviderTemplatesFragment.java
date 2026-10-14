package com.aris.templateapp.ui.provider;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto.StatusCountsDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto.TemplateSummaryDto;
import com.aris.templateapp.databinding.FragmentProviderTemplatesBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.guide.Guide;
import com.google.android.material.button.MaterialButton;
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

    private boolean isSpeedDialOpen = false;
    private boolean isGridMode = false;

    /**
     * Tombol Back HP saat speed dial terbuka: tutup menu melayang dulu sebelum aksi kembali lainnya.
     */
    private final OnBackPressedCallback closeSpeedDialOnBack = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            closeSpeedDial();
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
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (!isGridMode) {
                    return 2;
                }
                int folderHeaderCount = folderHeader.getItemCount();
                if (position < folderHeaderCount) {
                    return 2;
                }
                position -= folderHeaderCount;
                int folderCount = folderAdapter.getItemCount();
                if (position < folderCount) {
                    return 1;
                }
                position -= folderCount;
                int templateHeaderCount = templateHeader.getItemCount();
                if (position < templateHeaderCount) {
                    return 2;
                }
                position -= templateHeaderCount;
                int templateCount = adapter.getItemCount();
                if (position < templateCount) {
                    return 1;
                }
                return 2;
            }
        });
        binding.list.setLayoutManager(layoutManager);
        binding.list.setAdapter(concat);
        binding.list.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                if (!isGridMode) {
                    outRect.setEmpty();
                    return;
                }
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (lp instanceof GridLayoutManager.LayoutParams) {
                    GridLayoutManager.LayoutParams glp = (GridLayoutManager.LayoutParams) lp;
                    if (glp.getSpanSize() == 1) {
                        int halfGap = getResources().getDimensionPixelSize(R.dimen.space_1);
                        if (glp.getSpanIndex() == 0) {
                            outRect.left = 0;
                            outRect.right = halfGap;
                        } else {
                            outRect.left = halfGap;
                            outRect.right = 0;
                        }
                        return;
                    }
                }
                outRect.setEmpty();
            }
        });
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

        // Satu tombol ala Google Drive: ikonnya menunjukkan tampilan TUJUAN (kisi saat sedang baris, dan sebaliknya).
        binding.viewToggle.setOnClickListener(v -> {
            viewModel.setGridMode(!viewModel.isGridMode());
            setViewMode(viewModel.isGridMode());
        });
        setViewMode(viewModel.isGridMode());

        setUpSearch();
        binding.fab.setOnClickListener(v -> openSpeedDial());
        binding.fabClose.setOnClickListener(v -> closeSpeedDial());
        binding.speedDialScrim.setOnClickListener(v -> closeSpeedDial());
        binding.statusChip.setOnClickListener(v -> openSpeedDial());
        binding.statusChip.setCloseIconContentDescription(getString(R.string.cd_status_chip_clear));
        binding.statusChip.setOnCloseIconClickListener(v -> {
            viewModel.setStatus(ProviderTemplatesViewModel.ALL);
            syncControls();
        });

        // Aksi pembuatan di speed dial (paling dekat dengan jempol)
        binding.speedDialActionFolder.setOnClickListener(v -> {
            closeSpeedDial();
            showCreateFolderDialog(null);
        });
        binding.speedDialActionUpload.setOnClickListener(v -> {
            closeSpeedDial();
            navigateToUploadTab();
        });

        // Filter status di speed dial
        binding.speedDialStatusAll.setOnClickListener(v -> applyStatusFromSpeedDial(ProviderTemplatesViewModel.ALL));
        binding.speedDialStatusPublished.setOnClickListener(v -> applyStatusFromSpeedDial(ProviderTemplatesViewModel.STATUS_PUBLISHED));
        binding.speedDialStatusNeedsFix.setOnClickListener(v -> applyStatusFromSpeedDial(ProviderTemplatesViewModel.STATUS_NEEDS_FIX));
        binding.speedDialStatusChecking.setOnClickListener(v -> applyStatusFromSpeedDial("checking"));
        binding.speedDialStatusDraft.setOnClickListener(v -> applyStatusFromSpeedDial("draft"));
        binding.speedDialStatusDisabled.setOnClickListener(v -> applyStatusFromSpeedDial("disabled"));

        binding.sortButton.setOnClickListener(this::showSortMenu);
        binding.folderBack.setOnClickListener(v -> viewModel.closeFolder());
        binding.folderMenu.setOnClickListener(v -> {
            TemplateFolderDto folder = viewModel.getFolder();
            if (folder != null) {
                showFolderMenu(folder, v);
            }
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), closeSpeedDialOnBack);
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

    private View[] getSpeedDialPills() {
        if (binding == null) {
            return new View[0];
        }
        // Urutan dari bawah ke atas (paling dekat dengan jempol/FAB adalah index 0)
        return new View[]{
                binding.speedDialActionFolder,
                binding.speedDialActionUpload,
                binding.speedDialStatusAll,
                binding.speedDialStatusPublished,
                binding.speedDialStatusNeedsFix,
                binding.speedDialStatusChecking,
                binding.speedDialStatusDraft,
                binding.speedDialStatusDisabled
        };
    }

    private void openSpeedDial() {
        if (isSpeedDialOpen || binding == null) {
            return;
        }
        hideKeyboard();
        isSpeedDialOpen = true;
        closeSpeedDialOnBack.setEnabled(true);

        updateSpeedDialCounts();

        // 1. Scrim meredup halus
        binding.speedDialScrim.animate().cancel();
        binding.speedDialScrim.setVisibility(View.VISIBLE);
        binding.speedDialScrim.setAlpha(0f);
        binding.speedDialScrim.animate()
                .alpha(1f)
                .setDuration(240)
                .start();

        // 2. Transisi FAB utama (+) ke tombol Tutup (✕) dengan putaran & skala
        binding.fab.setVisibility(View.GONE);
        binding.fabClose.animate().cancel();
        binding.fabClose.setVisibility(View.VISIBLE);
        binding.fabClose.setAlpha(0f);
        binding.fabClose.setScaleX(0.7f);
        binding.fabClose.setScaleY(0.7f);
        binding.fabClose.setRotation(-90f);
        binding.fabClose.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 3. Animasi bertingkat (staggered cascade) tiap pil melayang dari bawah ke atas
        View[] pills = getSpeedDialPills();
        for (View pill : pills) {
            pill.animate().cancel();
            pill.setAlpha(0f);
            pill.setTranslationY(48f);
            pill.setScaleX(0.65f);
            pill.setScaleY(0.65f);
        }

        binding.speedDialScroll.setVisibility(View.VISIBLE);
        binding.speedDialScroll.setAlpha(1f);
        binding.speedDialScroll.setTranslationY(0f);

        // Tunggu satu siklus agar dimensi view terukur presisi untuk titik tumpu (pivot)
        binding.speedDialContainer.post(() -> {
            if (!isSpeedDialOpen || binding == null) {
                return;
            }
            for (int i = 0; i < pills.length; i++) {
                View pill = pills[i];
                float pivotX = pill.getWidth() > 0 ? pill.getWidth() : 200f;
                float pivotY = pill.getHeight() > 0 ? (pill.getHeight() / 2f) : 24f;
                pill.setPivotX(pivotX);
                pill.setPivotY(pivotY);

                pill.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(260)
                        .setStartDelay((long) i * 28) // Stagger dari bawah ke atas
                        .setInterpolator(new OvershootInterpolator(1.1f))
                        .start();
            }
        });
    }

    private void closeSpeedDial() {
        if (!isSpeedDialOpen || binding == null) {
            return;
        }
        isSpeedDialOpen = false;
        closeSpeedDialOnBack.setEnabled(false);

        // 1. Scrim memudar
        binding.speedDialScrim.animate().cancel();
        binding.speedDialScrim.animate()
                .alpha(0f)
                .setDuration(160)
                .withEndAction(() -> {
                    if (binding != null) {
                        binding.speedDialScrim.setVisibility(View.GONE);
                    }
                }).start();

        // 2. Tombol Tutup (✕) berputar balik dan kembali ke FAB (+)
        binding.fabClose.animate().cancel();
        binding.fabClose.animate()
                .alpha(0f)
                .scaleX(0.7f)
                .scaleY(0.7f)
                .rotation(-90f)
                .setDuration(160)
                .withEndAction(() -> {
                    if (binding != null) {
                        binding.fabClose.setVisibility(View.GONE);
                        binding.fab.setVisibility(View.VISIBLE);
                        binding.fab.setAlpha(1f);
                        binding.fab.setScaleX(1f);
                        binding.fab.setScaleY(1f);
                        binding.fab.setRotation(0f);
                    }
                }).start();

        // 3. Pil-pil melayang runtuh ke bawah secara berurutan (reverse cascade)
        View[] pills = getSpeedDialPills();
        for (int i = 0; i < pills.length; i++) {
            View pill = pills[i];
            pill.animate().cancel();
            long delay = (long) (pills.length - 1 - i) * 16;
            final boolean isLast = (i == 0);

            pill.animate()
                    .alpha(0f)
                    .translationY(36f)
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .setDuration(140)
                    .setStartDelay(delay)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        if (isLast && binding != null) {
                            binding.speedDialScroll.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    private void applyStatusFromSpeedDial(String status) {
        closeSpeedDial();
        viewModel.setStatus(status);
        syncControls();
    }

    private void navigateToUploadTab() {
        Fragment parent = getParentFragment();
        if (parent instanceof ProviderDashboardFragment) {
            ((ProviderDashboardFragment) parent).selectTab(R.id.tab_upload);
        }
    }

    private void updateSpeedDialCounts() {
        if (binding == null) {
            return;
        }
        StatusCountsDto counts = viewModel.getCounts();
        setFilterPillText(binding.speedDialStatusAll, R.string.filter_all, counts == null ? null : counts.all);
        setFilterPillText(binding.speedDialStatusPublished, R.string.filter_published,
                counts == null ? null : counts.published);
        setFilterPillText(binding.speedDialStatusNeedsFix, R.string.filter_needs_fix,
                counts == null ? null : counts.needsFix);
        setFilterPillText(binding.speedDialStatusChecking, R.string.filter_checking,
                counts == null ? null : counts.checking);
        setFilterPillText(binding.speedDialStatusDraft, R.string.filter_draft,
                counts == null ? null : counts.draft);
        setFilterPillText(binding.speedDialStatusDisabled, R.string.filter_disabled,
                counts == null ? null : counts.disabled);
    }

    private void setFilterPillText(MaterialButton button, @StringRes int labelRes, @Nullable Long count) {
        if (button == null) {
            return;
        }
        if (count != null && count > 0) {
            button.setText(getString(R.string.chip_with_count, getString(labelRes), count));
        } else {
            button.setText(labelRes);
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
            // Jumlah dari server ikut tersaring pencarian, jadi "belum punya template" hanya
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

    private void setViewMode(boolean grid) {
        if (binding != null) {
            binding.viewToggle.setImageResource(grid ? R.drawable.ic_list : R.drawable.ic_grid_view);
            binding.viewToggle.setContentDescription(getString(grid ? R.string.cd_view_line : R.string.cd_view_grid));
        }
        // Tanpa "return jika sama": objek Fragment bisa dipakai ulang dengan view & adapter baru (mis. kembali dari
        // detail template), sehingga field ini masih true padahal adapter baru masih mode baris. Adapter sendiri
        // mengabaikan pemanggilan yang tidak mengubah apa-apa.
        this.isGridMode = grid;
        folderAdapter.setGridMode(grid);
        adapter.setGridMode(grid);
        if (binding != null) {
            binding.list.invalidateItemDecorations();
            binding.list.requestLayout();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isSpeedDialOpen = false;
        closeSpeedDialOnBack.setEnabled(false);
        handler.removeCallbacks(applySearch);
        binding = null;
    }
}
