package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

/** "Minta diterbitkan" dari Editor yang masih menunggu Admin/Pemilik. */
public class PublishRequest {

    public final String id;
    public final String websiteId;
    public final String websiteName;
    @Nullable public final String requestedByName;
    @Nullable public final String note;
    public final long createdAt;

    public PublishRequest(String id, String websiteId, String websiteName, @Nullable String requestedByName,
                          @Nullable String note, long createdAt) {
        this.id = id;
        this.websiteId = websiteId;
        this.websiteName = websiteName;
        this.requestedByName = requestedByName;
        this.note = note;
        this.createdAt = createdAt;
    }
}
