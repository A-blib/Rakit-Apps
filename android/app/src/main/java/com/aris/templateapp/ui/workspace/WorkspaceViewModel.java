package com.aris.templateapp.ui.workspace;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.repository.UserRepository;
import com.aris.templateapp.data.repository.WorkspaceRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Ruang kerja aktif untuk Dashboard Pembuat Website. Saat dashboard tampil, daftar ruang kerja dimuat ulang dari
 * backend; jika user login ternyata belum punya ruang kerja sama sekali, {@link #getNeedsWorkspace()} memberi tanda
 * agar layar pilihan ruang kerja dibuka (revisi-fondasi-orgspace.md bagian 5, user lama tanpa ruang kerja).
 */
@HiltViewModel
public class WorkspaceViewModel extends ViewModel {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final AppExecutors executors;
    private final MutableLiveData<Event<Boolean>> needsWorkspace = new MutableLiveData<>();
    private boolean refreshing;

    @Inject
    public WorkspaceViewModel(WorkspaceRepository workspaceRepository, UserRepository userRepository,
                              AppExecutors executors) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.executors = executors;
    }

    /** Null untuk tamu atau user yang belum punya ruang kerja. */
    public LiveData<Workspace> getActive() {
        return workspaceRepository.getActive();
    }

    public LiveData<List<Workspace>> getWorkspaces() {
        return workspaceRepository.getWorkspaces();
    }

    /** Pindah ruang kerja aktif; semua tab yang mengamati {@link #getActive()} ikut dimuat ulang. */
    public void select(Workspace workspace) {
        workspaceRepository.select(workspace);
    }

    public LiveData<Event<Boolean>> getNeedsWorkspace() {
        return needsWorkspace;
    }

    /** Dipanggil setiap dashboard tampil. Offline: diam saja, app bar memakai daftar yang tersimpan di HP. */
    public void refresh() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        executors.networkIO().execute(() -> {
            User user = userRepository.getCachedUser();
            if (user != null) {
                Resource<List<Workspace>> result = workspaceRepository.refresh(user.getLastOrganizationId());
                if (result.getStatus() == Resource.Status.SUCCESS && result.getData().isEmpty()) {
                    needsWorkspace.postValue(new Event<>(true));
                }
            }
            refreshing = false;
        });
    }
}
