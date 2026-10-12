package com.aris.templateapp.ui.organization;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.Invitation;
import com.aris.templateapp.data.model.Member;
import com.aris.templateapp.data.model.Organization;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.data.repository.OrganizationRepository;
import com.aris.templateapp.data.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Anggota, undangan, dan serah terima (revisi-fondasi-orgspace.md 3.1 & 4.4). Dipakai layar Anggota, sheet Undang
 * anggota (lewat ViewModel milik layar Anggota), dan layar Serah terima.
 */
@HiltViewModel
public class MembersViewModel extends ViewModel {

    /** Semua data layar Anggota sekaligus, agar tombol yang tampil selalu sesuai peran terbaru. */
    public static class State {
        public final Organization organization;
        public final List<Member> members;
        public final List<Invitation> invitations;
        @Nullable public final String myUserId;

        State(Organization organization, List<Member> members, List<Invitation> invitations, @Nullable String myUserId) {
            this.organization = organization;
            this.members = members;
            this.invitations = invitations;
            this.myUserId = myUserId;
        }
    }

    private final OrganizationRepository repository;
    private final UserRepository userRepository;
    private final AppExecutors executors;

    private final MutableLiveData<Resource<State>> state = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> done = new MutableLiveData<>();
    private final MutableLiveData<Event<Invitation>> invitationCreated = new MutableLiveData<>();
    private String organizationId;

    @Inject
    public MembersViewModel(OrganizationRepository repository, UserRepository userRepository, AppExecutors executors) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.executors = executors;
    }

    public void start(String id) {
        if (organizationId == null) {
            organizationId = id;
            load();
        }
    }

    public LiveData<Resource<State>> getState() {
        return state;
    }

    public LiveData<Boolean> isBusy() {
        return busy;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    /** Aksi selesai; isinya nama anggota yang terkena aksi (untuk pesan), boleh kosong. */
    public LiveData<Event<String>> getDone() {
        return done;
    }

    /** Kode undangan baru (hanya sekali ini kodenya diketahui). */
    public LiveData<Event<Invitation>> getInvitationCreated() {
        return invitationCreated;
    }

    public void load() {
        if (state.getValue() == null || state.getValue().getStatus() == Resource.Status.ERROR) {
            state.setValue(Resource.loading());
        }
        executors.networkIO().execute(() -> state.postValue(fetch()));
    }

    public void changeRole(Member member, WorkspaceRole role) {
        run(() -> toBoolean(repository.updateMember(organizationId, member.userId, role, null)), member.displayName);
    }

    /** @param title kosong = jabatan dihapus */
    public void changePosition(Member member, String title) {
        run(() -> toBoolean(repository.updateMember(organizationId, member.userId, null, title.trim())),
                member.displayName);
    }

    public void remove(Member member) {
        run(() -> repository.removeMember(organizationId, member.userId), member.displayName);
    }

    public void revoke(Invitation invitation) {
        run(() -> repository.revokeInvitation(organizationId, invitation.id), "");
    }

    /** @param keepMeAs "admin", "editor", atau "leave" */
    public void transfer(Member receiver, String keepMeAs) {
        run(() -> repository.transferOwnership(organizationId, receiver.userId, keepMeAs), receiver.displayName);
    }

    public void createInvitation(WorkspaceRole role) {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<Invitation> result = repository.createInvitation(organizationId, role);
            busy.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                invitationCreated.postValue(new Event<>(result.getData()));
                state.postValue(fetch());
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }

    private Resource<State> fetch() {
        Resource<Organization> organization = repository.get(organizationId);
        if (organization.getStatus() != Resource.Status.SUCCESS) {
            return Resource.error(organization.getError());
        }
        Resource<List<Member>> members = repository.members(organizationId);
        if (members.getStatus() != Resource.Status.SUCCESS) {
            return Resource.error(members.getError());
        }
        // Daftar undangan hanya untuk Admin ke atas; Editor tidak memintanya (akan ditolak backend).
        List<Invitation> invitations = Collections.emptyList();
        if (organization.getData().canManage() && !organization.getData().personal) {
            Resource<List<Invitation>> result = repository.invitations(organizationId);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                invitations = result.getData();
            }
        }
        User me = userRepository.getCachedUser();
        return Resource.success(new State(organization.getData(), members.getData(), invitations,
                me == null ? null : me.getId()));
    }

    private void run(Supplier<Resource<Boolean>> action, String name) {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<Boolean> result = action.get();
            busy.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                done.postValue(new Event<>(name));
                state.postValue(fetch());
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }

    private static <T> Resource<Boolean> toBoolean(Resource<T> result) {
        return result.getStatus() == Resource.Status.SUCCESS ? Resource.success(true) : Resource.error(result.getError());
    }
}
