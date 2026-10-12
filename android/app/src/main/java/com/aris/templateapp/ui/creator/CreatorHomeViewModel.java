package com.aris.templateapp.ui.creator;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.PublishRequest;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsitePage;
import com.aris.templateapp.data.model.WebsiteQuery;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.data.remote.dto.GalleryPageDto;
import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;
import com.aris.templateapp.data.repository.GalleryRepository;
import com.aris.templateapp.data.repository.WebsiteRepository;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Beranda pembuat website (alur-pembuatan-website.md bagian 3): website terakhir diubah di ruang kerja aktif,
 * banner perubahan & permintaan terbit (Admin/Pemilik), dan "Template untuk {org}" dari galeri.
 */
@HiltViewModel
public class CreatorHomeViewModel extends ViewModel {

    /** 1 kartu besar + maksimal 3 kartu kecil (bagian 3.1). */
    static final int RECENT_LIMIT = 4;

    /** Isi Beranda dari server untuk satu ruang kerja. */
    public static final class Home {
        public final List<Website> recent;
        /** Website online yang isinya lebih baru dari versi online (banner, hanya Admin/Pemilik). */
        public final long changedCount;
        /** Permintaan "Minta diterbitkan" yang masih terbuka (hanya Admin/Pemilik). */
        public final List<PublishRequest> requests;
        /** Belum tahu (null) saat gagal dimuat dan cache kosong: Beranda tidak memilih sapaan besar/kecil. */
        @Nullable public final Boolean hasWebsites;

        Home(List<Website> recent, long changedCount, List<PublishRequest> requests, @Nullable Boolean hasWebsites) {
            this.recent = recent;
            this.changedCount = changedCount;
            this.requests = requests;
            this.hasWebsites = hasWebsites;
        }

        static Home empty(@Nullable Boolean hasWebsites) {
            return new Home(Collections.emptyList(), 0, Collections.emptyList(), hasWebsites);
        }
    }

    private final WebsiteRepository websiteRepository;
    private final GalleryRepository galleryRepository;
    private final AppExecutors executors;

    private final MutableLiveData<Home> home = new MutableLiveData<>();
    private final MutableLiveData<List<GalleryTemplateDto>> recommendations = new MutableLiveData<>();
    @Nullable private Workspace workspace;
    /** Kategori yang dipakai rekomendasi terakhir (untuk "Lihat semua"), null = semua kategori. */
    private String recommendedCategory;
    private int generation;
    private boolean started;

    @Inject
    public CreatorHomeViewModel(WebsiteRepository websiteRepository, GalleryRepository galleryRepository,
                                AppExecutors executors) {
        this.websiteRepository = websiteRepository;
        this.galleryRepository = galleryRepository;
        this.executors = executors;
    }

    public LiveData<Home> getHome() {
        return home;
    }

    /** Daftar kosong = sembunyikan bagian ini (offline atau galeri kosong, bagian 6). */
    public LiveData<List<GalleryTemplateDto>> getRecommendations() {
        return recommendations;
    }

    public String getRecommendedCategory() {
        return recommendedCategory;
    }

    /** Tamu atau belum punya ruang kerja = null. Ruang kerja berganti → Beranda dimuat ulang. */
    public void setWorkspace(@Nullable Workspace workspace) {
        boolean changed = workspace == null ? this.workspace != null
                : this.workspace == null || !workspace.getId().equals(this.workspace.getId())
                || workspace.getRole() != this.workspace.getRole();
        this.workspace = workspace;
        // Panggilan pertama selalu memuat (tamu: workspace null sejak awal, tetapi tetap butuh template populer).
        if (changed || !started) {
            started = true;
            // Data ruang kerja lama tidak boleh sempat tampil di ruang kerja baru.
            home.setValue(workspace == null ? Home.empty(false) : null);
            load();
        }
    }

    public void load() {
        loadRecommendations();
        Workspace current = workspace;
        int request = ++generation;
        if (current == null) {
            home.setValue(Home.empty(false));
            return;
        }
        boolean admin = current.getRole() != WorkspaceRole.EDITOR;
        executors.networkIO().execute(() -> {
            Resource<WebsitePage> page = websiteRepository.page(current.getId(), WebsiteQuery.initial(), 0);
            List<PublishRequest> requests = Collections.emptyList();
            if (admin && page.getStatus() == Resource.Status.SUCCESS && page.getData().offlineSince == null) {
                Resource<List<PublishRequest>> result = websiteRepository.openRequests(current.getId());
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    requests = result.getData();
                }
            }
            Home result;
            if (page.getStatus() == Resource.Status.SUCCESS) {
                List<Website> items = page.getData().items;
                List<Website> recent = items.subList(0, Math.min(RECENT_LIMIT, items.size()));
                result = new Home(recent, admin ? page.getData().counts.changed : 0, requests, !items.isEmpty());
            } else {
                result = Home.empty(null);
            }
            executors.mainThread().execute(() -> {
                if (request == generation) {
                    home.setValue(result);
                }
            });
        });
    }

    /**
     * "Template untuk {org}": terpopuler di kategori organisasi ruang kerja aktif; jika kurang dari {@link
     * Recommendations#LIMIT}, dilengkapi dari semua kategori tanpa duplikat. Ruang pribadi & tamu: semua kategori
     * (alur-pembuatan-website.md 6). Daftar lama tetap tampil sampai yang baru datang.
     */
    private void loadRecommendations() {
        Workspace current = workspace;
        String category = current == null || current.isPersonal() ? null : current.getCategory();
        executors.networkIO().execute(() -> {
            List<GalleryTemplateDto> result = category == null ? fetch(null) : withCategory(category);
            recommendedCategory = category;
            recommendations.postValue(result == null ? Collections.emptyList() : result);
        });
    }

    @Nullable
    private List<GalleryTemplateDto> withCategory(String category) {
        List<GalleryTemplateDto> own = fetch(category);
        if (own == null) {
            return null;
        }
        return Recommendations.needsFallback(own) ? Recommendations.merge(own, fetch(null)) : own;
    }

    /** null jika gagal (mis. offline). */
    private List<GalleryTemplateDto> fetch(String category) {
        Resource<GalleryPageDto> page = galleryRepository.templates(category, null, GalleryRepository.SORT_POPULAR,
                0, Recommendations.LIMIT);
        return page.getStatus() == Resource.Status.SUCCESS && page.getData().items != null ? page.getData().items : null;
    }
}
