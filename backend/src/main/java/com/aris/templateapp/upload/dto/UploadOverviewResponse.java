package com.aris.templateapp.upload.dto;

import com.aris.templateapp.upload.check.CheckStage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Halaman awal tab Upload (alur-fitur-upload.md bagian 3.1). Kondisi A (kosong) jika ketiga daftar kosong.
 *
 * @param draftCount  draft + upload yang sedang dicek (dihitung ke kuota)
 * @param canStartNew false jika kuota draft penuh; tombol upload baru dinonaktifkan
 */
public record UploadOverviewResponse(List<NeedsFix> needsFix, List<Checking> checking, List<Draft> drafts,
                                     int draftCount, int draftLimit, boolean canStartNew) {

    /** Upload yang gagal pengecekan ("Perlu diperbaiki"). */
    public record NeedsFix(UUID templateId, String fileName, int errorCount, int warningCount, Instant updatedAt) {
    }

    /** Upload yang sedang dicek server. */
    public record Checking(UUID templateId, String fileName, CheckStage stage) {
    }

    /**
     * Draft yang bisa dilanjutkan.
     *
     * @param deleteAt     kapan draft dihapus otomatis jika tidak disentuh
     * @param expiringSoon true jika {@code deleteAt} tinggal beberapa hari ("⚠ Dihapus dalam 5 hari")
     */
    public record Draft(UUID templateId, String name, String thumbnailUrl, int wizardStep, Instant updatedAt,
                        Instant deleteAt, boolean expiringSoon) {
    }
}
