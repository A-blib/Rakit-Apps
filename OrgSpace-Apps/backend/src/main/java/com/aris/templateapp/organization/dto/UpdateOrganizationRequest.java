package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.organization.OrgLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Edit Profil organisasi. Seluruh isian dikirim ulang dan menggantikan isi lama (kosong/null = dikosongkan),
 * sama seperti form edit lain di app. Kategori, jenjang, dan instansi diabaikan untuk ruang pribadi.
 * Media sosial boleh berupa link lengkap atau "@username"; server menormalkannya.
 */
public record UpdateOrganizationRequest(
        @NotBlank(message = "Nama organisasi wajib diisi")
        @Size(max = 100, message = "Nama maksimal 100 karakter")
        String name,

        @Size(max = 40, message = "Nama singkat maksimal 40 karakter")
        String shortName,

        OrgCategory category,

        OrgLevel level,

        @Size(max = 150, message = "Nama instansi maksimal 150 karakter")
        String institutionName,

        @Size(max = 100, message = "Kota maksimal 100 karakter")
        String city,

        @Min(value = 2000, message = "Tahun tidak valid") @Max(value = 2100, message = "Tahun tidak valid")
        Integer periodStartYear,

        @Min(value = 2000, message = "Tahun tidak valid") @Max(value = 2100, message = "Tahun tidak valid")
        Integer periodEndYear,

        @Size(max = 255, message = "Alamat maksimal 255 karakter")
        String address,

        @Email(message = "Format email tidak valid")
        @Size(max = 255, message = "Email maksimal 255 karakter")
        String email,

        @Size(max = 30, message = "Nomor WhatsApp terlalu panjang")
        String whatsapp,

        @Size(max = 200, message = "Isian terlalu panjang")
        String instagram,

        @Size(max = 200, message = "Isian terlalu panjang")
        String tiktok,

        @Size(max = 200, message = "Isian terlalu panjang")
        String youtube) {
}
