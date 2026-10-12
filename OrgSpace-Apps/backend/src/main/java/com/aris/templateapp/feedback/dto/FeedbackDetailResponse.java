package com.aris.templateapp.feedback.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Satu percakapan kritik &amp; saran. {@code messages[0]} selalu pesan pertama pengirim.
 * {@code appVersion} dan {@code deviceInfo} hanya dikirim ke admin.
 */
public record FeedbackDetailResponse(UUID id, String category, String subject, String status, String senderRole,
                                     Integer rating, String source, FeedbackSummaryResponse.Sender sender,
                                     UUID organizationId, String organizationName, String appVersion, String deviceInfo,
                                     List<Message> messages, Instant createdAt, Instant updatedAt) {

    /** @param authorName nama staf ditampilkan sebagai "Tim OrgSpace" ke pengirim, agar akun staf tidak terbuka */
    public record Message(UUID id, boolean fromStaff, String authorName, String body, Instant createdAt) {
    }
}
