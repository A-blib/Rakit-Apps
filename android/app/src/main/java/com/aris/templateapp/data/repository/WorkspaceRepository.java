package com.aris.templateapp.data.repository;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.mapper.WorkspaceMapper;
import com.aris.templateapp.data.model.InvitationPreview;
import com.aris.templateapp.data.model.OrgCategories;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.remote.api.OrganizationApi;
import com.aris.templateapp.data.remote.dto.CategoriesDto;
import com.aris.templateapp.data.remote.dto.CreateOrganizationRequestDto;
import com.aris.templateapp.data.remote.dto.InvitationCodeRequestDto;
import com.aris.templateapp.data.remote.dto.InvitationPreviewDto;
import com.aris.templateapp.data.remote.dto.LastOrganizationRequestDto;
import com.aris.templateapp.data.remote.dto.OrganizationDto;
import com.aris.templateapp.data.remote.dto.OrganizationSummaryDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Ruang kerja milik user (revisi-fondasi-orgspace.md bagian 3): daftar, ruang kerja aktif, membuat, dan bergabung.
 * <p>
 * Daftar dan ruang kerja aktif disimpan di HP ({@link SessionStore}) agar app bar tetap benar saat offline, lalu
 * dibagikan ke semua layar lewat LiveData: saat user berpindah ruang kerja, semua tab yang mengamati ikut berganti.
 * Method {@code @WorkerThread} wajib dipanggil dari thread latar.
 */
@Singleton
public class WorkspaceRepository {

    private final OrganizationApi api;
    private final SessionStore sessionStore;
    private final ApiErrorParser errorParser;
    private final AppExecutors executors;

    private final MutableLiveData<List<Workspace>> workspaces = new MutableLiveData<>();
    private final MutableLiveData<Workspace> active = new MutableLiveData<>();
    // Kategori jarang berubah; cukup diambil sekali per app dibuka.
    @Nullable private OrgCategories categories;

    @Inject
    public WorkspaceRepository(OrganizationApi api, SessionStore sessionStore, ApiErrorParser errorParser,
                               AppExecutors executors) {
        this.api = api;
        this.sessionStore = sessionStore;
        this.errorParser = errorParser;
        this.executors = executors;
        executors.diskIO().execute(() -> publish(sessionStore.getWorkspaces(), null));
    }

    public LiveData<List<Workspace>> getWorkspaces() {
        return workspaces;
    }

    /** Ruang kerja yang sedang dibuka; null jika user belum punya ruang kerja (atau tamu). */
    public LiveData<Workspace> getActive() {
        return active;
    }

    /**
     * Memuat ulang daftar dari backend. Ruang kerja aktif dipertahankan jika masih ada; jika tidak, dipakai
     * {@code serverLastId} (ruang kerja terakhir menurut server, mis. dari HP lain), lalu yang pertama.
     */
    @WorkerThread
    public Resource<List<Workspace>> refresh(@Nullable String serverLastId) {
        try {
            Response<List<OrganizationSummaryDto>> response = api.organizations().execute();
            List<OrganizationSummaryDto> body = response.body();
            if (!response.isSuccessful() || body == null) {
                return Resource.error(errorParser.parse(response));
            }
            sessionStore.saveWorkspaces(body);
            return Resource.success(publish(body, serverLastId));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    @WorkerThread
    public Resource<Workspace> create(CreateOrganizationRequestDto request) {
        return executeJoin(api.create(request));
    }

    @WorkerThread
    public Resource<InvitationPreview> preview(String code) {
        try {
            Response<InvitationPreviewDto> response = api.previewInvitation(new InvitationCodeRequestDto(code, null)).execute();
            InvitationPreviewDto body = response.body();
            return response.isSuccessful() && body != null
                    ? Resource.success(WorkspaceMapper.toModel(body)) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    @WorkerThread
    public Resource<Workspace> accept(String code, @Nullable String positionTitle) {
        return executeJoin(api.acceptInvitation(new InvitationCodeRequestDto(code, positionTitle)));
    }

    @WorkerThread
    public Resource<OrgCategories> categories() {
        if (categories != null) {
            return Resource.success(categories);
        }
        try {
            Response<CategoriesDto> response = api.categories().execute();
            CategoriesDto body = response.body();
            if (!response.isSuccessful() || body == null) {
                return Resource.error(errorParser.parse(response));
            }
            categories = WorkspaceMapper.toModel(body);
            return Resource.success(categories);
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    /**
     * Pindah ruang kerja. Langsung berlaku di HP; server diberi tahu di latar (gagal karena offline tidak masalah,
     * pilihan tetap tersimpan di HP dan terkirim lagi lain kali).
     */
    public void select(Workspace workspace) {
        sessionStore.saveActiveWorkspaceId(workspace.getId());
        active.setValue(workspace);
        executors.networkIO().execute(() -> {
            try {
                api.setLastOrganization(new LastOrganizationRequestDto(workspace.getId())).execute();
            } catch (IOException ignored) {
                // Offline: pilihan di HP sudah tersimpan.
            }
        });
    }

    /** Dipanggil saat keluar akun. Data di HP sudah dihapus SessionStore.clearUser(). */
    public void clear() {
        workspaces.postValue(new ArrayList<>());
        active.postValue(null);
    }

    /** Membuat/bergabung: ruang kerja baru langsung ditambahkan ke daftar dan menjadi ruang kerja aktif. */
    private Resource<Workspace> executeJoin(Call<OrganizationDto> call) {
        try {
            Response<OrganizationDto> response = call.execute();
            OrganizationDto body = response.body();
            if (!response.isSuccessful() || body == null) {
                return Resource.error(errorParser.parse(response));
            }
            List<OrganizationSummaryDto> list = sessionStore.getWorkspaces();
            list.removeIf(item -> item.id.equals(body.id));
            list.add(WorkspaceMapper.toSummary(body));
            sessionStore.saveWorkspaces(list);
            sessionStore.saveActiveWorkspaceId(body.id);
            publish(list, body.id);
            return Resource.success(WorkspaceMapper.toModel(WorkspaceMapper.toSummary(body)));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    private List<Workspace> publish(List<OrganizationSummaryDto> dtos, @Nullable String fallbackId) {
        List<Workspace> list = WorkspaceMapper.toModels(dtos);
        Workspace chosen = find(list, sessionStore.getActiveWorkspaceId());
        if (chosen == null) {
            chosen = find(list, fallbackId);
        }
        if (chosen == null && !list.isEmpty()) {
            chosen = list.get(0);
        }
        if (chosen != null) {
            sessionStore.saveActiveWorkspaceId(chosen.getId());
        }
        workspaces.postValue(list);
        active.postValue(chosen);
        return list;
    }

    @Nullable
    private static Workspace find(List<Workspace> list, @Nullable String id) {
        if (id == null) {
            return null;
        }
        for (Workspace workspace : list) {
            if (workspace.getId().equals(id)) {
                return workspace;
            }
        }
        return null;
    }
}
