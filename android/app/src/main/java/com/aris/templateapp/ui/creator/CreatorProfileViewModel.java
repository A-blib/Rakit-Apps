package com.aris.templateapp.ui.creator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.data.local.ProjectCounts;
import com.aris.templateapp.data.repository.ProjectRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Ringkasan di tab Profil: jumlah project & yang sudah diexport, dihitung dari Room di HP (bagian 5.1).
 * Data user memakai {@code CurrentUserViewModel}; beralih mode & keluar memakai {@code ProfileViewModel}.
 */
@HiltViewModel
public class CreatorProfileViewModel extends ViewModel {

    private final LiveData<ProjectCounts> counts;

    @Inject
    public CreatorProfileViewModel(ProjectRepository repository) {
        counts = repository.observeCounts("");
    }

    public LiveData<ProjectCounts> getCounts() {
        return counts;
    }
}
