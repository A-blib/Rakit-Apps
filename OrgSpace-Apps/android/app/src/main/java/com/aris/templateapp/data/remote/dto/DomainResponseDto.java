package com.aris.templateapp.data.remote.dto;

import java.util.List;

public class DomainResponseDto {
    public String id;
    public String siteId;
    public String hostname;
    public String status; // pending, active, failed, removed
    public String verificationToken;
    public String targetCname;
    public String targetA;
    public String activatedAt;
    public List<DomainInstructionDto> instructions;
    public String lastError;

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }
}
