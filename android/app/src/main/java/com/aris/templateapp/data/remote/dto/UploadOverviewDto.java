package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Halaman awal tab Upload (GET /providers/me/uploads, alur-fitur-upload.md bagian 3.1). */
public class UploadOverviewDto {
    public List<NeedsFixDto> needsFix;
    public List<CheckingDto> checking;
    public List<DraftItemDto> drafts;
    public int draftCount;
    public int draftLimit;
    public boolean canStartNew;

    public boolean isEmpty() {
        return (needsFix == null || needsFix.isEmpty()) && (checking == null || checking.isEmpty())
                && (drafts == null || drafts.isEmpty());
    }

    public static class NeedsFixDto {
        public String templateId;
        public String fileName;
        public int errorCount;
        public int warningCount;
        public String updatedAt;
    }

    public static class CheckingDto {
        public String templateId;
        public String fileName;
        public String stage;
    }

    public static class DraftItemDto {
        public String templateId;
        public String name;
        public String thumbnailUrl;
        public int wizardStep;
        public int fieldCount;
        public String updatedAt;
        public String deleteAt;
        public boolean expiringSoon;
    }
}
