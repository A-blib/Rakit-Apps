package com.aris.templateapp.upload.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Isian Info template yang disimpan otomatis (alur-fitur-upload.md bagian 6). Field yang null tidak diubah, sehingga
 * app boleh mengirim satu isian saja setiap kali provider berhenti mengetik. Syarat lengkap (nama 3–60, deskripsi
 * 20–300, minimal 1 kata kunci) diperiksa app untuk tombol Lanjut dan diperiksa ulang server saat Kirim.
 *
 * @param category nilai kategori (sekolah, organisasi, umkm, instansi, pribadi, lainnya)
 */
public record DraftInfoRequest(
        @Size(max = 60, message = "Nama maksimal 60 karakter") String name,
        String category,
        @Size(max = 300, message = "Deskripsi maksimal 300 karakter") String description,
        @Size(max = 5, message = "Maksimal 5 kata kunci") List<String> keywords) {
}
