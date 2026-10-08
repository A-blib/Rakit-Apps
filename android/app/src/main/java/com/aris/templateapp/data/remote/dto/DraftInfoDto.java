package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Body PATCH /providers/me/uploads/{id}/info. Field null tidak dikirim (Gson) dan tidak diubah server. */
public class DraftInfoDto {
    public String name;
    public String category;
    public String description;
    public List<String> keywords;
}
