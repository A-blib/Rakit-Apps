package com.aris.templateapp.data.remote.dto;

/** Body POST /organizations. Untuk ruang pribadi cukup type + name; field null tidak dikirim. */
public class CreateOrganizationRequestDto {
    public final String type;
    public final String name;
    public String shortName;
    public String category;
    public String level;
    public String institutionName;
    public String city;
    public Integer periodStartYear;
    public Integer periodEndYear;
    public String positionTitle;

    public CreateOrganizationRequestDto(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
