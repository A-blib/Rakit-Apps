package com.aris.templateapp.ui.upload;

import androidx.annotation.Nullable;

import com.aris.templateapp.core.upload.ZipQuickCheck;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.UploadCheckDto;

/** Keadaan layar langkah 2 (bagian 4, 5.4, 5.8). Satu objek baru untuk setiap perubahan. */
final class UploadCheckState {

    enum Phase {
        /** Membaca ZIP di HP (cek kilat). */
        LOCAL_CHECKING,
        /** Cek kilat gagal: file tidak dikirim sama sekali. */
        LOCAL_FAILED,
        /** File > 10 MB dan HP memakai data seluler: tanya dulu (bagian 5.7b). */
        CONFIRM_MOBILE_DATA,
        UPLOADING,
        /** Upload berhenti: sinyal putus terlalu lama, atau server menolak (mis. kuota draft penuh). */
        UPLOAD_ERROR,
        /** Server sedang mengecek; {@code stage} berisi tahap yang berjalan. */
        CHECKING,
        /** Server lolos; HP menjalankan halaman (tahap C). */
        DEVICE_CHECK,
        FAILED,
        PASSED,
        /** Gagal memuat hasil pengecekan (mis. offline saat membuka dari kartu). */
        LOAD_ERROR
    }

    final Phase phase;
    @Nullable
    final ZipQuickCheck.Result local;
    final long sent;
    final long total;
    /** > 0 jika sedang menunggu sinyal untuk melanjutkan upload. */
    final int waitingAttempt;
    @Nullable
    final ApiError error;
    @Nullable
    final String stage;
    @Nullable
    final UploadCheckDto result;

    private UploadCheckState(Phase phase, @Nullable ZipQuickCheck.Result local, long sent, long total, int waitingAttempt,
                             @Nullable ApiError error, @Nullable String stage, @Nullable UploadCheckDto result) {
        this.phase = phase;
        this.local = local;
        this.sent = sent;
        this.total = total;
        this.waitingAttempt = waitingAttempt;
        this.error = error;
        this.stage = stage;
        this.result = result;
    }

    static UploadCheckState of(Phase phase) {
        return new UploadCheckState(phase, null, 0, 0, 0, null, null, null);
    }

    static UploadCheckState localFailed(ZipQuickCheck.Result local) {
        return new UploadCheckState(Phase.LOCAL_FAILED, local, 0, 0, 0, null, null, null);
    }

    static UploadCheckState uploading(long sent, long total, int waitingAttempt) {
        return new UploadCheckState(Phase.UPLOADING, null, sent, total, waitingAttempt, null, null, null);
    }

    static UploadCheckState error(Phase phase, ApiError error) {
        return new UploadCheckState(phase, null, 0, 0, 0, error, null, null);
    }

    static UploadCheckState checking(@Nullable String stage, @Nullable UploadCheckDto result) {
        return new UploadCheckState(Phase.CHECKING, null, 0, 0, 0, null, stage, result);
    }

    static UploadCheckState result(Phase phase, UploadCheckDto result) {
        return new UploadCheckState(phase, null, 0, 0, 0, null, "done", result);
    }
}
