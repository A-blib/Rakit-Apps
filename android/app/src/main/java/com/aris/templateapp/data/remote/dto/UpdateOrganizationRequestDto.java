package com.aris.templateapp.data.remote.dto;

/**
 * Body PATCH /organizations/{id}. Semua isian dikirim ulang dan menggantikan isi lama (null = dikosongkan),
 * sama seperti form edit lain di app. Media sosial boleh link lengkap atau "@username"; server menormalkannya.
 */
public class UpdateOrganizationRequestDto {
    public String name;
    public String shortName;
    public String category;
    public String level;
    public String institutionName;
    public String city;
    public Integer periodStartYear;
    public Integer periodEndYear;
    public String address;
    public String email;
    public String whatsapp;
    public String instagram;
    public String tiktok;
    public String youtube;
}
