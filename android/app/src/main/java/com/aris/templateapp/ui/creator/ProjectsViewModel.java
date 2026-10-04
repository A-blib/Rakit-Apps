package com.aris.templateapp.ui.creator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.data.local.ProjectCounts;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectFilter;
import com.aris.templateapp.data.model.ProjectStatus;
import com.aris.templateapp.data.repository.ProjectRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Tab Project (alur-pembuatan-website.md bagian 4). Setiap kali filter berubah, {@code switchMap} mengganti query
 * Room yang dipantau; Room lalu otomatis mengirim daftar baru setiap kali tabel berubah (ganti nama, hapus, dst).
 */
@HiltViewModel
public class ProjectsViewModel extends ViewModel {

    private final ProjectRepository repository;
    private final MutableLiveData<ProjectFilter> filter = new MutableLiveData<>(ProjectFilter.DEFAULT);
    private final LiveData<List<ProjectEntity>> projects;
    private final LiveData<ProjectCounts> counts;
    private final LiveData<ProjectCounts> allCounts;

    @Inject
    public ProjectsViewModel(ProjectRepository repository) {
        this.repository = repository;
        projects = Transformations.switchMap(filter, repository::observe);
        // Jumlah di chip ikut pencarian; hanya ganti query Room jika kata kuncinya berubah.
        counts = Transformations.switchMap(Transformations.distinctUntilChanged(
                Transformations.map(filter, f -> f.query)), repository::observeCounts);
        // Tanpa filter apa pun: untuk membedakan "belum punya project" dengan "filter tidak menemukan apa-apa".
        allCounts = repository.observeCounts("");
    }

    public LiveData<ProjectFilter> getFilter() {
        return filter;
    }

    public LiveData<List<ProjectEntity>> getProjects() {
        return projects;
    }

    public LiveData<ProjectCounts> getCounts() {
        return counts;
    }

    public LiveData<ProjectCounts> getAllCounts() {
        return allCounts;
    }

    private ProjectFilter current() {
        ProjectFilter value = filter.getValue();
        return value == null ? ProjectFilter.DEFAULT : value;
    }

    private void update(ProjectFilter next) {
        if (!next.equals(current())) {
            filter.setValue(next);
        }
    }

    public void setStatus(ProjectStatus status) {
        update(current().withStatus(status));
    }

    public void setQuery(String query) {
        update(current().withQuery(query));
    }

    public void setSort(ProjectFilter.Sort sort) {
        update(current().withSort(sort));
    }

    public void clearFilters() {
        update(current().cleared());
    }

    public void rename(String id, String name) {
        repository.rename(id, name);
    }

    public void duplicate(String id, String copyName) {
        repository.duplicate(id, copyName);
    }

    public void delete(String id) {
        repository.delete(id);
    }
}
