package com.aris.templateapp.portal.dto;

/**
 * Data landing page (tanpa login).
 *
 * @param androidDownloadUrl null jika belum diatur (web menyembunyikan tombol unduh)
 */
public record PublicOverviewResponse(Stats stats, String androidDownloadUrl) {

    /** Angka pamer di landing page. Web boleh menyembunyikan angka yang masih kecil. */
    public record Stats(long organizations, long websitesOnline, long templates, long providers) {
    }
}
