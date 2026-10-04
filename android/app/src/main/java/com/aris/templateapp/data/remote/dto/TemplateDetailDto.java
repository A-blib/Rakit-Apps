package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Detail template + hasil pengecekan terakhir (GET /providers/me/templates/{id}). */
public class TemplateDetailDto {
    public String id;
    public String name;
    public String category;
    public String thumbnailUrl;
    public String status;
    public int warningCount;
    public long views;
    public long downloads;
    public String createdAt;
    public String updatedAt;
    public String publishedAt;
    /** null jika belum pernah dicek (mis. draft). */
    public CheckDto latestCheck;

    public static class CheckDto {
        public int version;
        public String status;
        public String finishedAt;
        public List<IssueDto> errors;
        public List<IssueDto> warnings;
    }

    public static class IssueDto {
        public String code;
        public String message;
        public String file;
        public Integer line;
        public String suggestion;
    }
}
