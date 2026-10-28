package com.aris.templateapp.data.repository;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.mapper.WorkspaceMapper;
import com.aris.templateapp.data.model.Activity;
import com.aris.templateapp.data.model.Invitation;
import com.aris.templateapp.data.model.Member;
import com.aris.templateapp.data.model.Organization;
import com.aris.templateapp.data.model.PortalCredential;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.data.remote.api.OrganizationApi;
import com.aris.templateapp.data.remote.dto.CreateInvitationRequestDto;
import com.aris.templateapp.data.remote.dto.DeleteOrganizationRequestDto;
import com.aris.templateapp.data.remote.dto.MemberUpdateRequestDto;
import com.aris.templateapp.data.remote.dto.OrganizationDto;
import com.aris.templateapp.data.remote.dto.PortalCredentialRequestDto;
import com.aris.templateapp.data.remote.dto.TransferOwnershipRequestDto;
import com.aris.templateapp.data.remote.dto.UpdateOrganizationRequestDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Mengelola satu organisasi (revisi-fondasi-orgspace.md 4.4): profil, logo, anggota, undangan, serah terima,
 * keluar, hapus, riwayat aktivitas, dan kredensial Portal Pengurus. Semua butuh internet; hak tiap aksi tetap dijaga backend.
 * <p>
 * Aksi yang mengubah daftar ruang kerja (ganti nama/logo, keluar, hapus, serah terima) memuat ulang
 * {@link WorkspaceRepository} agar app bar dan pemilih ruang kerja ikut berubah. Method {@code @WorkerThread} wajib
 * dipanggil dari thread latar.
 */
@Singleton
public class OrganizationRepository {

    private static final MediaType PNG = MediaType.get("image/png");

    private final OrganizationApi api;
    private final ApiErrorParser errorParser;
    private final WorkspaceRepository workspaceRepository;

    @Inject
    public OrganizationRepository(OrganizationApi api, ApiErrorParser errorParser, WorkspaceRepository workspaceRepository) {
        this.api = api;
        this.errorParser = errorParser;
        this.workspaceRepository = workspaceRepository;
    }

    @WorkerThread
    public Resource<Organization> get(String id) {
        return execute(api.organization(id), WorkspaceMapper::toModel);
    }

    @WorkerThread
    public Resource<Organization> update(String id, UpdateOrganizationRequestDto request) {
        return refreshAfter(execute(api.update(id, request), WorkspaceMapper::toModel));
    }

    /** @param png isi file PNG yang sudah diperkecil di HP (ImageProcessor.logoPng) */
    @WorkerThread
    public Resource<Organization> uploadLogo(String id, byte[] png) {
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", "logo.png", RequestBody.create(png, PNG));
        return refreshAfter(execute(api.uploadLogo(id, part), WorkspaceMapper::toModel));
    }

    @WorkerThread
    public Resource<Boolean> delete(String id, String confirmName) {
        return refreshAfter(executeVoid(api.delete(id, new DeleteOrganizationRequestDto(confirmName))));
    }

    @WorkerThread
    public Resource<List<Member>> members(String id) {
        return execute(api.members(id), list -> map(list, WorkspaceMapper::toModel));
    }

    /** @param role null = tidak diubah; @param positionTitle null = tidak diubah, "" = dihapus */
    @WorkerThread
    public Resource<Member> updateMember(String id, String userId, @Nullable WorkspaceRole role,
                                         @Nullable String positionTitle) {
        return execute(api.updateMember(id, userId,
                new MemberUpdateRequestDto(role == null ? null : role.value(), positionTitle)), WorkspaceMapper::toModel);
    }

    @WorkerThread
    public Resource<Boolean> removeMember(String id, String userId) {
        return executeVoid(api.removeMember(id, userId));
    }

    @WorkerThread
    public Resource<Boolean> leave(String id) {
        return refreshAfter(executeVoid(api.leave(id)));
    }

    /** @param keepMeAs "admin", "editor", atau "leave" */
    @WorkerThread
    public Resource<Boolean> transferOwnership(String id, String userId, String keepMeAs) {
        try {
            // Balasan 200 (berisi profil) atau 204 (Pemilik lama keluar) sama-sama berarti berhasil.
            Response<OrganizationDto> response = api.transferOwnership(id,
                    new TransferOwnershipRequestDto(userId, keepMeAs)).execute();
            return refreshAfter(response.isSuccessful() ? Resource.success(true)
                    : Resource.error(errorParser.parse(response)));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    @WorkerThread
    public Resource<Invitation> createInvitation(String id, WorkspaceRole role) {
        return execute(api.createInvitation(id, new CreateInvitationRequestDto(role.value())), WorkspaceMapper::toModel);
    }

    @WorkerThread
    public Resource<List<Invitation>> invitations(String id) {
        return execute(api.invitations(id), list -> map(list, WorkspaceMapper::toModel));
    }

    @WorkerThread
    public Resource<Boolean> revokeInvitation(String id, String invitationId) {
        return executeVoid(api.revokeInvitation(id, invitationId));
    }

    /** Kredensial Portal yang sudah diatur (hanya Pemilik). */
    @WorkerThread
    public Resource<List<PortalCredential>> portalCredentials(String id) {
        return execute(api.portalCredentials(id), list -> map(list, WorkspaceMapper::toModel));
    }

    @WorkerThread
    public Resource<PortalCredential> savePortalCredential(String id, WorkspaceRole role, String username, String password) {
        return execute(api.savePortalCredential(id, role.value(), new PortalCredentialRequestDto(username, password)),
                WorkspaceMapper::toModel);
    }

    /** Satu halaman riwayat; {@code hasMore} ada di {@link ActivityPage}. */
    @WorkerThread
    public Resource<ActivityPage> activities(String id, int page) {
        return execute(api.activities(id, page),
                dto -> new ActivityPage(map(dto.items, WorkspaceMapper::toModel), dto.hasMore));
    }

    public static class ActivityPage {
        public final List<Activity> items;
        public final boolean hasMore;

        public ActivityPage(List<Activity> items, boolean hasMore) {
            this.items = items;
            this.hasMore = hasMore;
        }
    }

    private <T> Resource<T> refreshAfter(Resource<T> result) {
        if (result.getStatus() == Resource.Status.SUCCESS) {
            workspaceRepository.refresh(null);
        }
        return result;
    }

    private <D, T> Resource<T> execute(Call<D> call, Function<D, T> mapper) {
        try {
            Response<D> response = call.execute();
            D body = response.body();
            return response.isSuccessful() && body != null
                    ? Resource.success(mapper.apply(body)) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    private Resource<Boolean> executeVoid(Call<Void> call) {
        try {
            Response<Void> response = call.execute();
            return response.isSuccessful() ? Resource.success(true) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    private static <D, T> List<T> map(@Nullable List<D> list, Function<D, T> mapper) {
        List<T> result = new ArrayList<>();
        if (list != null) {
            for (D item : list) {
                result.add(mapper.apply(item));
            }
        }
        return result;
    }
}
