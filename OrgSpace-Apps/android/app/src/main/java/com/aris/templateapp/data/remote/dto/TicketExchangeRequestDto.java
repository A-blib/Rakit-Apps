package com.aris.templateapp.data.remote.dto;

/** Body /auth/github/exchange: tiket dari deep link templateapp://auth/callback?ticket=... */
public class TicketExchangeRequestDto {
    public final String ticket;

    public TicketExchangeRequestDto(String ticket) {
        this.ticket = ticket;
    }
}
