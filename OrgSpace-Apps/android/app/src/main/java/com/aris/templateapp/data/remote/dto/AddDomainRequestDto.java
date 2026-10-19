package com.aris.templateapp.data.remote.dto;

public class AddDomainRequestDto {
    public String hostname;

    public AddDomainRequestDto() {
    }

    public AddDomainRequestDto(String hostname) {
        this.hostname = hostname;
    }
}
