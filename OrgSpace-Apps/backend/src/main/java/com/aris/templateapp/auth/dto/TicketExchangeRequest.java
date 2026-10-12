package com.aris.templateapp.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** @param ticket nilai {@code ticket} dari deep link {@code templateapp://auth/callback?ticket=...} */
public record TicketExchangeRequest(
        @NotBlank(message = "Ticket wajib diisi")
        String ticket) {
}
