package com.aris.templateapp.data.remote.dto;

/**
 * Satu kartu tab Website (GET /organizations/{id}/websites). status: draft/online/offline/suspended;
 * mode: template/assembled. lockedBy hanya terisi jika website sedang diedit anggota lain.
 */
public class WebsiteSummaryDto {
    public String id;
    public String name;
    public String mode;
    public String status;
    public boolean hasUnpublishedChanges;
    public String siteUrl;
    public int missingCount;
    public String thumbnailUrl;
    public String sourceTemplateId;
    public String createdAt;
    public String updatedAt;
    public String updatedByName;
    public LockHolderDto lockedBy;

    public static class LockHolderDto {
        public String name;
        public String expiresAt;
    }
}
