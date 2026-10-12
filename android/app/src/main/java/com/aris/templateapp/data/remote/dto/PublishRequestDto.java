package com.aris.templateapp.data.remote.dto;

/** Permintaan "Minta diterbitkan" yang masih terbuka. */
public class PublishRequestDto {
    public String id;
    public String projectId;
    public String projectName;
    public String requestedByName;
    public String note;
    public String createdAt;
}
