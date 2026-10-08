package com.aris.templateapp.upload.publish;

import java.util.UUID;

/** Diterbitkan saat provider menekan Kirim; memicu pengecekan akhir dan pembuatan paket di latar belakang. */
public record SubmittedEvent(UUID templateId, UUID checkId) {
}
