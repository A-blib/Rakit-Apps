package com.aris.templateapp.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Daftar kode error yang dikirim ke app Android. App memilih pesan untuk user berdasarkan {@code code},
 * jadi nama konstanta di sini adalah kontrak API dan tidak boleh diganti sembarangan.
 */
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Data tidak valid."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email atau password salah."),
    USE_SOCIAL_LOGIN(HttpStatus.CONFLICT, "Akun ini terdaftar dengan metode login lain."),
    EMAIL_ALREADY_USED(HttpStatus.CONFLICT, "Email sudah terdaftar."),
    SOCIAL_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "Verifikasi akun Google/GitHub gagal."),
    ACCOUNT_LINK_REQUIRED(HttpStatus.CONFLICT, "Email ini sudah terdaftar dengan metode lain."),
    LINK_USER_MISMATCH(HttpStatus.FORBIDDEN, "Akun yang dipakai masuk berbeda dengan akun yang akan disambungkan."),
    LINK_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Token penyambungan tidak valid atau kedaluwarsa."),
    IDENTITY_IN_USE(HttpStatus.CONFLICT, "Metode login ini sudah dipakai akun lain."),
    LAST_IDENTITY(HttpStatus.CONFLICT, "Metode login terakhir tidak boleh dilepas."),
    TICKET_INVALID(HttpStatus.BAD_REQUEST, "Tiket tidak valid atau kedaluwarsa."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Sesi berakhir. Silakan masuk lagi."),
    PROVIDER_PROFILE_EXISTS(HttpStatus.CONFLICT, "Profil provider sudah ada."),
    MODE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "Mode ini tidak tersedia untuk akunmu."),
    PROVIDER_REQUIRED(HttpStatus.FORBIDDEN, "Fitur ini khusus penyedia template."),
    PROVIDER_SUSPENDED(HttpStatus.FORBIDDEN, "Mode provider dinonaktifkan. Hubungi admin untuk informasi lebih lanjut."),
    // Fitur Upload (alur-fitur-upload.md)
    FILE_NOT_ZIP(HttpStatus.BAD_REQUEST, "Upload template dalam format ZIP."),
    RAR_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "Format RAR belum didukung. Simpan ulang template sebagai ZIP."),
    UPLOAD_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "Ukuran ZIP melebihi batas."),
    DRAFT_LIMIT_REACHED(HttpStatus.CONFLICT, "Selesaikan atau hapus draft dulu (maks 5)."),
    UPLOAD_OFFSET_MISMATCH(HttpStatus.CONFLICT, "Posisi potongan upload tidak cocok. Tanyakan posisi terakhir lalu lanjutkan."),
    UPLOAD_INCOMPLETE(HttpStatus.CONFLICT, "Upload belum selesai."),
    TEMPLATE_NOT_EDITABLE(HttpStatus.CONFLICT, "Template ini tidak bisa diubah pada status sekarang."),
    IMAGE_INVALID(HttpStatus.BAD_REQUEST, "Gambar harus JPG, PNG, atau WebP, maksimal 1 MB."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "Masalah ini sudah dilaporkan."),
    // Folder di "Template Anda"
    FOLDER_NAME_TAKEN(HttpStatus.CONFLICT, "Nama folder ini sudah dipakai. Pilih nama lain."),
    FOLDER_LIMIT_REACHED(HttpStatus.CONFLICT, "Folder sudah mencapai batas (maks 100)."),
    // Ruang kerja OrgSpace (revisi-fondasi-orgspace.md bagian 4.4)
    ORG_NOT_FOUND(HttpStatus.NOT_FOUND, "Ruang kerja ini tidak ditemukan."),
    NOT_ORG_MEMBER(HttpStatus.FORBIDDEN, "Kamu bukan anggota ruang kerja ini."),
    ROLE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "Peranmu belum bisa melakukan ini."),
    LAST_OWNER(HttpStatus.CONFLICT, "Jadikan anggota lain Pemilik dulu sebelum keluar."),
    PERSONAL_SPACE_EXISTS(HttpStatus.CONFLICT, "Kamu sudah punya ruang pribadi."),
    CATEGORY_LEVEL_MISMATCH(HttpStatus.BAD_REQUEST, "Kategori ini tidak tersedia untuk jenjang yang dipilih."),
    INVITATION_INVALID(HttpStatus.BAD_REQUEST, "Kode undangan ini sudah tidak berlaku. Minta kode baru ke pengurus, ya."),
    ALREADY_MEMBER(HttpStatus.CONFLICT, "Kamu sudah jadi anggota organisasi ini."),
    ORG_MEMBER_LIMIT(HttpStatus.CONFLICT, "Anggota organisasi ini sudah penuh."),
    ORG_HAS_ONLINE_SITES(HttpStatus.CONFLICT, "Matikan dulu semua website yang masih online."),
    CONFIRM_NAME_MISMATCH(HttpStatus.BAD_REQUEST, "Nama yang diketik belum sama."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "Website tidak ditemukan."),
    PROJECT_LOCKED(HttpStatus.CONFLICT, "Website ini sedang diedit anggota lain."),
    LOCK_LOST(HttpStatus.CONFLICT, "Kunci edit sudah diambil anggota lain."),
    VERSION_CONFLICT(HttpStatus.CONFLICT, "Isi website sudah berubah di HP lain."),
    STORAGE_LIMIT_REACHED(HttpStatus.CONFLICT, "Penyimpanan gambar ruang kerja ini sudah penuh."),
    ASSET_INVALID(HttpStatus.BAD_REQUEST, "Gambar harus WebP, JPEG, atau PNG dan tidak lebih dari batas ukuran."),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "Template tidak ditemukan atau sudah tidak tayang."),
    BACKUP_NOT_FOUND(HttpStatus.NOT_FOUND, "Cadangan tidak ditemukan."),
    LOGO_INVALID(HttpStatus.BAD_REQUEST, "Logo harus gambar PNG atau JPG."),
    AVATAR_INVALID(HttpStatus.BAD_REQUEST, "Foto profil harus gambar PNG atau JPG."),
    ONBOARDING_NEEDS_SPACE(HttpStatus.CONFLICT, "Buat atau gabung ruang kerja dulu."),
    TOO_MANY_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "Terlalu banyak percobaan. Tunggu sebentar, lalu coba lagi."),
    // Portal web: admin, kritik & saran, dokumentasi (docs/rancangan/portal-web.md)
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "Halaman ini khusus tim internal OrgSpace."),
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "Kritik & saran tidak ditemukan."),
    FEEDBACK_CLOSED(HttpStatus.CONFLICT, "Percakapan ini sudah ditutup. Kirim kritik & saran baru, ya."),
    DOC_NOT_FOUND(HttpStatus.NOT_FOUND, "Artikel tidak ditemukan."),
    DOC_SLUG_TAKEN(HttpStatus.CONFLICT, "Alamat artikel (slug) sudah dipakai artikel lain."),
    // Publish & Hosting website (docs/rancangan/alur-publish-website.md bagian 7.3)
    SITE_NOT_PUBLISHED(HttpStatus.NOT_FOUND, "Website belum diterbitkan atau sedang tidak aktif."),
    RELEASE_NOT_FOUND(HttpStatus.NOT_FOUND, "Versi rilis website tidak ditemukan."),
    SITE_LIMIT_REACHED(HttpStatus.CONFLICT, "Batas maksimal website per ruang kerja telah tercapai."),
    SLUG_TAKEN(HttpStatus.CONFLICT, "Nama subdomain sudah digunakan."),
    SLUG_INVALID(HttpStatus.BAD_REQUEST, "Format subdomain tidak valid."),
    SLUG_RESERVED(HttpStatus.BAD_REQUEST, "Nama subdomain dicadangkan oleh sistem."),
    SITE_CONTENT_INVALID(HttpStatus.BAD_REQUEST, "Konten website tidak valid."),
    OUTPUT_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "Ukuran website melebihi batas yang diizinkan."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Silakan masuk terlebih dahulu."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Data tidak ditemukan."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Terjadi kesalahan di server.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
