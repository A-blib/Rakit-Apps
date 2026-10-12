package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.organization.OrgLevel;
import com.aris.templateapp.organization.OrganizationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Form "Buat ruang organisasi" (bagian 5.1) atau "Untuk pribadi". Untuk {@code personal}, kategori, jenjang, dan
 * instansi diabaikan; untuk {@code organization} ketiganya wajib (dicek service karena bergantung pada type).
 */
public record CreateOrganizationRequest(
        @NotNull(message = "Jenis ruang kerja wajib dipilih")
        OrganizationType type,

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

        @Size(max = 60, message = "Jabatan maksimal 60 karakter")
        String positionTitle) {
}
