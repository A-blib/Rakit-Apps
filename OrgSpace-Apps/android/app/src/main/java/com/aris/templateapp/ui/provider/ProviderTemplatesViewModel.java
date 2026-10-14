package com.aris.templateapp.ui.provider;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.R;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto.TemplateSummaryDto;
import com.aris.templateapp.data.repository.ProviderRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * "Template Anda": folder (ala Google Drive), filter, urutan, pencarian, dan paginasi 20 per halaman
 * (alur-provider.md bagian 6).
 * <p>
 * Cara kerja folder: halaman utama menampilkan daftar folder + template yang belum masuk folder. Begitu filter
 * status atau pencarian aktif di halaman utama, folder disembunyikan dan template dari SEMUA folder
 * ditampilkan (seperti hasil pencarian Drive). Di dalam folder, filter hanya berlaku untuk isi folder itu.
 * <p>
 * Diambil dengan {@code new ViewModelProvider(requireActivity())} sehingga hidup selama Activity hidup:
 * filter dan folder terakhir tetap diingat walau user pindah tab atau membuka detail.
 */
@HiltViewModel
public class ProviderTemplatesViewModel extends ViewModel {

    public static final String ALL = "all";
    public static final String STATUS_NEEDS_FIX = "needs_fix";
    public static final String STATUS_PUBLISHED = "published";
    public static final String SORT_UPDATED = "updated";
    /** Nilai parameter {@code folder} untuk "hanya template tanpa folder". */
    private static final String FOLDER_ROOT = "root";

    /** Pesan singkat (Snackbar) setelah aksi folder berhasil. {@code arg} mengisi %1$s bila ada. */
    public static final class Notice {
        @StringRes
        public final int message;
        @Nullable
        public final String arg;

        Notice(@StringRes int message, @Nullable String arg) {
            this.message = message;
            this.arg = arg;
        }
    }

    private final ProviderRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<TemplateListState> state = new MutableLiveData<>();
    private final MutableLiveData<Event<ApiError>> loadMoreFailed = new MutableLiveData<>();
    private final MutableLiveData<Event<ApiError>> actionFailed = new MutableLiveData<>();
    private final MutableLiveData<Event<Notice>> notice = new MutableLiveData<>();

    private String status = ALL;
    private String sort = SORT_UPDATED;
    private String query = "";
    @Nullable
    private TemplateFolderDto folder;
    /** Tampilan kisi (2 kolom) atau baris; diingat selama app terbuka, seperti filter. */
    private boolean gridMode;

    private final List<TemplateSummaryDto> items = new ArrayList<>();
    private List<TemplateFolderDto> folders = Collections.emptyList();
    private TemplateListDto.StatusCountsDto counts;
    private int page;
    private int totalPages;
    private boolean loadingFirstPage;
    private boolean loadingMore;
    private int generation;

    @Inject
    public ProviderTemplatesViewModel(ProviderRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    public LiveData<TemplateListState> getState() {
        return state;
    }

    public LiveData<Event<ApiError>> getLoadMoreFailed() {
        return loadMoreFailed;
    }

    /** Gagal membuat/mengubah/menghapus folder atau memindahkan template. */
    public LiveData<Event<ApiError>> getActionFailed() {
        return actionFailed;
    }

    public LiveData<Event<Notice>> getNotice() {
        return notice;
    }

    public String getStatus() {
        return status;
    }

    public String getSort() {
        return sort;
    }

    public String getQuery() {
        return query;
    }

    @Nullable
    public TemplateFolderDto getFolder() {
        return folder;
    }

    /** Semua folder milik provider (untuk dialog "Pindahkan ke folder"), walau sedang disembunyikan dari daftar. */
    public List<TemplateFolderDto> getFolders() {
        return folders;
    }

    @Nullable
    public TemplateListDto.StatusCountsDto getCounts() {
        return counts;
    }

    public boolean isGridMode() {
        return gridMode;
    }

    public void setGridMode(boolean gridMode) {
        this.gridMode = gridMode;
    }

    public boolean hasActiveFilter() {
        return !ALL.equals(status) || !query.isEmpty();
    }

    public void setStatus(String status) {
        if (!status.equals(this.status)) {
            this.status = status;
            refresh();
        }
    }

    /**
     * Dari luar tab (mis. "Lihat semua" di Beranda): keluar dari folder lalu tampilkan status itu dari semua
     * folder, supaya tidak ada template bermasalah yang tersembunyi di folder lain.
     */
    public void showStatusEverywhere(String status) {
        folder = null;
        this.status = status;
        refresh();
    }

    public void setSort(String sort) {
        if (!sort.equals(this.sort)) {
            this.sort = sort;
            refresh();
        }
    }

    public void setQuery(String query) {
        String trimmed = query == null ? "" : query.trim();
        if (!trimmed.equals(this.query)) {
            this.query = trimmed;
            refresh();
        }
    }

    /** Tombol "Hapus filter": kembali ke semua status, tanpa pencarian (urutan & folder tetap). */
    public void clearFilters() {
        status = ALL;
        query = "";
        refresh();
    }

    public void openFolder(TemplateFolderDto folder) {
        this.folder = folder;
        items.clear();
        refresh();
    }

    /** Kembali ke halaman utama (tombol kembali di judul folder atau tombol Back HP). */
    public void closeFolder() {
        if (folder != null) {
            folder = null;
            items.clear();
            refresh();
        }
    }

    // ---------- aksi folder ----------

    /** @param moveTemplateId template yang langsung dipindah ke folder baru ini; null = hanya membuat folder */
    public void createFolder(String name, @Nullable String moveTemplateId) {
        executors.networkIO().execute(() -> {
            Resource<TemplateFolderDto> created = repository.createFolder(name.trim());
            if (created.getStatus() != Resource.Status.SUCCESS) {
                fail(created.getError());
                return;
            }
            TemplateFolderDto folder = created.getData();
            if (moveTemplateId != null) {
                Resource<Boolean> moved = repository.moveTemplate(moveTemplateId, folder.id);
                if (moved.getStatus() != Resource.Status.SUCCESS) {
                    fail(moved.getError());
                    return;
                }
                done(new Notice(R.string.folder_moved_to, folder.name));
            } else {
                done(new Notice(R.string.folder_created, folder.name));
            }
        });
    }

    public void renameFolder(TemplateFolderDto target, String name) {
        executors.networkIO().execute(() -> {
            Resource<TemplateFolderDto> result = repository.renameFolder(target.id, name.trim());
            if (result.getStatus() != Resource.Status.SUCCESS) {
                fail(result.getError());
                return;
            }
            executors.mainThread().execute(() -> {
                if (folder != null && folder.id.equals(target.id)) {
                    folder = result.getData();
                }
            });
            done(new Notice(R.string.folder_renamed, null));
        });
    }

    public void deleteFolder(TemplateFolderDto target) {
        executors.networkIO().execute(() -> {
            Resource<Boolean> result = repository.deleteFolder(target.id);
            if (result.getStatus() != Resource.Status.SUCCESS) {
                fail(result.getError());
                return;
            }
            executors.mainThread().execute(() -> {
                if (folder != null && folder.id.equals(target.id)) {
                    folder = null;
                }
            });
            done(new Notice(R.string.folder_deleted, target.name));
        });
    }

    /** @param target null = keluarkan dari folder (kembali ke halaman utama) */
    public void moveTemplate(String templateId, @Nullable TemplateFolderDto target) {
        executors.networkIO().execute(() -> {
            Resource<Boolean> result = repository.moveTemplate(templateId, target == null ? null : target.id);
            if (result.getStatus() != Resource.Status.SUCCESS) {
                fail(result.getError());
                return;
            }
            done(target == null ? new Notice(R.string.folder_moved_out, null)
                    : new Notice(R.string.folder_moved_to, target.name));
        });
    }

    private void fail(ApiError error) {
        executors.mainThread().execute(() -> actionFailed.setValue(new Event<>(error)));
    }

    private void done(Notice message) {
        executors.mainThread().execute(() -> {
            notice.setValue(new Event<>(message));
            refresh();
        });
    }

    // ---------- memuat daftar ----------

    /** Halaman utama tanpa filter = template tanpa folder; filter aktif = semua folder; di dalam folder = isinya. */
    @Nullable
    private String folderParam() {
        if (folder != null) {
            return folder.id;
        }
        return hasActiveFilter() ? null : FOLDER_ROOT;
    }

    /** Muat ulang dari halaman pertama (isi lama tetap tampil sampai jawaban datang). */
    public void refresh() {
        int request = ++generation;
        loadingFirstPage = true;
        loadingMore = false;
        publish(null);
        String f = folderParam(), s = status, c = ALL, o = sort, q = query.isEmpty() ? null : query;
        executors.networkIO().execute(() -> {
            // Daftar folder selalu ikut diambil: dipakai baris folder di halaman utama dan dialog "Pindahkan".
            Resource<List<TemplateFolderDto>> folderResult = repository.folders();
            Resource<TemplateListDto> result = repository.templates(f, s, c, o, q, 0);
            executors.mainThread().execute(() -> {
                if (request != generation) {
                    return;
                }
                if (folderResult.getStatus() == Resource.Status.SUCCESS) {
                    folders = folderResult.getData();
                    if (syncOpenFolder()) {
                        return;
                    }
                }
                loadingFirstPage = false;
                items.clear();
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    accept(result.getData());
                    publish(null);
                } else {
                    publish(result.getError());
                }
            });
        });
    }

    /**
     * Menyamakan folder yang sedang dibuka dengan data terbaru (mis. diganti nama atau dihapus dari HP lain).
     *
     * @return true jika folder sudah tidak ada sehingga daftar dimuat ulang dari halaman utama
     */
    private boolean syncOpenFolder() {
        if (folder == null) {
            return false;
        }
        for (TemplateFolderDto f : folders) {
            if (Objects.equals(f.id, folder.id)) {
                folder = f;
                return false;
            }
        }
        folder = null;
        refresh();
        return true;
    }

    /** Dipanggil saat daftar di-scroll mendekati akhir. Tidak melakukan apa-apa jika tidak ada halaman lagi. */
    public void loadMore() {
        if (loadingFirstPage || loadingMore || page + 1 >= totalPages) {
            return;
        }
        int request = generation;
        loadingMore = true;
        publish(null);
        int next = page + 1;
        String f = folderParam(), s = status, c = ALL, o = sort, q = query.isEmpty() ? null : query;
        executors.networkIO().execute(() -> {
            Resource<TemplateListDto> result = repository.templates(f, s, c, o, q, next);
            executors.mainThread().execute(() -> {
                if (request != generation) {
                    return;
                }
                loadingMore = false;
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    accept(result.getData());
                } else {
                    loadMoreFailed.setValue(new Event<>(result.getError()));
                }
                publish(null);
            });
        });
    }

    private void accept(TemplateListDto data) {
        if (data.items != null) {
            items.addAll(data.items);
        }
        counts = data.counts;
        page = data.page;
        totalPages = data.totalPages;
    }

    private void publish(ApiError error) {
        boolean showFolders = folder == null && !hasActiveFilter();
        state.setValue(new TemplateListState(new ArrayList<>(items),
                showFolders ? new ArrayList<>(folders) : new ArrayList<>(), folder, counts, loadingFirstPage,
                loadingMore, page + 1 < totalPages, error));
    }
}
