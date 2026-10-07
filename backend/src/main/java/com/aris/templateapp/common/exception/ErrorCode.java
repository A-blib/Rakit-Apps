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
    ALREADY_REPORTED(HttpStatus.CONFLICT, "Masalah ini sudah dilaporkan."),
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
