package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;
import jakarta.validation.constraints.Size;

/**
 * Ubah peran dan/atau jabatan anggota. Null = tidak diubah; jabatan berisi teks kosong = dihapus.
 * Peran {@code owner} tidak bisa diberikan di sini; pakai serah terima.
 */
public record MemberUpdateRequest(
        MemberRole role,
        @Size(max = 60, message = "Jabatan maksimal 60 karakter") String positionTitle) {
}
