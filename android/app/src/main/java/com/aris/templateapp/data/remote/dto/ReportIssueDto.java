package com.aris.templateapp.data.remote.dto;

/** Body "Ini keliru? Laporkan": alasan boleh kosong. */
public class ReportIssueDto {
    public final String reason;

    public ReportIssueDto(String reason) {
        this.reason = reason;
    }
}
