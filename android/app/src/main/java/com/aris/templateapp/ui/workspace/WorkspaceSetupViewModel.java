package com.aris.templateapp.ui.workspace;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.InvitationPreview;
import com.aris.templateapp.data.model.OrgCategories;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.remote.dto.CreateOrganizationRequestDto;
import com.aris.templateapp.data.repository.UserRepository;
import com.aris.templateapp.data.repository.WorkspaceRepository;

import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Tiga jalan masuk ruang kerja (revisi-fondasi-orgspace.md bagian 5): buat organisasi, gabung lewat kode, atau ruang
 * pribadi. Setelah ruang kerja ada, onboarding pembuat website diselesaikan bila belum (backend menolak onboarding
 * tanpa ruang kerja, jadi urutannya harus begini).
 */
@HiltViewModel
public class WorkspaceSetupViewModel extends ViewModel {

    static final String TYPE_ORGANIZATION = "organization";
    static final String TYPE_PERSONAL = "personal";

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final AppExecutors executors;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Resource<OrgCategories>> categories = new MutableLiveData<>();
    private final MutableLiveData<Map<OrganizationForm.Field, Integer>> formErrors = new MutableLiveData<>();
    private final MutableLiveData<InvitationPreview> preview = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<User>> done = new MutableLiveData<>();
    // Nama untuk langkah onboarding yang gagal setelah ruang kerja terbuat: "" = tanpa nama baru, null = tidak ada.
    private volatile String pendingFinishName;

    @Inject
    public WorkspaceSetupViewModel(WorkspaceRepository workspaceRepository, UserRepository userRepository,
                                   AppExecutors executors) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.executors = executors;
        executors.diskIO().execute(() -> currentUser.postValue(userRepository.getCachedUser()));
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Resource<OrgCategories>> getCategories() {
        return categories;
    }

    public LiveData<Map<OrganizationForm.Field, Integer>> getFormErrors() {
        return formErrors;
    }

    /** Hasil "Cek kode"; null = belum dicek atau kode diubah lagi. */
    public LiveData<InvitationPreview> getPreview() {
        return preview;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    /** Ruang kerja siap dan onboarding selesai: layar boleh pindah ke dashboard. */
    public LiveData<Event<User>> getDone() {
        return done;
    }

    public void loadCategories() {
        Resource<OrgCategories> current = categories.getValue();
        if (current != null && current.getStatus() != Resource.Status.ERROR) {
            return;
        }
        categories.setValue(Resource.loading());
        executors.networkIO().execute(() -> categories.postValue(workspaceRepository.categories()));
    }

    public void createOrganization(OrganizationForm form) {
        Map<OrganizationForm.Field, Integer> errors = form.validate();
        formErrors.setValue(errors);
        if (!errors.isEmpty()) {
            return;
        }
        CreateOrganizationRequestDto request = new CreateOrganizationRequestDto(TYPE_ORGANIZATION, form.name.trim());
        request.shortName = OrganizationForm.blankToNull(form.shortName);
        request.level = form.level;
        request.category = form.category;
        request.institutionName = form.institution.trim();
        request.city = OrganizationForm.blankToNull(form.city);
        request.periodStartYear = OrganizationForm.year(form.periodStart);
        request.periodEndYear = OrganizationForm.year(form.periodEnd);
        request.positionTitle = OrganizationForm.blankToNull(form.position);
        run(() -> workspaceRepository.create(request), null);
    }

    public void createPersonal(String displayName) {
        String name = displayName == null ? "" : displayName.trim();
        if (name.isEmpty()) {
            return;
        }
        // Ruang pribadi diberi nama tampilan user; ini yang tampil di app bar.
        run(() -> workspaceRepository.create(new CreateOrganizationRequestDto(TYPE_PERSONAL, name)), name);
    }

    public void checkCode(String code) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        loading.setValue(true);
        preview.setValue(null);
        executors.networkIO().execute(() -> {
            Resource<InvitationPreview> result = workspaceRepository.preview(code.trim());
            loading.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                preview.postValue(result.getData());
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }

    /** Kode diketik ulang: pratinjau lama tidak berlaku lagi. */
    public void clearPreview() {
        if (preview.getValue() != null) {
            preview.setValue(null);
        }
    }

    public void join(String code, String positionTitle) {
        run(() -> workspaceRepository.accept(code.trim(), OrganizationForm.blankToNull(positionTitle)), null);
    }

    /**
     * Membuat/bergabung ruang kerja, lalu menyelesaikan onboarding pembuat website jika user belum punya peran itu.
     *
     * @param displayName nama tampilan baru (ruang pribadi); null = pakai nama yang sudah ada
     */
    private void run(Supplier<Resource<Workspace>> request, String displayName) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        loading.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<Workspace> created = request.get();
            if (created.getStatus() != Resource.Status.SUCCESS) {
                loading.postValue(false);
                failure.postValue(new Event<>(created.getError()));
                return;
            }
            finish(displayName);
        });
    }

    /**
     * Tombol "Coba lagi" setelah ruang kerja sudah terbuat tetapi langkah onboarding gagal (mis. koneksi putus):
     * hanya langkah onboarding yang diulang, agar ruang kerja tidak terbuat dua kali.
     *
     * @return false jika tidak ada langkah yang tertunda (pemanggil mengulang aksi aslinya)
     */
    public boolean retryPendingFinish() {
        if (pendingFinishName == null) {
            return false;
        }
        String name = pendingFinishName;
        loading.setValue(true);
        executors.networkIO().execute(() -> finish(name.isEmpty() ? null : name));
        return true;
    }

    private void finish(String displayName) {
        User user = userRepository.getCachedUser();
        boolean needsOnboarding = user == null || !user.isOnboardingCompleted() || !user.hasRole(UserRole.CREATOR)
                || displayName != null;
        Resource<User> finished = needsOnboarding
                ? userRepository.completeCreatorOnboarding(displayName != null ? displayName
                : user == null ? "" : user.getDisplayName())
                : userRepository.fetchMe();
        loading.postValue(false);
        if (finished.getStatus() == Resource.Status.SUCCESS) {
            pendingFinishName = null;
            done.postValue(new Event<>(finished.getData()));
        } else if (needsOnboarding) {
            pendingFinishName = displayName == null ? "" : displayName;
            failure.postValue(new Event<>(finished.getError()));
        } else {
            // Hanya gagal menyegarkan data user: ruang kerja sudah siap, lanjut dengan data tersimpan.
            done.postValue(new Event<>(user));
        }
    }
}
