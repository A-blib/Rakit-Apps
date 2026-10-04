package com.aris.templateapp.ui.creator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.local.ProjectCounts;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.remote.dto.GalleryPageDto;
import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;
import com.aris.templateapp.data.repository.GalleryRepository;
import com.aris.templateapp.data.repository.ProjectRepository;
import com.aris.templateapp.data.repository.UserRepository;

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
    private final UserRepository userRepository;
    private final AppExecutors executors;

    private final LiveData<List<ProjectEntity>> recent;
    private final LiveData<ProjectCounts> counts;
    private final MutableLiveData<List<GalleryTemplateDto>> recommendations = new MutableLiveData<>();
    /** Kategori yang dipakai rekomendasi terakhir (untuk "Lihat semua"), null = semua kategori. */
    private String recommendedCategory;

    @Inject
    public CreatorHomeViewModel(ProjectRepository projectRepository, GalleryRepository galleryRepository,
                                UserRepository userRepository, AppExecutors executors) {
        this.galleryRepository = galleryRepository;
        this.userRepository = userRepository;
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
     * Login dengan tujuan website → terpopuler di kategori itu, dilengkapi dari semua kategori jika kurang dari 6.
     * Tamu / melewati onboarding → terpopuler dari semua kategori. Daftar lama tetap tampil sampai yang baru datang.
     */
    public void loadRecommendations() {
        executors.networkIO().execute(() -> {
            User user = userRepository.getCachedUser();
            String purpose = user == null ? null : user.getWebsitePurpose();
            List<GalleryTemplateDto> result;
            if (purpose == null) {
                result = fetch(null);
            } else {
                result = fetch(purpose);
                if (result != null && Recommendations.needsFallback(result)) {
                    List<GalleryTemplateDto> all = fetch(null);
                    result = Recommendations.merge(result, all);
                }
            }
            recommendedCategory = purpose;
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
