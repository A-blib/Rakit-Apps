package com.aris.templateapp.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddDomainRequest(
        @NotBlank(message = "Nama domain tidak boleh kosong")
        @Size(max = 253, message = "Panjang domain maksimal 253 karakter")
        String hostname
) {}
