package com.aris.templateapp.data.remote.dto;

/** Body ganti nama (PATCH /projects/{id}) dan duplikat (POST /projects/{id}/duplicate, name boleh null). */
public class WebsiteNameRequestDto {
    public final String name;

    public WebsiteNameRequestDto(String name) {
        this.name = name;
    }
}
