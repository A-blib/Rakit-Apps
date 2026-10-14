package com.aris.templateapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class DirectPublishRequestDto {
    @SerializedName("name")
    public String name;

    @SerializedName("sourceTemplateId")
    public String sourceTemplateId;

    @SerializedName("templateVersion")
    public Integer templateVersion;

    @SerializedName("content")
    public Map<String, Object> content;

    @SerializedName("missingCount")
    public Integer missingCount;

    @SerializedName("slug")
    public String slug;

    public DirectPublishRequestDto(String name, String sourceTemplateId, Integer templateVersion,
                                   Map<String, Object> content, Integer missingCount, String slug) {
        this.name = name;
        this.sourceTemplateId = sourceTemplateId;
        this.templateVersion = templateVersion;
        this.content = content;
        this.missingCount = missingCount;
        this.slug = slug;
    }
}
