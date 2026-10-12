package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Satu baris Riwayat aktivitas. Kalimatnya disusun app dari {@code action} + {@code metadata} (ActivityText), agar
 * teksnya tetap di strings.xml.
 */
public class Activity {

    public final String id;
    @Nullable public final String actorName;
    public final String action;
    public final Map<String, Object> metadata;
    @Nullable public final Instant createdAt;

    public Activity(String id, @Nullable String actorName, String action, @Nullable Map<String, Object> metadata,
                    @Nullable Instant createdAt) {
        this.id = id;
        this.actorName = actorName;
        this.action = action;
        this.metadata = metadata == null ? Collections.emptyMap() : metadata;
        this.createdAt = createdAt;
    }

    /** Nilai teks metadata, mis. "memberName"; null jika tidak ada. */
    @Nullable
    public String meta(String key) {
        Object value = metadata.get(key);
        return value == null ? null : value.toString();
    }
}
