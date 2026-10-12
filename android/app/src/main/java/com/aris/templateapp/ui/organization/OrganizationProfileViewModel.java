package com.aris.templateapp.ui.organization;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.R;
import com.aris.templateapp.core.template.ImageProcessor;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.OrgCategories;
import com.aris.templateapp.data.model.Organization;
import com.aris.templateapp.data.remote.dto.UpdateOrganizationRequestDto;
import com.aris.templateapp.data.repository.OrganizationRepository;
import com.aris.templateapp.data.repository.WorkspaceRepository;
import com.aris.templateapp.ui.workspace.OrganizationForm;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Profil organisasi (revisi-fondasi-orgspace.md 4.4 & K10): dilihat semua anggota, diubah Admin/Pemilik.
 * Profil ini nantinya mengisi otomatis kolom template yang dihubungkan kreator.
 */
@HiltViewModel
public class OrganizationProfileViewModel extends ViewModel {

    /** Isian kontak yang tidak ikut OrganizationForm. */
    public static class Contact {
        public String address;
        public String email;
        public String whatsapp;
        public String instagram;
        public String tiktok;
        public String youtube;
    }

    public enum Saved { PROFILE, LOGO }

    private final OrganizationRepository organizationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ImageProcessor imageProcessor;
    private final AppExecutors executors;

    private final MutableLiveData<Resource<Organization>> organization = new MutableLiveData<>();
    private final MutableLiveData<OrgCategories> categories = new MutableLiveData<>();
    private final MutableLiveData<Map<OrganizationForm.Field, Integer>> formErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> imageError = new MutableLiveData<>();
    private final MutableLiveData<Event<Saved>> saved = new MutableLiveData<>();
    private String organizationId;

    @Inject
    public OrganizationProfileViewModel(OrganizationRepository organizationRepository,
                                        WorkspaceRepository workspaceRepository, ImageProcessor imageProcessor,
                                        AppExecutors executors) {
        this.organizationRepository = organizationRepository;
        this.workspaceRepository = workspaceRepository;
        this.imageProcessor = imageProcessor;
        this.executors = executors;
    }

    public void start(String id) {
        if (organizationId == null) {
            organizationId = id;
            load();
        }
    }

    /** Profil dan daftar kategori dimuat bersamaan; layar baru tampil setelah keduanya siap. */
    public void load() {
        organization.setValue(Resource.loading());
        executors.networkIO().execute(() -> {
            Resource<OrgCategories> categoryResult = workspaceRepository.categories();
            if (categoryResult.getStatus() != Resource.Status.SUCCESS) {
                organization.postValue(Resource.error(categoryResult.getError()));
                return;
            }
            categories.postValue(categoryResult.getData());
            organization.postValue(organizationRepository.get(organizationId));
        });
    }

    public LiveData<Resource<Organization>> getOrganization() {
        return organization;
    }

    public LiveData<OrgCategories> getCategories() {
        return categories;
    }

    public LiveData<Map<OrganizationForm.Field, Integer>> getFormErrors() {
        return formErrors;
    }

    public LiveData<Boolean> isBusy() {
        return busy;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    /** Pesan (id string) saat gambar yang dipilih tidak bisa dibaca di HP. */
    public LiveData<Event<Integer>> getImageError() {
        return imageError;
    }

    public LiveData<Event<Saved>> getSaved() {
        return saved;
    }

    public void save(OrganizationForm form, Contact contact) {
        Map<OrganizationForm.Field, Integer> errors = form.validate();
        formErrors.setValue(errors);
        if (!errors.isEmpty() || Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        UpdateOrganizationRequestDto request = new UpdateOrganizationRequestDto();
        request.name = form.name.trim();
        request.shortName = OrganizationForm.blankToNull(form.shortName);
        request.level = form.personal ? null : form.level;
        request.category = form.personal ? null : form.category;
        request.institutionName = form.personal ? null : OrganizationForm.blankToNull(form.institution);
        request.city = OrganizationForm.blankToNull(form.city);
        request.periodStartYear = OrganizationForm.year(form.periodStart);
        request.periodEndYear = OrganizationForm.year(form.periodEnd);
        request.address = OrganizationForm.blankToNull(contact.address);
        request.email = OrganizationForm.blankToNull(contact.email);
        request.whatsapp = OrganizationForm.blankToNull(contact.whatsapp);
        request.instagram = OrganizationForm.blankToNull(contact.instagram);
        request.tiktok = OrganizationForm.blankToNull(contact.tiktok);
        request.youtube = OrganizationForm.blankToNull(contact.youtube);
        busy.setValue(true);
        executors.networkIO().execute(() -> finish(organizationRepository.update(organizationId, request), Saved.PROFILE));
    }

    /** Foto dari Photo Picker → dipotong persegi & diperkecil di HP → diunggah. */
    public void uploadLogo(Uri uri) {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            byte[] png;
            try {
                png = imageProcessor.logoPng(uri);
            } catch (IOException | SecurityException e) {
                png = null;
            }
            if (png == null) {
                busy.postValue(false);
                imageError.postValue(new Event<>(R.string.error_image_unreadable));
                return;
            }
            finish(organizationRepository.uploadLogo(organizationId, png), Saved.LOGO);
        });
    }

    private void finish(Resource<Organization> result, Saved what) {
        busy.postValue(false);
        if (result.getStatus() == Resource.Status.SUCCESS) {
            organization.postValue(result);
            saved.postValue(new Event<>(what));
        } else {
            failure.postValue(new Event<>(result.getError()));
        }
    }
}
