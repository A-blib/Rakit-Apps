package com.aris.templateapp.notification;

/**
 * Jenis notifikasi provider (bagian 4.6). Disimpan sebagai nama konstanta (huruf besar) sesuai CHECK constraint V11,
 * jadi cukup memakai {@code @Enumerated(STRING)}.
 */
public enum NotificationType {
    TEMPLATE_CHECK_FAILED,
    TEMPLATE_CHECK_WARNING,
    TEMPLATE_PUBLISHED,
    TEMPLATE_DISABLED,
    // Fitur Upload: file lolos pengecekan, dan pengingat/penghapusan draft (alur-provider.md bagian 5.4).
    UPLOAD_CHECK_PASSED,
    DRAFT_EXPIRING,
    DRAFT_DELETED
}
