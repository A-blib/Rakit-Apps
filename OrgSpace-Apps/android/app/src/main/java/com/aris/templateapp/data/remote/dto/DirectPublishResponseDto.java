package com.aris.templateapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectPublishResponseDto {
    @SerializedName("projectId")
    public String projectId;

    @SerializedName("organizationId")
    public String organizationId;

    @SerializedName("organizationName")
    public String organizationName;

    @SerializedName("siteId")
    public String siteId;

    @SerializedName("slug")
    public String slug;

    @SerializedName("url")
    public String url;

    @SerializedName("port")
    public int port;

    @SerializedName("status")
    public String status;

    @SerializedName("version")
    public Integer version;

    /** Kunci isian foto yang filenya tidak ada lagi di HP; server memakai foto contoh template untuk isian itu. */
    @SerializedName("missingImages")
    public List<String> missingImages;
}
