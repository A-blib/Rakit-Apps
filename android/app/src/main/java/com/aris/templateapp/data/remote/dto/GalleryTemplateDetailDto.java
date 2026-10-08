package com.aris.templateapp.data.remote.dto;

import androidx.annotation.Nullable;

import java.util.List;

/** Detail template untuk layar Unduh (GET /templates/{id}). */
public class GalleryTemplateDetailDto {
    public String id;
    public String name;
    @Nullable
    public String category;
    @Nullable
    public String description;
    public List<String> keywords;
    public String creatorName;
    @Nullable
    public String thumbnailUrl;
    public long downloads;
    public List<String> pages;
    public List<String> libraries;
    public boolean responsive;
    public int version;
    /** Null jika template belum punya paket (tidak bisa dipakai). */
    @Nullable
    public Long packageSizeBytes;
}
