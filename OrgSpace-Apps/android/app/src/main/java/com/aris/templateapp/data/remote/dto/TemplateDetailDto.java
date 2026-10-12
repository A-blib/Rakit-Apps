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
        /** running · passed · failed */
        public String status;
        /** Tahap yang sedang berjalan (uploaded · opening_zip · structure · html_library · size · done); null untuk data lama. */
        public String stage;
        public String finishedAt;
        public List<IssueDto> errors;
        public List<IssueDto> warnings;
    }

    public static class IssueDto {
        /** Dipakai untuk "Ini keliru? Laporkan". */
        public String id;
        /** Kode aturan, juga kunci artikel Panduan (alur-fitur-upload.md 5.11). */
        public String code;
        /** Judul singkat aturan, mis. "File index.html tidak ada"; null untuk kode di luar daftar aturan. */
        public String title;
        public String message;
        public String file;
        public Integer line;
        public String suggestion;
        /** true jika sudah dilaporkan keliru. */
        public boolean reported;
    }
}
