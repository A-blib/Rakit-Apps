package com.aris.templateapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * @param ticket       nilai {@code ticket}/{@code linkTicket} dari deep link {@code templateapp://auth/callback?...}
 * @param codeVerifier rahasia acak yang dibuat app saat memulai login GitHub (pasangan codeChallenge)
 */
public record TicketExchangeRequest(
        @NotBlank(message = "Ticket wajib diisi")
        String ticket,
        @NotBlank(message = "codeVerifier wajib diisi")
        @Pattern(regexp = "^[A-Za-z0-9._~-]{43,128}$", message = "Format codeVerifier tidak valid")
        String codeVerifier) {
}
