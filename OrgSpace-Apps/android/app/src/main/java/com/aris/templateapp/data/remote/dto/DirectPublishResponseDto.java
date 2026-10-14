package com.aris.templateapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

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
}
