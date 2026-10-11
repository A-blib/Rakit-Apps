package com.aris.templateapp.data.remote.dto;

/** Satu baris GET /organizations (pemilih ruang kerja). type: organization/personal; myRole: owner/admin/editor. */
public class OrganizationSummaryDto {
    public String id;
    public String type;
    public String name;
    public String shortName;
    public String logoUrl;
    public String myRole;
}
