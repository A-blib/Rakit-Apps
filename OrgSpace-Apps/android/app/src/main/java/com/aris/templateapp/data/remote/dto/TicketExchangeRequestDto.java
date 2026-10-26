package com.aris.templateapp.data.remote.dto;

/** Body /auth/github/exchange dan /users/me/identities/github/exchange: tiket dari deep link + verifier app ini. */
public class TicketExchangeRequestDto {
    public final String ticket;
    public final String codeVerifier;

    public TicketExchangeRequestDto(String ticket, String codeVerifier) {
        this.ticket = ticket;
        this.codeVerifier = codeVerifier;
    }
}
