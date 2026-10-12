package com.aris.templateapp.data.remote.dto;

/**
 * Satu baris GET /organizations (pemilih ruang kerja). type: organization/personal; myRole: owner/admin/editor;
 * category & level null untuk ruang pribadi.
 */
public class OrganizationSummaryDto {
    public String id;
    public String type;
    public String name;
    public String shortName;
    public String logoUrl;
    public String myRole;
    public String category;
    public String level;
}
