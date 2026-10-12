package com.aris.templateapp.ui.upload;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.DraftDto;
import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.aris.templateapp.data.remote.dto.UploadCheckDto;
import com.aris.templateapp.data.repository.UploadRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Langkah 6 Upload: ringkasan → Kirim → pengecekan akhir di server → tayang (alur-fitur-upload.md bagian 9). */
@HiltViewModel
public class SendViewModel extends ViewModel {

    enum Phase { LOADING, READY, SENDING, CHECKING, PUBLISHED, FAILED, ERROR }

    /** Keadaan layar Kirim. */
    static final class State {
        final Phase phase;
        final DraftDto draft;
        final MarkingDto marking;
        final UploadCheckDto check;
        final ApiError error;

        State(Phase phase, DraftDto draft, MarkingDto marking, UploadCheckDto check, ApiError error) {
            this.phase = phase;
            this.draft = draft;
            this.marking = marking;
            this.check = check;
            this.error = error;
        }

        State with(Phase next, UploadCheckDto newCheck, ApiError newError) {
            return new State(next, draft, marking, newCheck, newError);
        }
    }

    private static final long POLL_MS = 800;

    private final UploadRepository repository;
    private final AppExecutors executors;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<State> state = new MutableLiveData<>();
    private String templateId;

    @Inject
    public SendViewModel(UploadRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    LiveData<State> getState() {
        return state;
    }

    String templateId() {
        return templateId;
    }

    void start(String templateId) {
        if (templateId.equals(this.templateId)) {
            return;
        }
        this.templateId = templateId;
        load();
    }

    void load() {
        state.setValue(new State(Phase.LOADING, null, null, null, null));
        String id = templateId;
        executors.networkIO().execute(() -> {
            Resource<DraftDto> draft = repository.draft(id);
            Resource<MarkingDto> marking = repository.marking(id);
            Resource<UploadCheckDto> check = repository.check(id);
            if (draft.getStatus() != Resource.Status.SUCCESS || marking.getStatus() != Resource.Status.SUCCESS
                    || check.getStatus() != Resource.Status.SUCCESS) {
                ApiError error = draft.getError() != null ? draft.getError()
                        : marking.getError() != null ? marking.getError() : check.getError();
                state.postValue(new State(Phase.ERROR, null, null, null, error));
                return;
            }
            UploadCheckDto dto = check.getData();
            boolean lastFailed = dto.check != null && "failed".equals(dto.check.status);
            Phase phase = "checking".equals(dto.status) ? Phase.CHECKING
                    : "published".equals(dto.status) ? Phase.PUBLISHED
                    : lastFailed ? Phase.FAILED : Phase.READY;
            if (phase == Phase.READY) {
                repository.updateStep(id, 6);
            }
            state.postValue(new State(phase, draft.getData(), marking.getData(), check.getData(), null));
            if (phase == Phase.CHECKING) {
                handler.postDelayed(this::poll, POLL_MS);
            }
        });
    }

    void send() {
        State current = state.getValue();
        if (current == null) {
            return;
        }
        state.setValue(current.with(Phase.SENDING, current.check, null));
        String id = templateId;
        executors.networkIO().execute(() -> {
            Resource<Boolean> result = repository.submit(id, true);
            if (result.getStatus() != Resource.Status.SUCCESS) {
                state.postValue(current.with(Phase.READY, current.check, result.getError()));
                return;
            }
            state.postValue(current.with(Phase.CHECKING, current.check, null));
            handler.postDelayed(this::poll, POLL_MS);
        });
    }

    private void poll() {
        String id = templateId;
        executors.networkIO().execute(() -> {
            Resource<UploadCheckDto> result = repository.check(id);
            State current = state.getValue();
            if (current == null) {
                return;
            }
            if (result.getStatus() != Resource.Status.SUCCESS) {
                handler.postDelayed(this::poll, POLL_MS * 3);
                return;
            }
            UploadCheckDto check = result.getData();
            if ("checking".equals(check.status)) {
                state.postValue(current.with(Phase.CHECKING, check, null));
                handler.postDelayed(this::poll, POLL_MS);
            } else if ("published".equals(check.status)) {
                state.postValue(current.with(Phase.PUBLISHED, check, null));
            } else {
                // Kembali ke draft dengan daftar masalah (bagian 5.5).
                state.postValue(current.with(Phase.FAILED, check, null));
            }
        });
    }

    /** "Ini keliru? Laporkan" untuk error pengecekan akhir; daftar dimuat ulang agar tautannya berubah. */
    void report(String issueId, String reason) {
        executors.networkIO().execute(() -> {
            repository.report(issueId, reason);
            Resource<UploadCheckDto> refreshed = repository.check(templateId);
            State current = state.getValue();
            if (current != null && refreshed.getStatus() == Resource.Status.SUCCESS) {
                state.postValue(current.with(current.phase, refreshed.getData(), null));
            }
        });
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacksAndMessages(null);
    }
}
