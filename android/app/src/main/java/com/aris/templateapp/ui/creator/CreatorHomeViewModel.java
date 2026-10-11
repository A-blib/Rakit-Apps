package com.aris.templateapp.ui.creator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.local.ProjectCounts;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.remote.dto.GalleryPageDto;
import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;
import com.aris.templateapp.data.repository.GalleryRepository;
import com.aris.templateapp.data.repository.ProjectRepository;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Beranda pembuat website: project terakhir (Room) + "Template untuk anda" (backend). */
@HiltViewModel
public class CreatorHomeViewModel extends ViewModel {

    /** 1 kartu besar + maksimal 3 kartu kecil (bagian 3.1). */
    static final int RECENT_LIMIT = 4;

    private final GalleryRepository galleryRepository;
    private final AppExecutors executors;

    private final LiveData<List<ProjectEntity>> recent;
    private final LiveData<ProjectCounts> counts;
    private final MutableLiveData<List<GalleryTemplateDto>> recommendations = new MutableLiveData<>();
    /** Kategori yang dipakai rekomendasi terakhir (untuk "Lihat semua"), null = semua kategori. */
    private String recommendedCategory;

    @Inject
    public CreatorHomeViewModel(ProjectRepository projectRepository, GalleryRepository galleryRepository,
                                AppExecutors executors) {
        this.galleryRepository = galleryRepository;
        this.executors = executors;
        recent = projectRepository.observeRecent(RECENT_LIMIT);
        counts = projectRepository.observeCounts("");
    }

    public LiveData<List<ProjectEntity>> getRecent() {
        return recent;
    }

    public LiveData<ProjectCounts> getCounts() {
        return counts;
    }

    /** Daftar kosong = sembunyikan bagian ini (offline atau galeri kosong, bagian 6.2). */
    public LiveData<List<GalleryTemplateDto>> getRecommendations() {
        return recommendations;
    }

    public String getRecommendedCategory() {
        return recommendedCategory;
    }

    /**
     * Terpopuler dari semua kategori. Rekomendasi per kategori organisasi ruang kerja dipasang bersama galeri
     * kategori baru (alur-pembuatan-website.md fase W4). Daftar lama tetap tampil sampai yang baru datang.
     */
    public void loadRecommendations() {
        executors.networkIO().execute(() -> {
            List<GalleryTemplateDto> result = fetch(null);
            recommendedCategory = null;
            recommendations.postValue(result == null ? Collections.emptyList() : result);
        });
    }

    /** null jika gagal (mis. offline). */
    private List<GalleryTemplateDto> fetch(String category) {
        Resource<GalleryPageDto> page = galleryRepository.templates(category, null, GalleryRepository.SORT_POPULAR,
                0, Recommendations.LIMIT);
        return page.getStatus() == Resource.Status.SUCCESS && page.getData().items != null ? page.getData().items : null;
    }
}
